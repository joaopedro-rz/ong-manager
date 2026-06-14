ALTER TABLE volunteer_applications
    ADD COLUMN skills_validated BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN skills_validated_at TIMESTAMP,
    ADD COLUMN skills_notes VARCHAR(255);

