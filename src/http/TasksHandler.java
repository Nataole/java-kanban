package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import tasks.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public TasksHandler(TaskManager manager, Gson gson) {
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
            sendText(exchange, gson.toJson(manager.getAllTasks()), 200);
            return;
        }

        if (pathParts.length == 3) {
            int id = Integer.parseInt(pathParts[2]);
            Task task = manager.getTaskById(id);

            if (task == null) {
                sendNotFound(exchange);
                return;
            }

            sendText(exchange, gson.toJson(task), 200);
            return;
        }

        sendNotFound(exchange);
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Task task = gson.fromJson(body, Task.class);

        Task result;
        if (task.getId() == 0) {
            result = manager.createTask(task);
        } else {
            result = manager.updateTask(task);
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
        Task task = manager.getTaskById(id);

        if (task == null) {
            sendNotFound(exchange);
            return;
        }

        manager.deleteTaskById(id);
        sendText(exchange, "{\"message\":\"Задача удалена\"}", 200);
    }
}
