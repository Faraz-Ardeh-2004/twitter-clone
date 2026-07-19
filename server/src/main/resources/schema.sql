-- ============================================================
-- Owner: AmirAli (Database)
-- ============================================================
-- Fill this file in gradually, phase by phase. Each section matches exactly
-- the phase it belongs to in the project spec. After each change, run it
-- against your local database (psql -f schema.sql), and in Report.md, under
-- Database Design, paste the final DDL.

-- ---------- Phase 0 (Day 1-2): a test table just to confirm connectivity ----------
-- TODO(AmirAli): you can drop this table after the initial test.
CREATE TABLE IF NOT EXISTS health_check (
    id SERIAL PRIMARY KEY,
    checked_at TIMESTAMP DEFAULT now()
);

-- ---------- Phase 1 (Day 3-5): authentication ----------
-- TODO(AmirAli): main users table.
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT now()
    -- TODO(AmirAli - profile/bonus phase): display_name, bio, avatar_url, banner_url
);

-- TODO(AmirAli): if you decide to store sessions in the database (instead of
-- an in-memory ConcurrentHashMap on the server), enable this table. Per the
-- spec's suggestion, the in-memory version is enough for 2 weeks, so this
-- table is optional.
-- CREATE TABLE IF NOT EXISTS sessions (
--     token VARCHAR(255) PRIMARY KEY,
--     user_id INT REFERENCES users(id),
--     expires_at TIMESTAMP
-- );

-- ---------- Phase 2 (Day 6-8): tweets and feed ----------
-- TODO(AmirAli): tweets table.
CREATE TABLE IF NOT EXISTS tweets (
    id SERIAL PRIMARY KEY,
    author_id INT NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT now()
    -- parent_tweet_id column is added in Phase 3 (see below)
);

-- TODO(AmirAli): index on created_at for feed speed (ORDER BY created_at DESC).
CREATE INDEX IF NOT EXISTS idx_tweets_created_at ON tweets (created_at DESC);

-- ---------- Phase 3 (Day 9-11): social graph and interactions ----------
-- TODO(AmirAli): add the reply column to tweets (nullable).
ALTER TABLE tweets ADD COLUMN IF NOT EXISTS parent_tweet_id INT REFERENCES tweets(id);

-- TODO(AmirAli): follow table (composite key).
CREATE TABLE IF NOT EXISTS follows (
    follower_id INT NOT NULL REFERENCES users(id),
    following_id INT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP DEFAULT now(),
    PRIMARY KEY (follower_id, following_id)
);

-- TODO(AmirAli): like table (composite key).
CREATE TABLE IF NOT EXISTS likes (
    user_id INT NOT NULL REFERENCES users(id),
    tweet_id INT NOT NULL REFERENCES tweets(id),
    created_at TIMESTAMP DEFAULT now(),
    PRIMARY KEY (user_id, tweet_id)
);

-- ---------- Phase 4 (Day 12-14): search ----------
-- TODO(AmirAli): for simple ILIKE search, a regular index doesn't help much;
-- if you have time, look into pg_trgm for faster search, otherwise no
-- special index is needed for the data volume in this student project.
-- CREATE EXTENSION IF NOT EXISTS pg_trgm;
-- CREATE INDEX IF NOT EXISTS idx_users_username_trgm ON users USING gin (username gin_trgm_ops);
