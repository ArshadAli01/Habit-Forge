-- User table with proper constraints
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL CHECK (LENGTH(TRIM(name)) BETWEEN 2 AND 100),
    email VARCHAR(255) NOT NULL UNIQUE CHECK (email ~* '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$'),
    password VARCHAR(255) NOT NULL,  -- BCrypt produces 60-char hashes; validation happens in service
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(50) DEFAULT 'system',
    updated_by VARCHAR(50) DEFAULT 'system'
);

-- Index for faster email lookups
CREATE INDEX idx_users_email ON users(email);