package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import tasks.Subtask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public SubtasksHandler(TaskManager manager, Gson gson) {
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
            sendHasInteractions(exchange);
        } catch (Exception e) {
            sendServerError(exchange);
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        String[] pathParts = path.split("/");

        if (pathParts.length == 2) {
            sendText(exchange, gson.toJson(manager.getAllSubtasks()), 200);
            return;
        }

        if (pathParts.length == 3) {
            int id = Integer.parseInt(pathParts[2]);
            Subtask subtask = manager.getSubtaskById(id);

            if (subtask == null) {
                sendNotFound(exchange);
                return;
            }

            sendText(exchange, gson.toJson(subtask), 200);
            return;
        }

        sendNotFound(exchange);
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Subtask subtask = gson.fromJson(body, Subtask.class);

        Subtask result;
        if (subtask.getId() == 0) {
            result = manager.createSubtask(subtask);
        } else {
            result = manager.updateSubtask(subtask);
        }

        if (result == null) {
            sendNotFound(exchange);
            return;
        }

        sendText(exchange, gson.toJson(result), 201);
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        String[] pathParts = path.split("/");

        if (pathParts.length != 3) {
            sendNotFound(exchange);
            return;
        }

        int id = Integer.parseInt(pathParts[2]);
        Subtask subtask = manager.getSubtaskById(id);

        if (subtask == null) {
            sendNotFound(exchange);
            return;
        }

        manager.deleteSubtaskById(id);
        sendText(exchange, "{\"message\":\"Подзадача удалена\"}", 200);
    }
}

