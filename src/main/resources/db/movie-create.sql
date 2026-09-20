CREATE TABLE movies (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    genre VARCHAR(100),
    language VARCHAR(100),
    release_date DATE,
    duration INTEGER,
    rating DOUBLE PRECISION,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

drop table movies;
-- ============================================================
-- Movie Reservation App - Database Schema (PostgreSQL)
-- ============================================================

-- ---------- ENUM TYPES ----------
CREATE TYPE seat_type AS ENUM ('REGULAR', 'PREMIUM', 'RECLINER');
CREATE TYPE seat_status AS ENUM ('AVAILABLE', 'LOCKED', 'BOOKED');
CREATE TYPE booking_status AS ENUM ('PENDING', 'CONFIRMED', 'CANCELLED', 'EXPIRED');
CREATE TYPE payment_status AS ENUM ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED');

-- ---------- MOVIE ----------
CREATE TABLE movies (
    id               BIGSERIAL PRIMARY KEY,
    title            VARCHAR(255) NOT NULL,
    genre            VARCHAR(100),
    language         VARCHAR(50),
    duration_minutes INTEGER,
    rating           VARCHAR(10),
    poster_url       VARCHAR(500),
    release_date     DATE,
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_movies_title ON movies (title);
CREATE INDEX idx_movies_genre ON movies (genre);

-- ---------- THEATER ----------
CREATE TABLE theaters (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(255) NOT NULL,
    city    VARCHAR(100) NOT NULL,
    address VARCHAR(500)
);

CREATE INDEX idx_theaters_city ON theaters (city);

-- ---------- SCREEN ----------
CREATE TABLE screens (
    id            BIGSERIAL PRIMARY KEY,
    theater_id    BIGINT NOT NULL REFERENCES theaters (id) ON DELETE CASCADE,
    screen_number INTEGER NOT NULL,
    CONSTRAINT uq_screen_theater_number UNIQUE (theater_id, screen_number)
);

CREATE INDEX idx_screens_theater_id ON screens (theater_id);

-- ---------- SEAT ----------
CREATE TABLE seats (
    id          BIGSERIAL PRIMARY KEY,
    screen_id   BIGINT NOT NULL REFERENCES screens (id) ON DELETE CASCADE,
    seat_number VARCHAR(10) NOT NULL,
    seat_row    VARCHAR(5) NOT NULL,
    seat_type   seat_type NOT NULL DEFAULT 'REGULAR',
    CONSTRAINT uq_seat_screen_number UNIQUE (screen_id, seat_number)
);

CREATE INDEX idx_seats_screen_id ON seats (screen_id);

-- ---------- SHOWTIME ----------
CREATE TABLE showtimes (
    id         BIGSERIAL PRIMARY KEY,
    movie_id   BIGINT NOT NULL REFERENCES movies (id) ON DELETE CASCADE,
    screen_id  BIGINT NOT NULL REFERENCES screens (id) ON DELETE CASCADE,
    start_time TIMESTAMP NOT NULL,
    end_time   TIMESTAMP NOT NULL,
    base_price NUMERIC(10, 2) NOT NULL,
    CONSTRAINT chk_showtime_times CHECK (end_time > start_time)
);

-- speeds up "showtimes for a movie on a given date" and seat-map lookups
CREATE INDEX idx_showtimes_movie_id ON showtimes (movie_id);
CREATE INDEX idx_showtimes_screen_id ON showtimes (screen_id);
CREATE INDEX idx_showtimes_start_time ON showtimes (start_time);

-- prevents double-booking a screen for overlapping times at the DB level
-- (requires btree_gist extension)
CREATE EXTENSION IF NOT EXISTS btree_gist;
ALTER TABLE showtimes
    ADD CONSTRAINT excl_screen_time_overlap
    EXCLUDE USING gist (
        screen_id WITH =,
        tsrange(start_time, end_time) WITH &&
    );

-- ---------- SHOWTIME_SEAT (the contended, lockable unit) ----------
CREATE TABLE showtime_seats (
    id                BIGSERIAL PRIMARY KEY,
    showtime_id       BIGINT NOT NULL REFERENCES showtimes (id) ON DELETE CASCADE,
    seat_id           BIGINT NOT NULL REFERENCES seats (id) ON DELETE CASCADE,
    status            seat_status NOT NULL DEFAULT 'AVAILABLE',
    locked_by_user_id VARCHAR(255),
    lock_expires_at   TIMESTAMP,
    price             NUMERIC(10, 2) NOT NULL,
    version           BIGINT NOT NULL DEFAULT 0,  -- optimistic locking column (@Version)
    CONSTRAINT uq_showtime_seat UNIQUE (showtime_id, seat_id)
);

CREATE INDEX idx_showtime_seats_showtime_id ON showtime_seats (showtime_id);

-- speeds up the sweep job's "find expired locks" query
CREATE INDEX idx_showtime_seats_status_expiry
    ON showtime_seats (status, lock_expires_at)
    WHERE status = 'LOCKED';

-- ---------- BOOKING ----------
CREATE TABLE bookings (
    id           BIGSERIAL PRIMARY KEY,
    user_id      VARCHAR(255) NOT NULL,           -- from JWT claim, no FK (owned by auth server)
    showtime_id  BIGINT NOT NULL REFERENCES showtimes (id) ON DELETE RESTRICT,
    status       booking_status NOT NULL DEFAULT 'PENDING',
    total_amount NUMERIC(10, 2) NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT now(),
    expires_at   TIMESTAMP
);

CREATE INDEX idx_bookings_user_id ON bookings (user_id);
CREATE INDEX idx_bookings_showtime_id ON bookings (showtime_id);

-- speeds up the sweep job's "find expired pending bookings" query
CREATE INDEX idx_bookings_status_expiry
    ON bookings (status, expires_at)
    WHERE status = 'PENDING';

-- ---------- BOOKING_SEAT (join table with extra fields) ----------
CREATE TABLE booking_seats (
    id                BIGSERIAL PRIMARY KEY,
    booking_id        BIGINT NOT NULL REFERENCES bookings (id) ON DELETE CASCADE,
    showtime_seat_id  BIGINT NOT NULL REFERENCES showtime_seats (id) ON DELETE RESTRICT,
    price_at_booking  NUMERIC(10, 2) NOT NULL,
    CONSTRAINT uq_booking_showtime_seat UNIQUE (showtime_seat_id)
    -- a given showtime_seat can only ever belong to ONE active booking_seat row
    -- (enforced further at the application layer via status checks, since a
    -- cancelled booking's seat should become bookable again by a new booking_seat row --
    -- if you need full history, drop this unique constraint and rely on status instead)
);

CREATE INDEX idx_booking_seats_booking_id ON booking_seats (booking_id);

-- ---------- PAYMENT ----------
CREATE TABLE payments (
    id             BIGSERIAL PRIMARY KEY,
    booking_id     BIGINT NOT NULL REFERENCES bookings (id) ON DELETE CASCADE,
    amount         NUMERIC(10, 2) NOT NULL,
    status         payment_status NOT NULL DEFAULT 'PENDING',
    provider       VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(255) NOT NULL,
    created_at     TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_payment_transaction_id UNIQUE (transaction_id)
    -- enforces webhook idempotency at the DB level too
);

CREATE INDEX idx_payments_booking_id ON payments (booking_id);


alter table movies drop description;
alter table movies RENAME duration to duration_minutes;

alter table movies add poster_url varchar(500);

ALTER TABLE movies
ALTER COLUMN created_at SET DEFAULT NOW(),
ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE movies
ALTER COLUMN updated_at SET DEFAULT NOW(),
ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE movies
ALTER COLUMN rating TYPE VARCHAR(10);

alter table Payments add column provider_order_id varchar(255);