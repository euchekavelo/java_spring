DROP TABLE IF EXISTS books;

CREATE TABLE books
(
    id       INT AUTO_INCREMENT PRIMARY KEY,
    author   VARCHAR(250) NOT NULL,
    title    VARCHAR(250) NOT NULL,
    priceOld DOUBLE DEFAULT 0.0,
    price    DOUBLE DEFAULT 0.0
);

create table authors
(
    id         INT,
    first_name VARCHAR(50),
    last_name  VARCHAR(50)
);