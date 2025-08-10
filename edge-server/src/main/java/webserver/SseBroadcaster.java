package webserver;

import java.io.PrintWriter;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Broadcasts messages to all connected clients via Server-Sent Events (SSE).
 */
public class SseBroadcaster {
    // Using a thread-safe list to store client writers
    private static final CopyOnWriteArrayList<PrintWriter> clients = new CopyOnWriteArrayList<>();

    public static void addClient(PrintWriter writer) {
        System.out.println("Client connected: " + writer);
        clients.add(writer);
    }

    public static void removeClient(PrintWriter writer) {
        System.out.println("Client disconnected: " + writer);
        clients.remove(writer);
    }

    public static void broadcast(String data) {
        for (PrintWriter writer : clients) {
            try {
                writer.print("data: " + data + "\n\n");
                writer.flush();
            } catch (Exception e) {
                System.err.println("Error broadcasting to client, removing client: " + writer);
                // Remove any client that fails
                clients.remove(writer);
            }
        }
    }
}
