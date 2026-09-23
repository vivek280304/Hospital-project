CREATE TABLE lab_test_result (
                                 id BIGINT NOT NULL AUTO_INCREMENT,
                                 order_id BIGINT NOT NULL,
                                 result VARCHAR(5000) NOT NULL,
                                 remarks VARCHAR(2000),
                                 created_at DATETIME NOT NULL,

                                 PRIMARY KEY (id),

                                 CONSTRAINT uk_lab_test_result_order
                                     UNIQUE (order_id),

                                 CONSTRAINT fk_lab_test_result_order
                                     FOREIGN KEY (order_id)
                                         REFERENCES lab_test_order(id)
);