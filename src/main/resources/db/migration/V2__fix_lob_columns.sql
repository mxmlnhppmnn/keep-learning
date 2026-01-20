-- Fix fuer PostgreSQL: @Lob String kann als LargeObject (OID) angelegt werden.
-- Das fuehrt beim Lesen ausserhalb einer expliziten Transaktion zu:
--   "LargeObjects (LOB) duerfen im Modus 'auto-commit' nicht verwendet werden".
--
-- Loesung: content-Spalten als TEXT sicherstellen.
-- Wenn eine bestehende Spalte vom Typ OID ist, wird sie nach *_lo umbenannt
-- und eine neue TEXT-Spalte mit dem urspruenglichen Namen angelegt.
-- (Datenmigration aus OID ist hier nicht vorgesehen; in der Regel sind die
--  Tabellen in der Dev-DB leer oder neu erzeugbar.)

DO $$
BEGIN
  -- invoice.content
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'invoice'
      AND column_name = 'content'
      AND udt_name = 'oid'
  ) THEN
    ALTER TABLE invoice RENAME COLUMN content TO content_lo;
    ALTER TABLE invoice ADD COLUMN content text;
  ELSIF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'invoice'
      AND column_name = 'content_lo'
  ) AND NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'invoice'
      AND column_name = 'content'
  ) THEN
    ALTER TABLE invoice ADD COLUMN content text;
  END IF;

  -- income_statement.content
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'income_statement'
      AND column_name = 'content'
      AND udt_name = 'oid'
  ) THEN
    ALTER TABLE income_statement RENAME COLUMN content TO content_lo;
    ALTER TABLE income_statement ADD COLUMN content text;
  ELSIF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'income_statement'
      AND column_name = 'content_lo'
  ) AND NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'income_statement'
      AND column_name = 'content'
  ) THEN
    ALTER TABLE income_statement ADD COLUMN content text;
  END IF;
END $$;
