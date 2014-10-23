--liquibase formatted sql

--changeset TaylorLABEJOF:20140912-productBaseline-1.0
--preconditions onFail:WARN onError:HALT
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM PRODUCTBASELINE
INSERT INTO PRODUCTBASELINE (
  ID,
  CREATIONDATE,
  DESCRIPTION,
  NAME,
  TYPE,
  CONFIGURATIONITEM_WORKSPACE_ID,
  CONFIGURATIONITEM_ID,
  PARTCOLLECTION_ID
);
SELECT
  ID,
  CREATIONDATE,
  DESCRIPTION,
  NAME,
  TYPE,
  CONFIGURATIONITEM_WORKSPACE_ID,
  CONFIGURATIONITEM_ID,
  PARTCOLLECTION_ID
FROM BASELINE;

--changeset TaylorLABEJOF:20140912-productBaseline-2.0
--preconditions onFail:WARN onError:WARN
--precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM information_schema.tables WHERE TABLE_NAME="BASELINE"
SET foreign_key_checks = 0;
DROP TABLE IF EXISTS BASELINE;
SET foreign_key_checks = 1;