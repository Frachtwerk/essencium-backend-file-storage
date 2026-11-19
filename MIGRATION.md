# Migration Guide

## Version `0.23.0`

### Spring Boot 4 and essencium-backend 4

The module requires Spring Boot 4.1 and essencium-backend 4.0. Follow the migration guide of essencium-backend for your application first.

### Database: renamed columns

essencium-backend 4 converts camelCase field names to snake_case column names by default. This happens when `essencium.jpa.camel-case-to-underscore` is not set. Version 3 used the field names unchanged in this case. The tables of this module change as follows:

| Table | Before | After |
|---|---|---|
| `FW_FILE` | `createdat`, `createdby`, `updatedat`, `updatedby`, `mimetype` | `created_at`, `created_by`, `updated_at`, `updated_by`, `mime_type` |
| `FW_FILE_STORAGEINFOS` | table name, `storageinfos_id`, `storageinfos_order` | `FW_FILE_STORAGE_INFOS`, `storage_infos_id`, `storage_infos_order` |
| `FW_LOCAL_STORAGE_INFO` | `createdat`, `createdby`, `updatedat`, `updatedby` | `created_at`, `created_by`, `updated_at`, `updated_by` |
| `FW_S3_STORAGE_INFO` | `createdat`, `createdby`, `updatedat`, `updatedby`, `s3objectkey` | `created_at`, `created_by`, `updated_at`, `updated_by`, `s3_object_key` |

The table names and the sequence `hibernate_sequence` stay the same. The change applies to the sequence, UUID and identity variants alike. The tables of essencium-backend itself change in the same way.

Rename the columns before the first start with the new version. Otherwise, Hibernate adds empty columns next to the old ones, and existing files lose their storage entries and MIME types.

**Applications without `essencium.jpa.camel-case-to-underscore` or with `false`** (PostgreSQL):

```sql
ALTER TABLE "FW_FILE" RENAME COLUMN createdat TO created_at;
ALTER TABLE "FW_FILE" RENAME COLUMN createdby TO created_by;
ALTER TABLE "FW_FILE" RENAME COLUMN updatedat TO updated_at;
ALTER TABLE "FW_FILE" RENAME COLUMN updatedby TO updated_by;
ALTER TABLE "FW_FILE" RENAME COLUMN mimetype TO mime_type;

ALTER TABLE "FW_FILE_STORAGEINFOS" RENAME TO "FW_FILE_STORAGE_INFOS";
ALTER TABLE "FW_FILE_STORAGE_INFOS" RENAME COLUMN storageinfos_id TO storage_infos_id;
ALTER TABLE "FW_FILE_STORAGE_INFOS" RENAME COLUMN storageinfos_order TO storage_infos_order;

ALTER TABLE "FW_LOCAL_STORAGE_INFO" RENAME COLUMN createdat TO created_at;
ALTER TABLE "FW_LOCAL_STORAGE_INFO" RENAME COLUMN createdby TO created_by;
ALTER TABLE "FW_LOCAL_STORAGE_INFO" RENAME COLUMN updatedat TO updated_at;
ALTER TABLE "FW_LOCAL_STORAGE_INFO" RENAME COLUMN updatedby TO updated_by;

ALTER TABLE "FW_S3_STORAGE_INFO" RENAME COLUMN createdat TO created_at;
ALTER TABLE "FW_S3_STORAGE_INFO" RENAME COLUMN createdby TO created_by;
ALTER TABLE "FW_S3_STORAGE_INFO" RENAME COLUMN updatedat TO updated_at;
ALTER TABLE "FW_S3_STORAGE_INFO" RENAME COLUMN updatedby TO updated_by;
ALTER TABLE "FW_S3_STORAGE_INFO" RENAME COLUMN s3objectkey TO s3_object_key;
```

**Applications with `essencium.jpa.camel-case-to-underscore: true`**: only one column changes, because the new naming strategy separates digits differently.

```sql
ALTER TABLE "FW_S3_STORAGE_INFO" RENAME COLUMN s3object_key TO s3_object_key;
```
