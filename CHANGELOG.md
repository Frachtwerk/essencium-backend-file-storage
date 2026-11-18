# Changelog

## Version `unreleased`

### 🌟 Features

- **Spring Boot 4**: the module now requires Spring Boot 4.1 and essencium-backend 4.0
- build and CI run on JDK 25; the published JAR stays compatible with Java 21

### 🐞 Bug Fixes

- integration tests use RustFS as S3 server, because the MinIO server image is no longer published

### 🔨 Dependency Upgrades

- upgrade org.springframework.boot:spring-boot-starter-parent from 3.5.6 to 4.1.2
- upgrade de.frachtwerk:essencium-backend from 3.1.0 to 4.0.2
- upgrade software.amazon.awssdk:bom from 2.40.8 to 2.59.3
- upgrade org.postgresql:postgresql from 42.7.8 to 42.7.14 (test dependency)
- upgrade io.minio:minio from 8.6.0 to 9.0.4 (test dependency)
- upgrade com.squareup.okhttp3:okhttp-bom and okhttp-jvm from 5.1.0 to 5.5.1 (test dependency)
- upgrade Testcontainers to 2.x (artifacts renamed to `testcontainers-*`)
- upgrade maven-surefire-plugin and maven-failsafe-plugin to 3.6.1
- upgrade build-helper-maven-plugin from 3.6.1 to 3.8.0
- upgrade jacoco-maven-plugin from 0.8.14 to 0.8.16
- upgrade central-publishing-maven-plugin from 0.9.0 to 0.12.0
- upgrade git-code-format-maven-plugin and google-java-format from 5.4 to 6.2
- upgrade cyclonedx-maven-plugin from 2.9.1 to 2.9.3
- upgrade maven-jar-plugin from 3.5.0 to 3.5.1
- upgrade actions/setup-java from 5 to 6

## Version `0.22.0` (2025-10-20)

### 🌟 Features

### 🐞 Bug Fixes

### 🔨 Dependency Upgrades

- upgrade org.postgresql:postgresql from 42.7.7 to 42.7.8
- upgrade software.amazon.awssdk:bom from 2.32.30 to 2.35.9
- upgrade org.springframework.boot:spring-boot-starter-parent from 3.5.5 to 3.5.6
- upgrade de.frachtwerk:essencium-backend from 2.12.0 to 3.1.0

## Version `0.21.0` (2025-08-26)

### 🌟 Features

- **Java21**
- add integration tests to verify correct up- and download handling

### 🐞 Bug Fixes

### 🔨 Dependency Upgrades

- upgrade software.amazon.awssdk:bom from 2.31.2 to 2.32.29
- upgrade de.frachtwerk:essencium-backend from 2.10.1 to 2.12.0
- upgrade org.springframework.boot:spring-boot-starter-parent from 3.4.3 to 3.5.5

## Version `0.20.0` (2025-03-19)

### 🌟 Features

### 🐞 Bug Fixes

### 🔨 Dependency Upgrades

- upgrade de.frachtwerk:essencium-backend from 2.9.0` to 2.10.1
- upgrade org.springframework.boot:spring-boot-starter-parent from 3.3.5 to 3.4.3
- upgrade software.amazon.awssdk:bom from 2.29.9 to 2.31.2

## Version `0.19.0` (2024-11-12)

### 🌟 Features

### 🐞 Bug Fixes

### 🔨 Dependency Upgrades

- upgraded de.frachtwerk:essencium-backend from 2.8.0` to 2.9.0
- upgraded software.amazon.awssdk:bom from 2.28.4 to 2.29.9
- upgraded org.springframework.boot:spring-boot-starter-parent from 3.3.3 to 3.3.5

## Version `0.18.0` (2024-09-19)

### 🌟 Features

### 🐞 Bug Fixes

### 🔨 Dependency Upgrades

- upgraded de.frachtwerk:essencium-backend from 2.6.0` to 2.8.0
- upgraded software.amazon.awssdk:bom from 2.26.3 to 2.28.4
- upgraded org.springframework.boot:spring-boot-starter-parent from 3.3.0` to 3.3.3

## Version `0.17.0` (2024-06-17)

### 🌟 Features

