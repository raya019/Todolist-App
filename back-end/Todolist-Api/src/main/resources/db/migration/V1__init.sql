CREATE TABLE users(
    id VARCHAR(255) NOT NULL,
    name VARCHAR(30) NOT NULL,
    email VARCHAR(30) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE todolist(
    id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    name_todolist VARCHAR(50) NOT NULL UNIQUE,
    is_done BOOLEAN NOT NULL DEFAULT false,
    PRIMARY KEY (id),
    constraint fk_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE refresh_token(
    id  SERIAL NOT NULL ,
    id_user VARCHAR(255) NOT NULL,
    token VARCHAR(255) NOT NULL,
    is_logged_out BOOLEAN NOT NULL,
    expired_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    constraint fk_user FOREIGN KEY (id_user) REFERENCES users(id)
);