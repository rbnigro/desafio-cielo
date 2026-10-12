CREATE TABLE events (
    id CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    name VARCHAR(200) NOT NULL,
    starts_at DATETIME(6) NOT NULL,
    capacity INT NOT NULL,
    available_tickets INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_events_capacity_positive CHECK (capacity > 0),
    CONSTRAINT chk_events_available_range CHECK (available_tickets >= 0 AND available_tickets <= capacity)
) ENGINE=InnoDB;

CREATE TABLE reservations (
    id CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    event_id CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    request_hash CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_reservation_event_idempotency UNIQUE (event_id, idempotency_key),
    CONSTRAINT fk_reservations_event FOREIGN KEY (event_id) REFERENCES events (id),
    CONSTRAINT chk_reservations_quantity_positive CHECK (quantity > 0),
    CONSTRAINT chk_reservations_status CHECK (status IN ('PENDING', 'CANCELLED', 'EXPIRED')),
    INDEX ix_reservations_expiry (status, expires_at, event_id)
) ENGINE=InnoDB;
