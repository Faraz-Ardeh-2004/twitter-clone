# 🐦 Twitter Clone — Advanced Programming Final Project

A desktop social-network application inspired by **X (Twitter)**, built with a
multi-threaded **client–server architecture** over Java sockets, a **JSON
protocol** (Gson), **JavaFX** for the UI, and **PostgreSQL** (JDBC + BCrypt) for
storage. Tweets, likes, follows, replies, retweets, and notifications are
delivered to connected clients **in real time**.

---

## Table of Contents

1. [Description](#description)
2. [Features](#features)
3. [Architecture at a glance](#architecture-at-a-glance)
4. [Tech stack](#tech-stack)
5. [Project structure](#project-structure)
6. [Getting started](#getting-started)
   - [Prerequisites](#prerequisites)
   - [Database setup](#database-setup)
   - [Build](#build)
   - [Run the server](#run-the-server)
   - [Run the client](#run-the-client)
7. [Usage guide](#usage-guide)
8. [Demo / Visuals](#demo--visuals)
9. [Configuration](#configuration)
10. [Credits](#credits)
11. [Changelog](#changelog)
12. [Contact](#contact)

---

## Description

This project is a fully functional social-media platform where users can
register, publish tweets (with images and hashtags), follow each other, like and
reply to tweets, retweet, search, and receive notifications. A central server
manages all state in PostgreSQL and pushes updates to connected clients so the
feed, likes, follows, and notifications update **without a manual refresh**.

The goal was to apply object-oriented design, socket programming, multithreading,
JDBC with parameterized queries, and JavaFX in a realistic, portfolio-quality
application. See [`Report.md`](Report.md) for the full architecture, ERD, class
design, and AI-usage disclosure.

## Features

**Mandatory features (all implemented):**

- **Accounts & auth** — registration (unique username/email), login/logout,
  in-memory session tokens, **BCrypt** password hashing, server-side validation.
- **Profiles** — display name, bio, avatar & banner images, profile viewing and
  editing, and follower/following/tweet **statistics**.
- **Tweets** — create, view, delete (with its reply thread), timestamps, tweet
  detail pages, and per-user tweet history.
- **Interactions** — replies with **threaded conversations**, and
  **retweets/reposts**.
- **Hashtags** — automatic detection on post, plus hashtag search.
- **Media** — attach up to 4 images per tweet, thumbnails in the timeline, and
  full-size viewing on click.
- **Social graph** — follow / unfollow, followers & following lists, and counts.
- **Likes** — like / unlike with live like counts.
- **Feed** — a chronological **personalized timeline** (your tweets + people you
  follow) with **real-time delivery** of new tweets, plus a global "Explore"
  feed.
- **Search** — by username/display name, by tweet text, and by hashtag; open
  profiles directly from results.

**Bonus features implemented:**

- 🔔 **Real-time notifications** (follow / like / reply / retweet) with a live
  nav badge, pushed over the socket.
- ♻️ Real-time **like** and **follow** propagation to other clients.
- 🌙 **Dark mode** toggle (full theme, light & dark).
- ✍️ **Character-limit enforcement** (280) with a live counter, validated on both
  client and server.
- 🖼️ **Image attachments**, thumbnails, and **full-size image viewing**.
- 🔁 **Retweets** with a "retweeted by" attribution in the feed.
- 🔥 **Trending hashtags** and **suggested users** queries.
- 📱 **Responsive UI** that adapts to window size.

## Architecture at a glance

```
┌──────────────┐   JSON packets over TCP sockets   ┌──────────────────────────┐
│ JavaFX Client│  ───────────────────────────────► │  Server (thread pool)    │
│ (per user)   │  ◄─────── real-time pushes ─────── │  ClientHandler           │
│              │                                    │   → Dispatcher (+auth)   │
│ NetworkService│                                   │     → Handlers (logic)   │
│ (bg listener)│                                    │       → DAOs (JDBC)      │
└──────────────┘                                    └───────────┬──────────────┘
                                                                │ HikariCP pool
                                                          ┌─────▼──────┐
                                                          │ PostgreSQL │
                                                          └────────────┘
```

Each connected client gets its own thread from a fixed pool. A
`ConnectionRegistry` maps online users to their socket so the server can push
`NEW_TWEET_PUSH`, `LIKE_PUSH`, `FOLLOW_PUSH`, and `NOTIFICATION_PUSH` messages in
real time. Full details in [`Report.md`](Report.md).

## Tech stack

| Layer | Technology |
|-------|------------|
| Language | Java 17+ (built/tested on Java 21) |
| UI | JavaFX 21 (FXML + programmatic, CSS theming) |
| Networking | Java `Socket` / `ServerSocket`, fixed thread pool |
| Protocol | JSON over a line-delimited socket stream (Gson 2.10) |
| Database | PostgreSQL 16 via JDBC |
| Connection pool | HikariCP |
| Password hashing | jBCrypt |
| Build | Maven (multi-module) |

## Project structure

```
twitter-clone/
├── shared/   models + JSON protocol contract (Packet, PacketType, User, Tweet,
│             Notification, HashtagParser) — shared by client and server
├── server/   sockets, thread pool, dispatcher, handlers, DAOs, schema.sql
├── client/   JavaFX client: network layer, controllers, programmatic UI, CSS
├── README.md
└── Report.md
```

## Getting started

### Prerequisites

- **JDK 17+** (Java 21 recommended)
- **Maven 3.8+**
- **PostgreSQL 14+** running locally

### Database setup

Create a database and a role (the defaults the server expects):

```bash
createdb twitter_clone
psql -d twitter_clone -c "CREATE ROLE phipsitheta LOGIN PASSWORD 'phipsitheta' SUPERUSER;"
```

You **do not** need to run the schema manually — the server applies
[`server/src/main/resources/schema.sql`](server/src/main/resources/schema.sql)
automatically on startup (it is idempotent). To run it by hand anyway:

```bash
psql -U phipsitheta -d twitter_clone -f server/src/main/resources/schema.sql
```

### Build

```bash
mvn clean install
```

### Run the server

```bash
# uses the defaults below unless overridden by environment variables
java -jar server/target/server-1.0-SNAPSHOT.jar
```

The server listens on **port 8080** and prints `Server is listening ...` once
ready.

### Run the client

Launch one or more clients (open several to see real-time updates between users):

```bash
mvn -pl client javafx:run
```

## Usage guide

1. **Register** a new account (unique username, valid email, 6+ char password),
   or **log in**.
2. **Compose a tweet** from the Home page — type up to 280 characters, add
   `#hashtags`, and optionally attach images. Watch the character counter.
3. **Interact** — click ♥ to like, ↻ to retweet, 💬 to reply. Click a tweet to
   open its **detail + thread**.
4. **Follow people** — open a profile (click any avatar/name or use Search) and
   press **Follow**. Their tweets now appear in your **Following** feed in real
   time.
5. **Edit your profile** — go to Profile → *Edit profile* to set a display name,
   bio, avatar, and banner.
6. **Search** — find people, tweets, or `#hashtags`; the Search page also shows
   **trending hashtags**.
7. **Notifications** — the 🔔 nav item shows a live unread badge for new
   follows, likes, replies, and retweets.
8. **Dark mode** — toggle from the left navigation at any time.

> **Tip:** log in as two different users in two client windows and like/follow
> from one — the other updates instantly.

## Demo / Visuals

> _Add screenshots or a short GIF here for your submission._ Suggested captures:
>
> - Login / registration screen (light **and** dark mode)
> - Home feed with a tweet containing an image and hashtags
> - A profile page with banner, avatar, bio, and stats
> - Two windows side-by-side showing a real-time like/follow update
> - The notifications page with the unread badge

Place images under a `docs/` folder and reference them, e.g.
`![Home feed](docs/home.png)`.

## Configuration

The server reads database settings from environment variables (with sensible
local defaults):

| Variable | Default | Purpose |
|----------|---------|---------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/twitter_clone` | JDBC URL |
| `DB_USER` | `phipsitheta` | database user |
| `DB_PASSWORD` | *(empty)* | database password |

The client connects to `localhost:8080` (see `ClientMain`). Change the host/port
constants there to connect to a remote server.

## Credits

- **Course:** Advanced Programming, Summer 2026 — Dr. Saeed Reza Kheradpisheh
- **Team roles:** Frontend (JavaFX), Backend (sockets/logic), Database (schema/JDBC)
- **Libraries:** [Gson](https://github.com/google/gson),
  [HikariCP](https://github.com/brettwooldridge/HikariCP),
  [jBCrypt](https://www.mindrot.org/projects/jBCrypt/),
  [PostgreSQL JDBC](https://jdbc.postgresql.org/),
  [OpenJFX](https://openjfx.io/)
- AI assistance is disclosed in [`Report.md`](Report.md#ai-usage-disclosure).

## Changelog

- **v1.0** — Full release. Auth, profiles (avatar/banner/bio/stats), tweets,
  replies/threads, retweets, likes, follows, hashtags, image media, search,
  trending, notifications, real-time push, dark mode, character limit, runnable
  server jar, auto-initialized schema.
- **v0.1** — Initial scaffold (modules, protocol contract, stubbed classes).

## Contact

For questions about this project, contact the team through the course channel or
the repository's issue tracker.
