CREATE TABLE doctor_leaves (
                               id BIGINT NOT NULL AUTO_INCREMENT,
                               doctor_id BIGINT NOT NULL,
                               leave_date DATE NOT NULL,
                               reason VARCHAR(255),

                               PRIMARY KEY (id),

                               CONSTRAINT fk_doctor_leaves_doctor
                                   FOREIGN KEY (doctor_id)
                                       REFERENCES doctor(id),

                               CONSTRAINT uk_doctor_leave_date
                                   UNIQUE (doctor_id, leave_date)
);