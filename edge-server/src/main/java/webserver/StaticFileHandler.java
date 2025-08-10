package webserver;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP handler for serving static files.
 */
public class StaticFileHandler implements HttpHandler {
    // Root directory for static assets
    private final String rootDir = "web";

    // Map of file extensions to MIME types
    private static final Map<String, String> mimeTypes = new HashMap<>();
    static {
        mimeTypes.put("html", "text/html");
        mimeTypes.put("css", "text/css");
        mimeTypes.put("js", "application/javascript");
        mimeTypes.put("png", "image/png");
        mimeTypes.put("jpg", "image/jpeg");
        mimeTypes.put("jpeg", "image/jpeg");
        mimeTypes.put("gif", "image/gif");
        mimeTypes.put("svg", "image/svg+xml");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestedPath = exchange.getRequestURI().getPath();

        // Serve index.html when root is requested
        if (requestedPath.equals("/")) {
            requestedPath = "/index.html";
        }

        File file = new File(rootDir + requestedPath);
        if (!file.exists() || file.isDirectory()) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        String fileExtension = getFileExtension(file.getName());
        String mimeType = mimeTypes.getOrDefault(fileExtension, "application/octet-stream");

        byte[] bytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
        // Set the Content-Type header based on the file type
        exchange.getResponseHeaders().set("Content-Type", mimeType);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String getFileExtension(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf('.');
        return (lastIndexOfDot == -1) ? "" : fileName.substring(lastIndexOfDot + 1);
    }
}
