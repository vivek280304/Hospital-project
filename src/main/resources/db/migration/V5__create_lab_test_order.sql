CREATE TABLE lab_test_order (
                                id BIGINT NOT NULL AUTO_INCREMENT,

                                patient_id BIGINT NOT NULL,
                                lab_test_id BIGINT NOT NULL,
                                technician_id BIGINT NULL,

                                scheduled_date DATE NOT NULL,
                                scheduled_time TIME NOT NULL,

                                status VARCHAR(30) NOT NULL,
                                ordered_at DATETIME NOT NULL,

                                collected_at DATETIME NULL,
                                processing_started_at DATETIME NULL,
                                completed_at DATETIME NULL,

                                PRIMARY KEY (id),

                                CONSTRAINT fk_lab_order_patient
                                    FOREIGN KEY (patient_id)
                                        REFERENCES patient(id),

                                CONSTRAINT fk_lab_order_test
                                    FOREIGN KEY (lab_test_id)
                                        REFERENCES lab_test(id),

                                CONSTRAINT fk_lab_order_technician
                                    FOREIGN KEY (technician_id)
                                        REFERENCES user(id),

                                INDEX idx_lab_order_patient (patient_id),
                                INDEX idx_lab_order_test (lab_test_id),
                                INDEX idx_lab_order_technician (technician_id),
                                INDEX idx_lab_order_status_date (status, scheduled_date)
);