DO $$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'svc_media') THEN
      CREATE ROLE svc_media WITH LOGIN PASSWORD 'svc_media';
   END IF;
END
$$;

DO $$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'filesdb') THEN
      CREATE DATABASE filesdb OWNER svc_media;
   END IF;
END
$$;

GRANT ALL PRIVILEGES ON DATABASE filesdb TO svc_media;

\connect filesdb

CREATE TABLE IF NOT EXISTS files (
    id SERIAL PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    size BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO files (filename, size) VALUES ('test.txt', 1234)
ON CONFLICT DO NOTHING;

INSERT INTO files (filename, size) VALUES ('photo.png', 4567)
ON CONFLICT DO NOTHING;

