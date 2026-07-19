# Twitter Clone — AP Final Project

Final project for the Advanced Programming course: a minimal social network
inspired by X (Twitter), built with a Client–Server architecture, JavaFX,
Java sockets, and PostgreSQL.

## Project structure (Maven multi-module)

```
twitter-clone/
├── shared/   ← models and the JSON protocol contract (shared between client and server)
├── server/   ← server: networking, database, business logic (AmirAli + Hesam)
└── client/   ← JavaFX client (Faraz)
```

Every Java file in this project has a header comment stating who **owns**
it and which **phase** it should be completed in, plus precise `TODO`
comments on each method explaining what needs to be written. The logic
inside these files is intentionally incomplete (in many places it's
`throw new UnsupportedOperationException("TODO...")`) — this is a scaffold,
not a finished implementation.

## How to get started (all 3 people)

1. Clone this repository and open the project root.
2. Find your file: every class says "Owner: [your name]" at the top.
3. Work through the TODO comments phase by phase (following their numbering).
4. Before changing anything in the `shared` module (especially `PacketType`
   and `Packet`), coordinate with the other two — this is the shared
   contract everyone depends on.

## Phases (summary)

| Phase | Days | Topic |
|---|---|---|
| 0 | 1-2 | Infrastructure: client-server-database connection |
| 1 | 3-5 | Authentication and session management |
| 2 | 6-8 | Tweets and the real-time feed |
| 3 | 9-11 | Follow, likes, replies |
| 4 | 12-14 | Search, polish, testing, documentation |

Full task details are in the code comments.

## Running the project (after implementation is complete)

```
# database: run the schema
psql -U <user> -d twitter_clone -f server/src/main/resources/schema.sql

# build the whole project
mvn -q -pl shared,server,client -am install

# run the server
java -jar server/target/server-1.0-SNAPSHOT.jar

# run the client (after adding the javafx-maven-plugin per the TODO in client/pom.xml)
mvn -pl client javafx:run
```

## Credits

- Course: Advanced Programming, Summer 2026 — Dr. Saeed Reza Kheradpisheh
- Team: Faraz (Frontend), Hesam (Backend), AmirAli (Database)

## Changelog

- v0.1 — initial project scaffold (modules, protocol contract, empty classes with TODOs)
