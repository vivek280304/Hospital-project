ALTER TABLE doctor_schedule
    ADD CONSTRAINT chk_schedule_time
        CHECK (start_time < end_time),
    ADD CONSTRAINT chk_slot_duration_positive
        CHECK (slot_duration > 0),
    ADD CONSTRAINT chk_slot_duration_within_schedule
        CHECK (
            slot_duration <= TIMESTAMPDIFF(
                MINUTE,
                start_time,
                end_time
            )
        );