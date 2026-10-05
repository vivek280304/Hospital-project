CREATE TABLE appointment_slot_holds (
                                        id BIGINT NOT NULL AUTO_INCREMENT,

                                        doctor_id BIGINT NOT NULL,

                                        appointment_date DATE NOT NULL,

                                        appointment_time TIME NOT NULL,

                                        patient_id BIGINT NOT NULL,

                                        order_id VARCHAR(100) NOT NULL,

                                        expires_at DATETIME NOT NULL,

                                        PRIMARY KEY (id),

                                        CONSTRAINT uk_doctor_slot_hold
                                            UNIQUE (
                                                    doctor_id,
                                                    appointment_date,
                                                    appointment_time
                                                ),

                                        CONSTRAINT uk_slot_hold_order_id
                                            UNIQUE (order_id),

                                        CONSTRAINT fk_slot_hold_doctor
                                            FOREIGN KEY (doctor_id)
                                                REFERENCES doctor(id)
);