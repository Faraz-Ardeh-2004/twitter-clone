package com.twitterclone.server;

import com.twitterclone.server.db.DatabaseConnection;
import com.twitterclone.server.db.SchemaInitializer;
import com.twitterclone.server.network.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ============================================================
 * Owner: Hesam (Backend) | Phase 0 - Day 1-2
 * ============================================================
 * Server entry point. This class only does three things:
 *   1. Opens a ServerSocket on a fixed port.
 *   2. Creates a fixed-size Thread Pool so each client gets its own thread.
 *   3. For every incoming connection, creates a ClientHandler and submits it
 *      to the thread pool.
 *
 * Phase 0 Definition of Done: client connects, sends PING, gets PONG back;
 * server does not crash.
 */
public class ServerMain {

    // TODO(Hesam): keep the port here, or move it to a config file (e.g. config.properties).
    private static final int PORT = 8080;

    // TODO(Hesam): tune the thread pool size to the expected number of
    // clients. For this project (a handful of clients on a local network),
    // 20 threads is plenty.
    private static final int THREAD_POOL_SIZE = 20;

    public static void main(String[] args) {
        System.out.println("Twitter-clone server starting on port " + PORT + " ...");

        // Fail fast if the database is unreachable, and ensure the schema exists.
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            System.out.println("Database connection OK (" + conn.getMetaData().getURL() + ")");
            SchemaInitializer.initialize();
            System.out.println("Schema verified / initialized.");
        } catch (Exception e) {
            System.err.println("FATAL: cannot connect to or initialize the database: " + e.getMessage());
            System.err.println("Check DB_URL / DB_USER / DB_PASSWORD and that PostgreSQL is running.");
            return;
        }

        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT + " ...");

            // Main accept loop: one ClientHandler per connection, each run on a
            // pooled thread so many clients are served concurrently.
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Server socket error: " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }
}
