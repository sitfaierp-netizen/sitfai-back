ALTER TABLE core_audit_event_store MODIFY COLUMN id VARCHAR(36) NOT NULL;
ALTER TABLE core_audit_event_store MODIFY COLUMN empresa_id VARCHAR(36) NOT NULL;
