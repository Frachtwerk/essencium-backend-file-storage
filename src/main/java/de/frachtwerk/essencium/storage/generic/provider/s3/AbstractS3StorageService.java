/*
 * Copyright (C) 2023 Frachtwerk GmbH, Leopoldstraße 7C, 76133 Karlsruhe.
 *
 * This file is part of essencium-backend.
 *
 * essencium-backend is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * essencium-backend is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with essencium-backend. If not, see <http://www.gnu.org/licenses/>.
 */

package de.frachtwerk.essencium.storage.generic.provider.s3;

import de.frachtwerk.essencium.storage.generic.model.AbstractFile;
import de.frachtwerk.essencium.storage.generic.model.AbstractStorageInfo;
import de.frachtwerk.essencium.storage.generic.model.Providers;
import de.frachtwerk.essencium.storage.generic.service.MimeTypeHelper;
import de.frachtwerk.essencium.storage.generic.service.StorageService;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.EncryptRequest;
import software.amazon.awssdk.services.kms.model.EncryptResponse;
import software.amazon.awssdk.services.kms.model.KmsException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.*;

public abstract class AbstractS3StorageService<
        F extends AbstractFile<F, ID, S>,
        ID extends Serializable,
        S extends AbstractStorageInfo<F, ID, S>>
    implements StorageService<F, ID, S> {

  private final Logger LOG = LoggerFactory.getLogger(AbstractS3StorageService.class);
  @NotNull private final S3StorageConfiguration config;
  private final MimeTypeHelper mimeTypeHelper;

  public AbstractS3StorageService(
      @NotNull S3StorageConfiguration config, MimeTypeHelper mimeTypeHelper) {
    this.config = config;
    this.mimeTypeHelper = mimeTypeHelper;
  }

  // Create the S3Client object.
  private S3Client getClient() {
    AwsCredentialsProvider credentialProvider = getCredentialProvider();
    S3ClientBuilder builder =
        S3Client.builder()
            .credentialsProvider(credentialProvider)
            .region(getRegion(config.getRegion()));

    if (StringUtils.isNotBlank(config.getEndpointUrl())) {
      LOG.debug("Using custom endpoint: {}", config.getEndpointUrl());
      builder.endpointOverride(URI.create(config.getEndpointUrl())).forcePathStyle(true);
    }
    return builder.build();
  }

  // Creates a credentials provider.
  private AwsCredentialsProvider getCredentialProvider() {
    /* according to https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/auth/credentials/DefaultCredentialsProvider.html DefaultCredentialsProvider covers most cases of authentication.

        AWS credentials provider chain that looks for credentials in this order:

        1. Java System Properties - aws.accessKeyId and aws.secretAccessKey
        2. Environment Variables - AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY
        3. Web Identity Token credentials from system properties or environment variables
        4. Credential profiles file at the default location (~/.aws/credentials) shared by all AWS SDKs and the AWS CLI
        5. Credentials delivered through the Amazon EC2 container service if "AWS_CONTAINER_CREDENTIALS_RELATIVE_URI" environment variable is set and security manager has permission to access the variable,
        6. Instance profile credentials delivered through the Amazon EC2 metadata service

        So if no Custom Endpoint and AccessKey and SecretKey are set, the DefaultCredentialsProvider should be sufficient.
    */
    if (StringUtils.isNotBlank(config.getEndpointUrl())
        && StringUtils.isNotBlank(config.getAccessKey())
        && StringUtils.isNotBlank(config.getSecretKey())) {
      LOG.debug("Using custom credentials provider");
      AwsCredentials awsCredentials =
          AwsBasicCredentials.create(config.getAccessKey(), config.getSecretKey());
      return StaticCredentialsProvider.create(awsCredentials);
    } else {
      LOG.debug("Using default credentials provider");
      DefaultCredentialsProvider defaultCredentialsProvider = DefaultCredentialsProvider.create();
      LOG.debug(
          "DefaultCredentialsProvider, accessKeyId: {}",
          defaultCredentialsProvider.resolveCredentials().accessKeyId());
      return defaultCredentialsProvider;
    }
  }

  // Return a KmsClient object.
  private KmsClient getKMSClient() {
    Region region = getRegion(config.getKmsRegion());
    return KmsClient.builder().credentialsProvider(getCredentialProvider()).region(region).build();
  }

  private Region getRegion(@Nullable String region) {
    if (StringUtils.isNotBlank(region)) return Region.of(region);
    else return Region.EU_CENTRAL_1;
  }

  @Override
  public final S saveFile(String originalName, byte[] content) throws IOException {
    LOG.debug("Saving file {} to S3 (saveFile())", originalName);
    try (S3Client s3 = getClient()) {
      // ensure bucket exists
      testBucketAccess(s3, config.getBucketName());

      // create object key
      String s3ObjectKey = getNewObjectKey(s3, config.getBucketName());

      // apply kms encryption in aws context if enabled
      if (!StringUtils.isBlank(config.getKmsKeyId())) {
        content = encryptWithKmsKey(content, config.getKmsKeyId());
      }

      // prepare upload
      PutObjectRequest putObjectRequest =
          PutObjectRequest.builder()
              .bucket(config.getBucketName())
              .key(s3ObjectKey)
              .contentType(mimeTypeHelper.getMimeType(s3ObjectKey, content))
              .contentLength((long) content.length)
              .build();
      PutObjectResponse putObjectResponse =
          s3.putObject(putObjectRequest, RequestBody.fromBytes(content));
      LOG.debug("S3: PutObjectResponse: {}", putObjectResponse);

      LOG.info("S3: {} has been uploaded successfully", s3ObjectKey);

      final S info = getNewAbstractS3StorageInfo(null, s3ObjectKey);
      info.setContent(new InputStreamResource(new ByteArrayInputStream(content)));
      return info;
    } catch (Exception e) {
      LOG.error("Error saving file to S3", e);
      throw e;
    }
  }

  protected abstract <SI extends AbstractS3StorageInfo<F, ID, S>> SI getNewAbstractS3StorageInfo(
      F file, String s3ObjectKey);

  @Override
  public final boolean deleteFile(S abstractInfo) {
    AbstractS3StorageInfo<F, ID, S> info = (AbstractS3StorageInfo<F, ID, S>) abstractInfo;
    try (S3Client s3 = getClient()) {
      // ensure bucket exists
      testBucketAccess(s3, config.getBucketName());

      LOG.info("S3: Deleting object {}", info.getS3ObjectKey());
      DeleteObjectsRequest deleteObjectsRequest =
          DeleteObjectsRequest.builder()
              .bucket(config.getBucketName())
              .delete(
                  Delete.builder()
                      .objects(
                          List.of(ObjectIdentifier.builder().key(info.getS3ObjectKey()).build()))
                      .build())
              .build();
      s3.deleteObjects(deleteObjectsRequest);
      LOG.info("S3: Object {} has been deleted successfully", info.getS3ObjectKey());
      return true;
    } catch (Exception e) {
      LOG.error("Error deleting file from S3", e);
      throw e;
    }
  }

  @Override
  public final S loadFile(S abstractInfo) {
    AbstractS3StorageInfo<F, ID, S> info = (AbstractS3StorageInfo<F, ID, S>) abstractInfo;
    try (S3Client s3 = getClient()) {
      // ensure bucket exists
      testBucketAccess(s3, config.getBucketName());
      GetObjectRequest objectRequest =
          GetObjectRequest.builder()
              .key(info.getS3ObjectKey())
              .bucket(config.getBucketName())
              .build();
      byte[] byteArray = s3.getObjectAsBytes(objectRequest).asByteArray();
      // decrypt using kms encryption in aws context if enabled
      if (!StringUtils.isBlank(config.getKmsKeyId())) {
        byteArray = decryptWithKmsKey(byteArray, config.getKmsKeyId());
      }
      info.setContent(new ByteArrayResource(byteArray));
      return (S) info;
    } catch (Exception e) {
      LOG.error("Error loading file from S3", e);
      throw e;
    }
  }

  @Override
  public Providers getType() {
    return Providers.S3;
  }

  /** Checks if bucket exists and user has permission to access it */
  public void testBucketAccess(S3Client s3, String bucketName) {
    // Try to access bucket using a HeadBucketRequest which is a fast, non-invasive operation.
    HeadBucketRequest headBucketRequest = HeadBucketRequest.builder().bucket(bucketName).build();
    HeadBucketResponse headBucketResponse = s3.headBucket(headBucketRequest);
    LOG.debug("S3: HeadBucketResponse: {}", headBucketResponse.responseMetadata());
    LOG.debug("S3: Bucket {} exists", bucketName);
  }

  /** Returns a new unique object key that does not exist in the bucket */
  private String getNewObjectKey(S3Client s3, @NotNull @NotBlank String bucketName) {
    ListObjectsV2Request listObjectsV2Request =
        ListObjectsV2Request.builder().bucket(bucketName).build();
    ListObjectsV2Response listObjectsV2Response = s3.listObjectsV2(listObjectsV2Request);
    LOG.debug(
        "S3: Bucket {} contains {} object keys", bucketName, listObjectsV2Response.keyCount());
    List<String> existingKeys =
        listObjectsV2Response.contents().stream().map(S3Object::key).toList();
    String s3ObjectKey;
    do {
      s3ObjectKey = UUID.randomUUID().toString();
    } while (existingKeys.contains(s3ObjectKey));
    return s3ObjectKey;
  }

  private byte[] encryptWithKmsKey(byte[] data, String keyId) {
    try (KmsClient kmsClient = getKMSClient()) {
      SdkBytes myBytes = SdkBytes.fromByteArray(data);
      EncryptRequest encryptRequest =
          EncryptRequest.builder().keyId(keyId).plaintext(myBytes).build();
      EncryptResponse encryptResponse = kmsClient.encrypt(encryptRequest);
      LOG.debug("The encryption algorithm is " + encryptResponse.encryptionAlgorithm().toString());
      // Return the encrypted data.
      return encryptResponse.ciphertextBlob().asByteArray();
    } catch (KmsException e) {
      LOG.error("Error encrypting data", e);
      throw e;
    }
  }

  // Decrypt the data passed as a byte array.
  private byte[] decryptWithKmsKey(byte[] data, String keyId) {
    try (KmsClient kmsClient = getKMSClient()) {
      SdkBytes encryptedData = SdkBytes.fromByteArray(data);
      DecryptRequest decryptRequest =
          DecryptRequest.builder().ciphertextBlob(encryptedData).keyId(keyId).build();
      return kmsClient.decrypt(decryptRequest).plaintext().asByteArray();
    } catch (KmsException e) {
      LOG.error("Error decrypting data", e);
      throw e;
    }
  }
}
