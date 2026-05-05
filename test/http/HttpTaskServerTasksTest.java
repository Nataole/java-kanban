package http;

import com.google.gson.Gson;
import manager.InMemoryTaskManager;
import manager.Managers;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Status;
import tasks.Task;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskServerTasksTest {
    private TaskManager manager;
    private HttpTaskServer server;
    private Gson gson;
    private HttpClient client;

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager(Managers.getDefaultHistory());
        server = new HttpTaskServer(manager);
        gson = HttpTaskServer.getGson();
        client = HttpClient.newHttpClient();
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void shouldCreateTask() throws IOException, InterruptedException {
        Task task = new Task(
                "Задача",
                "Описание",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 4, 1, 10, 0)
        );

        String json = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_CREATED, response.statusCode());
        assertEquals(1, manager.getAllTasks().size());
        assertEquals("Задача", manager.getAllTasks().get(0).getTitle());
    }

    @Test
    void shouldGetTaskById() throws IOException, InterruptedException {
        Task task = manager.createTask(new Task(
                "Задача",
                "Описание",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 4, 1, 10, 0)
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + task.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
    }

    @Test
    void shouldReturnNotFoundForMissingTask() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
    }

    @Test
    void shouldReturnNotAcceptableForOverlappingTask() throws IOException, InterruptedException {
        manager.createTask(new Task(
                "Задача 1",
                "Описание",
                Status.NEW,
                Duration.ofMinutes(60),
                LocalDateTime.of(2026, 4, 1, 10, 0)
        ));

        Task task = new Task(
                "Задача 2",
                "Описание",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 4, 1, 10, 30)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_NOT_ACCEPTABLE, response.statusCode());
    }

    @Test
    void shouldDeleteTask() throws IOException, InterruptedException {
        Task task = manager.createTask(new Task("Задача", "Описание", Status.NEW));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + task.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        assertEquals(0, manager.getAllTasks().size());
    }
}

