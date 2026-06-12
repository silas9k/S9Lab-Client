package site.s9lab.backend.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public final class Json {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private Json() {
    }

    public static <T> T read(HttpExchange exchange, Class<T> type) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, type);
        } catch (JsonSyntaxException exception) {
            throw new IOException("invalid_json", exception);
        }
    }

    public static void send(HttpExchange exchange, int status, Object value) throws IOException {
        byte[] body = GSON.toJson(value).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(body);
        }
    }
}
