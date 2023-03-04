CREATE TABLE search_count
(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    keyword VARCHAR(255),
    count   BIGINT,
    version BIGINT
);