CREATE TABLE user (
                      id SERIAL PRIMARY KEY,
                      first_name VARCHAR(255) NOT NULL,
                      last_name VARCHAR(255) NOT NULL,
                      email VARCHAR(255) NOT NULL,
                      username VARCHAR(255) NOT NULL,
                      password VARCHAR(255) NOT NULL
);

INSERT INTO user (first_name, last_name, email, username, password)
VALUES ('iqadot', 'kit', 'iqadot@gmail.com', 'iqadot', 'iqadot');
