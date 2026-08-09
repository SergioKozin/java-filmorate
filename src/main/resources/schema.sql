CREATE TABLE IF NOT EXISTS users
(
    user_id   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email     VARCHAR(255) NOT NULL,
    login     VARCHAR(255) NOT NULL,
    user_name VARCHAR(255) NOT NULL,
    birthday  DATE
);

CREATE TABLE IF NOT EXISTS films
(
    film_id      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    film_name    VARCHAR(255) NOT NULL,
    description  VARCHAR(255) NOT NULL,
    release_date DATE,
    duration     INT          NOT NULL,
    mpa          INT          NOT NULL REFERENCES mpa (mpa_id)
);

CREATE TABLE IF NOT EXISTS friends
(
    id        INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id   INT NOT NULL REFERENCES users (user_id),
    friend_id INT NOT NULL REFERENCES users (user_id)
);

CREATE TABLE IF NOT EXISTS likes
(
    like_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    film_id INT NOT NULL REFERENCES films (film_id),
    user_id INT NOT NULL REFERENCES users (user_id)
);

CREATE TABLE IF NOT EXISTS genre
(
    id   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    genre_name VARCHAR(255) NOT NULL,
    film_id INT NOT NULL REFERENCES films (film_id)
);

CREATE TABLE IF NOT EXISTS mpa
(
    mpa_id   INT
        CONSTRAINT IDENTITY PRIMARY KEY,
    mpa_name VARCHAR(255) NOT NULL
);