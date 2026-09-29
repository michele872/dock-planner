CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE dock (
    id                 BIGSERIAL PRIMARY KEY,
    code               VARCHAR(20)   NOT NULL UNIQUE,
    name               VARCHAR(100)  NOT NULL,
    length_meters      NUMERIC(5, 2) NOT NULL CHECK (length_meters > 0),
    max_height_meters  NUMERIC(4, 2) NOT NULL CHECK (max_height_meters > 0),
    active             BOOLEAN       NOT NULL DEFAULT TRUE
);

CREATE TABLE vehicle (
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(100)  NOT NULL,
    -- Italian plate: 2 letters, 3 digits, 2 letters; I, O, Q and U are never used.
    plate_number   VARCHAR(7)    NOT NULL UNIQUE
                   CHECK (plate_number ~ '^[A-HJ-NPR-TV-Z]{2}[0-9]{3}[A-HJ-NPR-TV-Z]{2}$'),
    type           VARCHAR(20)   NOT NULL,
    length_meters  NUMERIC(5, 2) NOT NULL CHECK (length_meters > 0),
    height_meters  NUMERIC(4, 2) NOT NULL CHECK (height_meters > 0)
);

CREATE TABLE booking (
    id          BIGSERIAL PRIMARY KEY,
    dock_id     BIGINT      NOT NULL REFERENCES dock (id),
    vehicle_id  BIGINT      NOT NULL REFERENCES vehicle (id),
    arrival     TIMESTAMPTZ NOT NULL,
    departure   TIMESTAMPTZ NOT NULL,
    status      VARCHAR(20) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    version     BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT booking_departure_after_arrival CHECK (departure > arrival),
    -- Safety net under the application checks: two confirmed bookings can never
    -- overlap on the same dock, even with concurrent requests.
    CONSTRAINT booking_no_overlap_per_dock EXCLUDE USING gist (
        dock_id WITH =,
        tstzrange(arrival, departure) WITH &&
    ) WHERE (status = 'CONFIRMED'),
    -- A vehicle cannot be at two docks at the same time.
    CONSTRAINT booking_no_overlap_per_vehicle EXCLUDE USING gist (
        vehicle_id WITH =,
        tstzrange(arrival, departure) WITH &&
    ) WHERE (status = 'CONFIRMED')
);

CREATE INDEX booking_dock_arrival_idx ON booking (dock_id, arrival);
