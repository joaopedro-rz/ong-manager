-- ============= USERS / AUTH =============
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(30),
    profile_image_url VARCHAR(500),
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_users_email ON users(email);

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(40) NOT NULL UNIQUE
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

INSERT INTO roles (name) VALUES ('ADMIN'),('ONG_MANAGER'),('DONOR'),('VOLUNTEER');

CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE email_verification_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============= NGO =============
CREATE TABLE ngo_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

INSERT INTO ngo_categories (name) VALUES
('Educacao'),('Saude'),('Meio Ambiente'),('Assistencia Social'),
('Direitos Humanos'),('Animais'),('Cultura'),('Esporte');

CREATE TABLE addresses (
    id BIGSERIAL PRIMARY KEY,
    street VARCHAR(200),
    number VARCHAR(20),
    complement VARCHAR(100),
    district VARCHAR(100),
    city VARCHAR(100),
    state VARCHAR(2),
    zip_code VARCHAR(15),
    country VARCHAR(60) DEFAULT 'Brasil'
);

CREATE TABLE ngos (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    cnpj VARCHAR(20) NOT NULL UNIQUE,
    description TEXT,
    phone VARCHAR(30),
    website VARCHAR(255),
    social_media VARCHAR(500),
    logo_url VARCHAR(500),
    certifications TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    address_id BIGINT REFERENCES addresses(id),
    category_id BIGINT REFERENCES ngo_categories(id),
    manager_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_ngos_status ON ngos(status);
CREATE INDEX idx_ngos_category ON ngos(category_id);

-- ============= CAMPAIGNS =============
CREATE TABLE campaigns (
    id BIGSERIAL PRIMARY KEY,
    ngo_id BIGINT NOT NULL REFERENCES ngos(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    financial_goal NUMERIC(14,2),
    raised_amount NUMERIC(14,2) NOT NULL DEFAULT 0,
    start_date DATE NOT NULL,
    end_date DATE,
    cover_image_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    urgent BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_campaigns_status ON campaigns(status);
CREATE INDEX idx_campaigns_ngo ON campaigns(ngo_id);

CREATE TABLE campaign_items (
    id BIGSERIAL PRIMARY KEY,
    campaign_id BIGINT NOT NULL REFERENCES campaigns(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(80),
    quantity_needed INT NOT NULL,
    quantity_received INT NOT NULL DEFAULT 0,
    unit VARCHAR(30) NOT NULL DEFAULT 'un'
);

CREATE TABLE campaign_updates (
    id BIGSERIAL PRIMARY KEY,
    campaign_id BIGINT NOT NULL REFERENCES campaigns(id) ON DELETE CASCADE,
    title VARCHAR(180) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============= DONATIONS =============
CREATE TABLE donations (
    id BIGSERIAL PRIMARY KEY,
    donor_id BIGINT NOT NULL REFERENCES users(id),
    campaign_id BIGINT NOT NULL REFERENCES campaigns(id),
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    donation_date TIMESTAMP NOT NULL DEFAULT NOW(),
    -- financial
    amount NUMERIC(14,2),
    payment_method VARCHAR(40),
    receipt_url VARCHAR(500),
    -- material
    item_name VARCHAR(150),
    item_quantity INT,
    item_unit VARCHAR(30),
    delivery_date DATE,
    confirmed_at TIMESTAMP,
    notes TEXT
);
CREATE INDEX idx_donations_donor ON donations(donor_id);
CREATE INDEX idx_donations_campaign ON donations(campaign_id);
CREATE INDEX idx_donations_status ON donations(status);

-- ============= VOLUNTEERS =============
CREATE TABLE skills (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE volunteer_opportunities (
    id BIGSERIAL PRIMARY KEY,
    ngo_id BIGINT NOT NULL REFERENCES ngos(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    slots INT NOT NULL DEFAULT 1,
    workload_hours INT,
    start_date DATE,
    end_date DATE,
    required_skills TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE volunteer_applications (
    id BIGSERIAL PRIMARY KEY,
    opportunity_id BIGINT NOT NULL REFERENCES volunteer_opportunities(id) ON DELETE CASCADE,
    volunteer_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    motivation TEXT,
    applied_at TIMESTAMP NOT NULL DEFAULT NOW(),
    reviewed_at TIMESTAMP,
    UNIQUE (opportunity_id, volunteer_id)
);

CREATE TABLE volunteer_schedule (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES volunteer_applications(id) ON DELETE CASCADE,
    scheduled_at TIMESTAMP NOT NULL,
    duration_hours INT NOT NULL,
    notes VARCHAR(255)
);
