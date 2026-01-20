-- Ensure the enum check constraint for report.type contains the new value STUDY_GROUP_MESSAGE.
-- Older schemas may have been created with a CHECK constraint listing only ADVERTISEMENT/MESSAGE.
DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM pg_constraint c
    JOIN pg_class t ON t.oid = c.conrelid
    WHERE t.relname = 'report'
      AND c.conname = 'report_type_check'
  ) THEN
    ALTER TABLE report DROP CONSTRAINT report_type_check;
  END IF;

  -- Recreate with all currently supported values.
  ALTER TABLE report
    ADD CONSTRAINT report_type_check
    CHECK (type IN ('ADVERTISEMENT', 'MESSAGE', 'STUDY_GROUP_MESSAGE'));
END $$;
