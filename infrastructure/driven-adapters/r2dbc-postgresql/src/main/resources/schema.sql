CREATE TABLE IF NOT EXISTS bootcamps (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(200) NOT NULL,
    release_date DATE NOT NULL,
    duration INTEGER NOT NULL CHECK (duration > 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_bootcamps_name ON bootcamps(name);
CREATE INDEX IF NOT EXISTS idx_bootcamps_release_date ON bootcamps(release_date);
CREATE INDEX IF NOT EXISTS idx_bootcamps_deleted_at ON bootcamps(deleted_at);

CREATE TABLE IF NOT EXISTS bootcamp_capacities (
    id BIGSERIAL PRIMARY KEY,
    bootcamp_id BIGINT NOT NULL,
    capacity_id BIGINT NOT NULL,
    CONSTRAINT fk_bootcamp FOREIGN KEY (bootcamp_id)
        REFERENCES bootcamps(id) ON DELETE CASCADE,
    CONSTRAINT unique_bootcamp_capacity
        UNIQUE (bootcamp_id, capacity_id)
);

CREATE INDEX IF NOT EXISTS idx_bootcamp_capacities_bootcamp_id
    ON bootcamp_capacities(bootcamp_id);
CREATE INDEX IF NOT EXISTS idx_bootcamp_capacities_capacity_id
    ON bootcamp_capacities(capacity_id);
