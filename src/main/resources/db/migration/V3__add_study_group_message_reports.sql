-- Support for reporting group chat messages

ALTER TABLE report ADD COLUMN IF NOT EXISTS study_group_message_id bigint;
