CREATE SEQUENCE user_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE rt_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE mlt_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE profile_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE users (
    id         BIGINT       NOT NULL DEFAULT nextval('user_id_seq'),
    email      VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT users_email_unique UNIQUE (email)
);

CREATE TABLE profiles (
    id         BIGINT       NOT NULL DEFAULT nextval('profile_seq'),
    email      VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT profiles_email_unique UNIQUE (email)
);

CREATE TABLE refresh_tokens (
    id         BIGINT       NOT NULL DEFAULT nextval('rt_id_seq'),
    user_id    BIGINT       NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL,
    expires_at TIMESTAMPTZ  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT refresh_tokens_token_hash_unique UNIQUE (token_hash),
    CONSTRAINT refresh_tokens_user_fk FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX refresh_tokens_user_id_idx ON refresh_tokens (user_id);

CREATE TABLE magic_link_tokens (
    id         BIGINT       NOT NULL DEFAULT nextval('mlt_id_seq'),
    user_id    BIGINT       NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL,
    expires_at TIMESTAMPTZ  NOT NULL,
    used       BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    CONSTRAINT magic_link_tokens_user_id_unique UNIQUE (user_id),
    CONSTRAINT magic_link_tokens_token_hash_unique UNIQUE (token_hash),
    CONSTRAINT magic_link_tokens_user_fk FOREIGN KEY (user_id) REFERENCES users (id)
);
