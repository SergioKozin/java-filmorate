CREATE TABLE IF NOT EXISTS users
(
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email     VARCHAR(255) NOT NULL UNIQUE,
    login     VARCHAR(255) NOT NULL UNIQUE,
    user_name VARCHAR(255) NOT NULL,
    birthday  DATE
);

CREATE TABLE IF NOT EXISTS mpa
(
    id       INT PRIMARY KEY,
    mpa_name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS films
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    film_name    VARCHAR(255) NOT NULL,
    description  VARCHAR(255) NOT NULL,
    release_date DATE,
    duration     BIGINT       NOT NULL,
    mpa          INT          NOT NULL REFERENCES mpa (id)
);

CREATE TABLE IF NOT EXISTS friendship
(
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id   BIGINT NOT NULL REFERENCES users (id),
    friend_id BIGINT NOT NULL REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS likes
(
    id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    film_id BIGINT NOT NULL REFERENCES films (id),
    user_id BIGINT NOT NULL REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS genres
(
    id         INT PRIMARY KEY,
    genre_name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS genres_of_film
(
    id       INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    film_id  BIGINT NOT NULL REFERENCES films (id),
    genre_id INT    NOT NULL REFERENCES genres (id)
);