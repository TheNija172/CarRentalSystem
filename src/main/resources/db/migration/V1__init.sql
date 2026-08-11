CREATE TABLE roles
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
);


CREATE TABLE users
(
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    first_name VARCHAR(50)  NOT NULL,
    last_name  VARCHAR(50)  NOT NULL,
    phone      VARCHAR(20),
    role_id    BIGINT       NOT NULL,
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_users_role
        FOREIGN KEY (role_id)
            REFERENCES roles (id)
);


CREATE TABLE car_categories
(
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(50)    NOT NULL UNIQUE,
    description   VARCHAR(255),
    price_per_day NUMERIC(10, 2) NOT NULL,
    created_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_car_categories_price
        CHECK (price_per_day > 0)
);


CREATE TABLE cars
(
    id              BIGSERIAL PRIMARY KEY,
    brand           VARCHAR(50) NOT NULL,
    model           VARCHAR(50) NOT NULL,
    production_year INTEGER     NOT NULL,
    license_plate   VARCHAR(20) NOT NULL UNIQUE,
    color           VARCHAR(30),
    transmission    VARCHAR(20) NOT NULL,
    fuel_type       VARCHAR(20) NOT NULL,
    seats           INTEGER     NOT NULL,
    category_id     BIGINT      NOT NULL,
    active          BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_cars_category
        FOREIGN KEY (category_id)
            REFERENCES car_categories (id),

    CONSTRAINT chk_cars_production_year
        CHECK (production_year >= 1900),

    CONSTRAINT chk_cars_seats
        CHECK (seats > 0)
);


CREATE TABLE rental_locations
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    address     VARCHAR(255) NOT NULL,
    city        VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE bookings
(
    id                 BIGSERIAL PRIMARY KEY,
    user_id            BIGINT         NOT NULL,
    car_id             BIGINT         NOT NULL,
    pickup_location_id BIGINT         NOT NULL,
    return_location_id BIGINT         NOT NULL,
    start_date         DATE           NOT NULL,
    end_date           DATE           NOT NULL,
    total_price        NUMERIC(10, 2) NOT NULL,
    status             VARCHAR(20)    NOT NULL,
    created_at         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_bookings_user
        FOREIGN KEY (user_id)
            REFERENCES users (id),

    CONSTRAINT fk_bookings_car
        FOREIGN KEY (car_id)
            REFERENCES cars (id),

    CONSTRAINT fk_bookings_pickup_location
        FOREIGN KEY (pickup_location_id)
            REFERENCES rental_locations (id),

    CONSTRAINT fk_bookings_return_location
        FOREIGN KEY (return_location_id)
            REFERENCES rental_locations (id),

    CONSTRAINT chk_bookings_dates
        CHECK (end_date > start_date),

    CONSTRAINT chk_bookings_total_price
        CHECK (total_price > 0),

    CONSTRAINT chk_bookings_status
        CHECK (status IN (
                          'PENDING',
                          'CONFIRMED',
                          'CANCELLED',
                          'COMPLETED'
            ))
);


CREATE TABLE payments
(
    id             BIGSERIAL PRIMARY KEY,
    booking_id     BIGINT         NOT NULL UNIQUE,
    amount         NUMERIC(10, 2) NOT NULL,
    status         VARCHAR(20)    NOT NULL,
    payment_method VARCHAR(20)    NOT NULL,
    transaction_id VARCHAR(100),
    paid_at        TIMESTAMP,
    created_at     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payments_booking
        FOREIGN KEY (booking_id)
            REFERENCES bookings (id),

    CONSTRAINT chk_payments_amount
        CHECK (amount > 0),

    CONSTRAINT chk_payments_status
        CHECK (status IN (
                          'PENDING',
                          'COMPLETED',
                          'FAILED',
                          'REFUNDED'
            )),

    CONSTRAINT chk_payments_method
        CHECK (payment_method IN (
                                  'CARD',
                                  'CASH'
            ))
);


CREATE TABLE reviews
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT    NOT NULL,
    car_id     BIGINT    NOT NULL,
    booking_id BIGINT    NOT NULL UNIQUE,
    rating     INTEGER   NOT NULL,
    comment    VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reviews_user
        FOREIGN KEY (user_id)
            REFERENCES users (id),

    CONSTRAINT fk_reviews_car
        FOREIGN KEY (car_id)
            REFERENCES cars (id),

    CONSTRAINT fk_reviews_booking
        FOREIGN KEY (booking_id)
            REFERENCES bookings (id),

    CONSTRAINT chk_reviews_rating
        CHECK (rating BETWEEN 1 AND 5)
);


CREATE INDEX idx_users_role_id
    ON users (role_id);

CREATE INDEX idx_cars_category_id
    ON cars (category_id);

CREATE INDEX idx_bookings_user_id
    ON bookings (user_id);

CREATE INDEX idx_bookings_car_id
    ON bookings (car_id);

CREATE INDEX idx_bookings_pickup_location_id
    ON bookings (pickup_location_id);

CREATE INDEX idx_bookings_return_location_id
    ON bookings (return_location_id);

CREATE INDEX idx_bookings_dates
    ON bookings (start_date, end_date);

CREATE INDEX idx_bookings_status
    ON bookings (status);

CREATE INDEX idx_payments_status
    ON payments (status);

CREATE INDEX idx_reviews_user_id
    ON reviews (user_id);

CREATE INDEX idx_reviews_car_id
    ON reviews (car_id);



INSERT INTO roles (name)
VALUES
    ('USER'),
    ('ADMIN');