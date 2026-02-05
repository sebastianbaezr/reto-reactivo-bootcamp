CREATE TABLE IF NOT EXISTS technologies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(90) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_technologies_name ON technologies(name);
CREATE INDEX IF NOT EXISTS idx_technologies_deleted_at ON technologies(deleted_at);
