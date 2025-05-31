CREATE TABLE IF NOT EXISTS kafka_config (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(255) UNIQUE NOT NULL,
    address VARCHAR(255) NOT NULL,
    topic VARCHAR(255) NOT NULL,
    consumer_group VARCHAR(255) NOT NULL,
    authentication VARCHAR(50),
    username VARCHAR(255),
    password VARCHAR(255),
    create_user_id BIGINT,
    update_user_id BIGINT,
    data_resource INTEGER,
    connection_status INTEGER
);
