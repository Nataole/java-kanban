package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import tasks.Epic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.net.HttpURLConnection;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public EpicsHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET":
                    handleGet(exchange, path);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange, path);
                    break;
                default:
                    sendNotFound(exchange);
            }
        } catch (IllegalArgumentException e) {
            sendNotFound(exchange);
        } catch (Exception e) {
            sendServerError(exchange);
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        String[] pathParts = path.split("/");

        if (pathParts.length == 2) {
            sendText(exchange, gson.toJson(manager.getAllEpics()), HttpURLConnection.HTTP_OK);
            return;
        }

        if (pathParts.length == 3) {
            int id = Integer.parseInt(pathParts[2]);
            Epic epic = manager.getEpicById(id);

            if (epic == null) {
                sendNotFound(exchange);
                return;
            }

            sendText(exchange, gson.toJson(epic), HttpURLConnection.HTTP_OK);
            return;
        }

        if (pathParts.length == 4 && "subtasks".equals(pathParts[3])) {
            int id = Integer.parseInt(pathParts[2]);
            Epic epic = manager.getEpicById(id);

            if (epic == null) {
                sendNotFound(exchange);
                return;
            }

            sendText(exchange, gson.toJson(manager.getSubtasksByEpicId(id)), HttpURLConnection.HTTP_OK);
            return;
        }

        sendNotFound(exchange);
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Epic epic = gson.fromJson(body, Epic.class);

        Epic result;
        if (epic.getId() == 0) {
            result = manager.createEpic(epic);
        } else {
            result = manager.updateEpic(epic);
        }

        if (result == null) {
            sendNotFound(exchange);
            return;
        }

        sendText(exchange, gson.toJson(result), HttpURLConnection.HTTP_CREATED);
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        String[] pathParts = path.split("/");

        if (pathParts.length != 3) {
            sendNotFound(exchange);
            return;
        }

        int id = Integer.parseInt(pathParts[2]);
        Epic epic = manager.getEpicById(id);

        if (epic == null) {
            sendNotFound(exchange);
            return;
        }

        manager.deleteEpicById(id);
        sendText(exchange, "{\"message\":\"Эпик удалён\"}", HttpURLConnection.HTTP_OK);
    }
}

