package com.twitterclone.server;

import com.twitterclone.server.db.DatabaseConnection;
import com.twitterclone.server.network.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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

        // TODO(Hesam): before accepting connections, make sure the database
        // is reachable. You can run a quick test here:
        // DatabaseConnection.getInstance().getConnection() and if it throws,
        // stop the server with a clear message (fail-fast is better than a
        // crash later).
        // Example:
        //   try (var conn = DatabaseConnection.getInstance().getConnection()) {
        //       System.out.println("Database connection OK");
        //   } catch (Exception e) {
        //       System.err.println("Cannot connect to database: " + e.getMessage());
        //       return;
        //   }

        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening...");

            // TODO(Hesam): this is the server's main loop. For every new connection:
            //   1. serverSocket.accept() returns a new Socket (blocks until a client connects)
            //   2. create a new ClientHandler with that Socket
            //   3. run it with threadPool.execute(handler) (not a manually created
            //      thread; use the pool)
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket);
                threadPool.execute(handler);
            }

        } catch (IOException e) {
            // TODO(Hesam): better logging (e.g. via java.util.logging), and retry if needed.
            System.err.println("Server socket error: " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }
}
