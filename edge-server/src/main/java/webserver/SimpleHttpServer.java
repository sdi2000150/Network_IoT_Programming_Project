package webserver;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple HTTP server that serves static files and provides an SSE endpoint.
 */
public class SimpleHttpServer {
    private static final Logger logger = LoggerFactory.getLogger(SimpleHttpServer.class);

    private HttpServer server;

    public SimpleHttpServer(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Serve static assets from the "web" directory
        server.createContext("/", new StaticFileHandler());
        // SSE endpoint for live updates
        server.createContext("/sse", new SseHandler());

        // Use a cached thread pool executor with a larger pool size
        server.setExecutor(Executors.newFixedThreadPool(50)); // Allow up to 50 concurrent threads
    }

    public void start() {
        server.start();
        logger.info("[SimpleHttpServer] HTTP Server started on port " + server.getAddress().getPort());
    }

    public void stop() {
        server.stop(0);
    }
}
