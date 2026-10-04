CREATE TABLE payments (
                          id BIGINT NOT NULL AUTO_INCREMENT,

                          order_id VARCHAR(100) NOT NULL,
                          cf_order_id VARCHAR(100),
                          transaction_id VARCHAR(150),

                          amount DECIMAL(10,2) NOT NULL,

                          status VARCHAR(20) NOT NULL,

                          payment_method VARCHAR(50),

                          appointment_id BIGINT NOT NULL,

                          created_at DATETIME NOT NULL,
                          updated_at DATETIME NOT NULL,

                          PRIMARY KEY (id),

                          CONSTRAINT uk_payments_order_id
                              UNIQUE (order_id),

                          CONSTRAINT fk_payments_appointment
                              FOREIGN KEY (appointment_id)
                                  REFERENCES appointment(id)
);