### 🐞 Bug Fixes

### 🔨 Dependency Upgrades

- upgraded de.frachtwerk:essencium-backend from 2.5.14 to 2.6.0
- upgraded software.amazon.awssdk:bom from 2.25.32 to 2.26.3
- upgraded org.springframework.boot:spring-boot-starter-parent from 3.2.4 to 3.3.0

## Version `0.16.1 (2024-04-18)

### 🌟 Features

### 🐞 Bug Fixes

- fix: Use Builder instead of 'create()' for DefaultCredentialsProvider

### 🔨 Dependency Upgrades

- upgraded software.amazon.awssdk:bom from 2.25.22 to 2.25.32
- upgraded de.frachtwerk:essencium-backend from 2.5.13 to 2.5.14

## Version `0.16.0` (2024-04-03)

### 🌟 Features

### 🐞 Bug Fixes

### 🔨 Dependency Upgrades

- upgraded de.frachtwerk:essencium-backend from 2.5.11 to 2.5.13
- upgraded software.amazon.awssdk:bom from 2.24.9 to 2.25.22
- upgraded org.springframework.boot:spring-boot-starter-parent from 3.2.3 to 3.2.4

## Version `0.15.0` (2024-02-26)

### 🌟 Features

### 🐞 Bug Fixes

### 🔨 Dependency Upgrades

- upgraded de.frachtwerk:essencium-backend from 2.5.4 to 2.5.11
- upgraded software.amazon.awssdk:bom from 2.23.9 to 2.24.9
- upgraded org.springframework.boot:spring-boot-starter-parent from 3.2.2 to 3.2.3

## Version `0.14.6` (2024-01-24)

### 🌟 Features

### 🐞 Bug Fixes

### 🔨 Dependency Upgrades

- upgraded de.frachtwerk:essencium-backend from 2.5.1 to 2.5.4
- upgraded org.springframework.boot:spring-boot-starter-parent from 3.2.1 to 3.2.2
- upgraded software.amazon.awssdk BOM from 2.23.1 to 2.23.9

## Version `0.14.5` (2024-01-15)

### 🌟 Features

### 🐞 Bug Fixes

### 🔨 Dependency Upgrades

- upgraded de.frachtwerk:essencium-backend from 2.5.0` to 2.5.1
- upgraded software.amazon.awssdk BOM from 2.22.11 to 2.23.1

## Version `0.14.4` (2024-01-08)

### 🌟 Features

### 🐞 Bug Fixes

### 🔨 Dependency Upgrades

- upgraded de.frachtwerk:essencium-backend from 2.4.1 to 2.5.0
- upgraded software.amazon.awssdk BOM from 2.22.3 to 2.22.11

## Version `0.14.3` (2023-12-22)

### 🌟 Features

### 🐞 Bug Fixes

### 🔨 Dependency Upgrades

- upgraded org.springframework.boot:spring-boot-starter-parent from 3.2.0` to 3.2.1
- upgraded de.frachtwerk:essencium-backend from 2.4.7 to 2.4.11
- upgraded software.amazon.awssdk BOM from 2.21.99 to 2.22.3

## Version `0.14.2` (2023-11-27)

### 🌟 Features

### 🐞 Bug Fixes

- removed non-working aws-kms-encryption

### 🔨 Dependency Upgrades

- upgraded org.springframework.boot:spring-boot-starter-parent from 3.1.5 to 3.2.0
- upgraded de.frachtwerk:essencium-backend from 2.4.6 to 2.4.7
- upgraded software.amazon.awssdk BOM from 2.21.6 to 2.21.99

## Version `0.14.1` (2023-10-25)

### 🌟 Features

### 🐞 Bug Fixes

### 🔨 Dependency Upgrades

- upgraded org.springframework.boot:spring-boot-starter-parent from 3.1.4 to 3.1.5
- upgraded software.amazon.awssdk BOM from 2.20.157 to 2.21.6
- upgraded de.frachtwerk:essencium-backend from 2.4.5 to 2.4.6

## Version `0.14.0` (2023-09-02)

### 🌟 Features

- Essencium-Migration
- Migration to GitHub-Repository

### 🐞 Bug Fixes

### 🔨 Dependency Upgrades
