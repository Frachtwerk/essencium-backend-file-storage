# Frachtwerk Starter File Storage Module

To be used together with [web-starter/backend](https://git.frachtwerk.de/fw-dev/web-starter/backend) (
version >=`2.1.0`).

This module provides an abstraction layer to store files needed for your application. The actual persistence location
and
implementation is independent of the handling of files. This is achieved by providing various storage provider
implementations
that can be used independently of another.

It is possible to store a file with multiple storage providers to provide redundancy and to access these even if not all
used providers are available at the time of requesting a file.

## Configuration

### Development

In order to integrate the file-storage-module into your own application, a strategic decision must be made at the beginning. There are several methods available to generate the primary key of an entry (ID). By the integration of the specific components one determines which strategy is followed.

Example using the sequence strategy:
```java
@SpringBootApplication(
    scanBasePackages = {
      "de.frachtwerk.my.application",
      "de.frachtwerk.starter.backend",
      "de.frachtwerk.starter.storage.generic",
      "de.frachtwerk.starter.storage.impl.sequence"
    })
@EntityScan(
    basePackages = {
      "de.frachtwerk.my.application",
      "de.frachtwerk.starter.backend",
      "de.frachtwerk.starter.storage.generic",
      "de.frachtwerk.starter.storage.impl.sequence"
    })
@ConfigurationPropertiesScan(
    basePackages = {
      "de.frachtwerk.my.application",
      "de.frachtwerk.starter.backend",
      "de.frachtwerk.starter.storage.generic",
      "de.frachtwerk.starter.storage.impl.sequence",
    })
@EnableJpaRepositories(
    basePackages = {
      "de.frachtwerk.my.application",
      "de.frachtwerk.starter.backend",
      "de.frachtwerk.starter.storage.generic",
      "de.frachtwerk.starter.storage.impl.sequence"
    })
public class SpringBootApp {
  public static void main(String[] args) {
    SpringApplication.run(SpringBootApp.class, args);
  }
}
```

Note that when linking custom entities to files, do not specify an abstract `File` class, but the specific implementation of the `File` class (`SequenceFile` or `IdentityFile`).

### Environment

The module is configured by using the application.yaml config file of your starter application. The used prefix is
`file.storage`

### usedProviders

- Type: `String[]`
- Required: `yes`

Specifies the providers that should be used to store files. Note: This does not activate the specified providers.

### `local`

Config parameters for the local provider.

| parameter      | type    | default | required           | comment                                                                                                                                                                                                                                  |
|----------------|---------|---------|--------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `active`       | boolean | `false` |                    | Setting active to `true` tells the spring application to include components needed for the local storage provider in its component and entity scan. A file stored with the local provider cannot be loaded if active is not set to true. |
| `keepFileName` | boolean | `false` |                    | Whether to use the provided original file when storing the file on the file system. When set to `false` randomized uuid's are used as file names.                                                                                        |
| `useTempFiles` | boolean | `false` |                    | Whether to use temp files (e.g. in `/tmp`) that are deleted when the application stops. For development purposes.                                                                                                                        |
| `path`         | String  | ---     | :white_check_mark: | The absolute path of the directory on the file system of the starter application to use to store files.                                                                                                                                  |

### `s3`

Config parameters for the s3 provider.

| parameter     | type    | default | required           | comment                                                                                                                                                                                                                            |
|---------------|---------|---------|--------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `active`      | boolean | `false` |                    | Setting active to `true` tells the spring application to include components needed for the s3 storage provider in its component and entity scan. A file stored with the s3 provider cannot be loaded if active is not set to true. |
| `endpointUrl` | String  | ---     |                    | Defines the endpoint URI to be used, e.g. `"http://127.0.0.1:9000"` can be used to access a local S3-bucket using MinIo                                                                                                            |
| `region`      | String  | ---     |                    | Defines the AWS-Region the s3-bucket is located. All regions from `com.amazonaws.regions.Regions` are accepted.                                                                                                                    |
| `bucketName`  | String  | ---     | :white_check_mark: | Defines the name of the s3-bucket to be used. If there is no bucket with the given name, one is created.                                                                                                                           |
| `accessKey`   | String  | ---     |                    | Access Key defined by your s3-bucket host                                                                                                                                                                                          |
| `secretKey`   | String  | ---     |                    | Secret Key defined by your s3-bucket host                                                                                                                                                                                          |
| `kmsKeyId`    | String  | ---     |                    | :warning: [ experimental, untested ] Use a KMS-Key and KMS-Region provided by AWS IAM to access the S3-Bucket.                                                                                                                     |
| `kmsRegion`   | String  | ---     |                    | :warning: [ experimental, untested ] Use a KMS-Key and KMS-Region provided by AWS IAM to access the S3-Bucket.                                                                                                                     |

If  `endpointUrl`, `accessKey` and `secretKey` are not provided, the aws default credentials provider chain is used. By
setting `kmsKeyId` and `kmsRegion` the s3 provider uses a KMS-Key to encrypt the files.
For non-AWS S3-Buckets, the `endpointUrl`, `accessKey` and `secretKey` have to be set to the S3-Endpoint and Credentials
of the host.

For using the s3 provider additional dependencies have to be added in your project:

```xml

<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>de.frachtwerk</groupId>
    <artifactId>yourApplication</artifactId>

    <!-- ... -->

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>software.amazon.awssdk</groupId>
                <artifactId>bom</artifactId>
                <version>2.20.82</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <!-- s3 Storage -->
        <dependency>
            <artifactId>s3</artifactId>
            <groupId>software.amazon.awssdk</groupId>
        </dependency>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>kms</artifactId>
        </dependency>
    </dependencies>

</project>
```

## Requirements

- JDK 17
- Maven

## Testing

The backend lib comprises both unit- and integration tests.

- **Unit Tests:** `mvn test`
- **Integration Tests:** `mvn failsafe:integration-test`

---

Frachtwerk GmbH
