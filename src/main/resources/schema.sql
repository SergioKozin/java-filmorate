CREATE TABLE IF NOT EXISTS users
(
    user_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email     VARCHAR(255) NOT NULL UNIQUE,
    login     VARCHAR(255) NOT NULL UNIQUE,
    user_name VARCHAR(255) NOT NULL,
    birthday  DATE
);

CREATE TABLE IF NOT EXISTS mpa
(
    mpa_id   INT PRIMARY KEY,
    mpa_name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS films
(
    film_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    film_name    VARCHAR(255) NOT NULL,
    description  VARCHAR(255) NOT NULL,
    release_date DATE,
    duration     BIGINT       NOT NULL,
    mpa          INT          NOT NULL REFERENCES mpa (mpa_id)
);

CREATE TABLE IF NOT EXISTS friendship
(
    friendship_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id       BIGINT NOT NULL REFERENCES users (user_id),
    friend_id     BIGINT NOT NULL REFERENCES users (user_id)
);

CREATE TABLE IF NOT EXISTS likes
(
    like_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    film_id BIGINT NOT NULL REFERENCES films (film_id),
    user_id BIGINT NOT NULL REFERENCES users (user_id)
);

CREATE TABLE IF NOT EXISTS genres
(
    genre_id   INT PRIMARY KEY,
    genre_name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS genres_of_film
(
    id       INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    film_id  BIGINT NOT NULL REFERENCES films (film_id),
    genre_id INT    NOT NULL REFERENCES genres (genre_id)
);