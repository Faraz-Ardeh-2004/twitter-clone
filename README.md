# Twitter Clone

A desktop social network inspired by **X (Twitter)** — a multi-threaded
client–server application written in **Java**, talking over raw **sockets** with
a **JSON protocol**, backed by **PostgreSQL**, and presented through a themed
**JavaFX** interface. New tweets, likes, follows, and notifications are pushed to
connected clients **in real time**.

> **Why this project?** It's the Advanced Programming final project: a single,
> realistic system that exercises everything the course covers — object-oriented
> design, concurrent socket servers, a hand-written JSON protocol, JDBC with
> parameterized queries, and GUI development — instead of a set of disconnected
> exercises. The result is portfolio-quality and interview-ready.

---

## Table of Contents

- [Description](#description)
- [Features](#features)
- [How it works](#how-it-works)
- [Usage](#usage)
  - [Prerequisites](#prerequisites)
  - [1. Set up the database](#1-set-up-the-database)
  - [2. Build](#2-build)
  - [3. Run the server](#3-run-the-server)
  - [4. Run the client(s)](#4-run-the-clients)
  - [Configuration](#configuration)
- [Demo / Visuals](#demo--visuals)
- [Credits](#credits)
- [Changelog](#changelog)
- [Contact](#contact)

---

## Description

**What it does.** Users register and log in, publish tweets (with images and
`#hashtags`), follow one another, like/reply/retweet, browse a personalized
timeline, search people and content, and receive notifications. A central server
owns all state in PostgreSQL; clients never touch the database directly — they
exchange JSON messages with the server over a socket.

**How, in one line.** Each client keeps one socket open; the server assigns every
connection a thread from a pool, routes each JSON request through a dispatcher to
the right handler, persists via DAOs, and pushes live updates back to the
relevant online clients.

The codebase is split into three Maven modules so the wire contract can never
drift between the two sides:

```
twitter-clone/
├── shared/   models + JSON protocol (Packet, PacketType, User, Tweet, …)
├── server/   sockets, thread pool, dispatcher, handlers, DAOs, schema.sql
└── client/   JavaFX app: network layer, controllers, UI components, CSS, icons
```

## Features

**Core**

- **Accounts** — registration with unique username/email, login/logout, session
  tokens, **BCrypt**-hashed passwords, server-side validation.
- **Profiles** — display name, bio, avatar & banner images, editing, and
  follower / following / tweet **stats**.
- **Tweets** — create, view, delete (with the whole reply thread), timestamps,
  per-user history, and a full detail page.
- **Interactions** — **threaded replies** and **retweets / reposts**.
- **Social graph** — follow / unfollow with follower & following lists.
- **Likes** — like / unlike with live counts.
- **Feed** — a chronological **personalized timeline** (you + who you follow)
  with **real-time tweet delivery**, plus a global *Explore* feed.
- **Hashtags** — auto-detected from text, clickable, and searchable. Tags are
  matched case-insensitively but displayed using their most common casing.
- **Media** — attach up to 4 images per tweet, thumbnails in the timeline, and
  full-size viewing.
- **Search** — people (username / display name), tweet text, and hashtags.

**Bonus**

-  **Real-time notifications** (follow / like / reply / retweet) with a live
  unread badge.
-  Real-time **like** and **follow** propagation across clients.
-  **Dark mode** — a full light/dark theme toggle.
-  **280-character limit** with a live counter, enforced on client *and* server.
-  **Trending hashtags** and suggested users.
-  Crisp, theme-aware **Lucide vector icons** throughout the UI.
-  **Responsive** layout that adapts to window size.

## How it works

```
┌───────────────┐   JSON packets over TCP    ┌──────────────────────────────┐
│ JavaFX Client │ ─────── request ─────────► │ Server (fixed thread pool)   │
│  (one/user)   │ ◄────── response ────────  │  ClientHandler → Dispatcher   │
│ NetworkService│ ◄─── real-time push ─────  │   → Handler → DAO (JDBC)      │
└───────────────┘                            └──────────────┬───────────────┘
                                                            │ HikariCP pool
                                                     ┌──────▼──────┐
                                                     │ PostgreSQL  │
                                                     └─────────────┘
```

Every message is one line of JSON (a `Packet`) carrying a `type`, an optional
session `token`, a `requestId` for matching responses, and a `payload`. A
`ConnectionRegistry` maps online users to their sockets so the server can deliver
`NEW_TWEET_PUSH`, `LIKE_PUSH`, `FOLLOW_PUSH`, and `NOTIFICATION_PUSH` without the
client polling. See [`Report.md`](Report.md) for the full architecture, ERD, and
design decisions.

## Usage

### Prerequisites

- **JDK 17+** (developed and tested on Java 21)
- **Maven 3.8+**
- **PostgreSQL 14+** running locally

### 1. Set up the database

Create the database and the role the server expects by default:

```bash
createdb twitter_clone
psql -d twitter_clone -c "CREATE ROLE phipsitheta LOGIN PASSWORD 'phipsitheta' SUPERUSER;"
```

You don't need to run any SQL by hand — on startup the server applies the bundled
[`schema.sql`](server/src/main/resources/schema.sql), which is idempotent (safe
to run every boot).

### 2. Build

```bash
mvn clean install
```

### 3. Run the server

```bash
java -jar server/target/server-1.0-SNAPSHOT.jar
```

It listens on **port 8080** and prints `Server is listening ...` once ready.

### 4. Run the client(s)

```bash
mvn -pl client javafx:run
```

Open **two** windows and log in as different users to watch real-time delivery in
action:

1. Register two accounts (unique username, valid email, 6+ char password).
2. From window A, open window B's profile and press **Follow**.
3. In window B, post a tweet with an image and a `#hashtag`.
4. It appears in window A's feed instantly, and A's 🔔 badge lights up when B
   likes or replies to A's tweets.
5. Toggle **Dark mode** from the left navigation any time.

### Configuration

The server reads database settings from environment variables (defaults shown):

| Variable | Default | Purpose |
|----------|---------|---------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/twitter_clone` | JDBC URL |
| `DB_USER` | `phipsitheta` | database user |
| `DB_PASSWORD` | *(empty)* | database password |

Example — point the server at a different database:

```bash
DB_URL="jdbc:postgresql://localhost:5432/mydb" DB_USER="me" DB_PASSWORD="secret" \
  java -jar server/target/server-1.0-SNAPSHOT.jar
```

The client connects to `localhost:8080`; change the constants in `ClientMain` to
reach a remote server.

## Demo / Visuals

> _Add screenshots or a short GIF here for the submission._ Suggested captures
> (place them under a `docs/` folder and link them, e.g. `![Home](docs/home.png)`):
>
> - Login screen in **light** and **dark** mode
> - Home feed showing a tweet with an image and highlighted hashtags
> - A profile page (banner, avatar, bio, stats, Follow button)
> - Two windows side-by-side demonstrating a real-time like / new tweet
> - The notifications page with the unread badge

## Credits

- **Course:** Advanced Programming, Summer 2026 — Dr. Saeed Reza Kheradpisheh
- **Team:** Frontend (JavaFX), Backend (sockets & logic), Database (schema & JDBC)
- **Libraries & resources:**
  [Gson](https://github.com/google/gson) ·
  [HikariCP](https://github.com/brettwooldridge/HikariCP) ·
  [jBCrypt](https://www.mindrot.org/projects/jBCrypt/) ·
  [PostgreSQL JDBC](https://jdbc.postgresql.org/) ·
  [OpenJFX](https://openjfx.io/) ·
  [Lucide icons](https://lucide.dev) (ISC License)
- AI assistance is disclosed in [`Report.md`](Report.md#7-ai-usage-disclosure).

## Changelog

**v1.0** — First complete release.

- Auth, sessions, and profiles (avatar, banner, bio, stats).
- Tweets, threaded replies, retweets, likes, follows, and the personalized feed.
- Real-time delivery of tweets, likes, follows, and notifications.
- Hashtags (case-insensitive, canonical-cased display), image media, and search.
- Trending hashtags, dark mode, 280-character limit, and Lucide vector icons.
- Runnable server jar and an auto-initialized, idempotent schema.

**v0.1** — Initial scaffold: modules, JSON protocol contract, and stubbed classes.

## Contact

Questions or issues? Reach the team via the course communication channel or open
an issue on the project's repository.
