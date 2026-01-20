-- Fixes runtime errors when running against an existing DB that was created
-- before adding the Hoppmann features.

-- Advertisement: booking metadata (F6)
ALTER TABLE IF EXISTS advertisement ADD COLUMN IF NOT EXISTS booked boolean NOT NULL DEFAULT false;
ALTER TABLE IF EXISTS advertisement ADD COLUMN IF NOT EXISTS booked_student_id bigint;
ALTER TABLE IF EXISTS advertisement ADD COLUMN IF NOT EXISTS booked_series_id bigint;

-- LessonSeries: payment metadata + link to course (F6/F12)
ALTER TABLE IF EXISTS lesson_series ADD COLUMN IF NOT EXISTS advertisement_id bigint;
ALTER TABLE IF EXISTS lesson_series ADD COLUMN IF NOT EXISTS price_per_hour double precision;
ALTER TABLE IF EXISTS lesson_series ADD COLUMN IF NOT EXISTS payment_method varchar(20);
ALTER TABLE IF EXISTS lesson_series ADD COLUMN IF NOT EXISTS paid boolean NOT NULL DEFAULT false;
ALTER TABLE IF EXISTS lesson_series ADD COLUMN IF NOT EXISTS paid_at timestamp;