-- Integration W7: writeback idempotency
SET NAMES utf8mb4;

ALTER TABLE int_writeback_log
  ADD COLUMN client_request_id VARCHAR(64) NULL AFTER endpoint_id,
  ADD UNIQUE KEY uk_writeback_client_req (client_request_id, org_id);
