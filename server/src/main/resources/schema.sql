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

-- ============================================================
-- Phase 5 (additive migration): profiles, media, hashtags, retweets,
-- notifications. Everything below is written to be idempotent and
-- non-destructive: it only ADDs to the tables defined above, so running the
-- whole file repeatedly is always safe and never drops existing data.
-- ============================================================

-- ---------- Profile fields on users ----------
ALTER TABLE users ADD COLUMN IF NOT EXISTS display_name VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS bio          VARCHAR(280);
ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_url   TEXT;   -- base64 data URI or URL
ALTER TABLE users ADD COLUMN IF NOT EXISTS banner_url   TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS verified     BOOLEAN NOT NULL DEFAULT FALSE;

-- ---------- Retweets/Reposts ----------
-- A retweet is a row in tweets whose retweet_of points at the original tweet.
-- content is empty for a plain repost, or holds the added text for a quote.
ALTER TABLE tweets ADD COLUMN IF NOT EXISTS retweet_of INT REFERENCES tweets(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_tweets_author  ON tweets (author_id);
CREATE INDEX IF NOT EXISTS idx_tweets_parent  ON tweets (parent_tweet_id);
CREATE INDEX IF NOT EXISTS idx_tweets_retweet ON tweets (retweet_of);

-- ---------- Media (images attached to a tweet) ----------
CREATE TABLE IF NOT EXISTS media (
                                     id       SERIAL PRIMARY KEY,
                                     tweet_id INT NOT NULL REFERENCES tweets(id) ON DELETE CASCADE,
    data     TEXT NOT NULL,          -- base64-encoded image bytes
    position INT  NOT NULL DEFAULT 0 -- ordering when a tweet has several images
    );
CREATE INDEX IF NOT EXISTS idx_media_tweet ON media (tweet_id);

-- ---------- Hashtags ----------
CREATE TABLE IF NOT EXISTS hashtags (
                                        id  SERIAL PRIMARY KEY,
                                        tag VARCHAR(140) NOT NULL  -- stored lower-cased, without the leading '#'
    );
CREATE UNIQUE INDEX IF NOT EXISTS idx_hashtags_lower_tag ON hashtags (LOWER(tag));
CREATE TABLE IF NOT EXISTS tweet_hashtags (
                                              tweet_id   INT NOT NULL REFERENCES tweets(id)   ON DELETE CASCADE,
    hashtag_id INT NOT NULL REFERENCES hashtags(id) ON DELETE CASCADE,
    PRIMARY KEY (tweet_id, hashtag_id)
    );
CREATE INDEX IF NOT EXISTS idx_tweet_hashtags_hashtag ON tweet_hashtags (hashtag_id);

-- ---------- Notifications ----------
-- type is one of: FOLLOW, LIKE, REPLY, RETWEET
CREATE TABLE IF NOT EXISTS notifications (
                                             id         SERIAL PRIMARY KEY,
                                             user_id    INT NOT NULL REFERENCES users(id) ON DELETE CASCADE, -- recipient
    actor_id   INT NOT NULL REFERENCES users(id) ON DELETE CASCADE, -- who triggered it
    type       VARCHAR(20) NOT NULL,
    tweet_id   INT REFERENCES tweets(id) ON DELETE CASCADE,          -- optional context
    is_read    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT now()
    );
CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications (user_id, created_at DESC);

-- Add ON DELETE CASCADE to the likes/follows FKs would require dropping the
-- original constraints; for a fresh database the definitions above already
-- cover everything, so we leave the Phase 1-3 tables as-is to stay additive.