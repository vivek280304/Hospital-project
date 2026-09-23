CREATE TABLE lab_test (
                          id BIGINT NOT NULL AUTO_INCREMENT,
                          name VARCHAR(255) NOT NULL,
                          description VARCHAR(500),
                          price DECIMAL(10,2) NOT NULL,
                          sample_type VARCHAR(255) NOT NULL,
                          active BOOLEAN NOT NULL DEFAULT TRUE,

                          PRIMARY KEY (id),
                          CONSTRAINT uk_lab_test_name UNIQUE (name)
);