package webserver;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * HTTP handler for the SSE endpoint.
 */
public class SseHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Set required SSE headers
        exchange.getResponseHeaders().set("Content-Type", "text/event-stream;charset=UTF-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        exchange.getResponseHeaders().set("Connection", "keep-alive");
        exchange.sendResponseHeaders(200, 0);

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(exchange.getResponseBody(), StandardCharsets.UTF_8), true);
        SseBroadcaster.addClient(writer);

        try {
            // Keep the connection open indefinitely
            while (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            // Thread interrupted; exit
        } finally {
            SseBroadcaster.removeClient(writer);
            writer.close();
            exchange.close();
        }
    }
}
