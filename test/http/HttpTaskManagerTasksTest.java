package http;

import com.google.gson.Gson;
import manager.*;
import tasks.*;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.net.HttpURLConnection;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTasksTest {
    private Gson gson;
    private TaskManager manager;
    private HttpTaskServer server;
    private HttpClient client;

    @BeforeEach
    void setUp() throws IOException {

        manager = new InMemoryTaskManager(Managers.getDefaultHistory());
        server = new HttpTaskServer(manager);
        server.start();

        client = HttpClient.newHttpClient();
        gson = HttpTaskServer.getGson();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void shouldAddTaskViaHttp() throws Exception {
        Task task = new Task(
                "Test task",
                "Test description",
                Status.NEW,
                Duration.ofMinutes(5),
                LocalDateTime.now()
        );

        String json = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_CREATED, response.statusCode());

        List<Task> tasks = manager.getAllTasks();
        assertEquals(1, tasks.size());
        assertEquals("Test task", tasks.get(0).getTitle());
    }


    @Test
    void shouldReturnTasks() throws Exception {
        manager.createTask(new Task(
                "Task 1",
                "Desc",
                Status.NEW,
                Duration.ofMinutes(5),
                LocalDateTime.now()
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());

        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(1, tasks.length);
    }

    @Test
    void shouldReturnTaskById() throws Exception {
        Task task = manager.createTask(new Task(
                "Task 1",
                "Desc",
                Status.NEW,
                Duration.ofMinutes(5),
                LocalDateTime.now()
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + task.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());

        Task returned = gson.fromJson(response.body(), Task.class);
        assertEquals(task.getId(), returned.getId());
    }

    @Test
    void shouldDeleteTask() throws Exception {
        Task task = manager.createTask(new Task(
                "Task 1",
                "Desc",
                Status.NEW,
                Duration.ofMinutes(5),
                LocalDateTime.now()
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + task.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        assertTrue(manager.getAllTasks().isEmpty());
    }

    @Test
    void shouldNotAddOverlappingTask() throws Exception {
        manager.createTask(new Task(
                "Task 1",
                "Desc",
                Status.NEW,
                Duration.ofMinutes(60),
                LocalDateTime.of(2026, 4, 1, 10, 0)
        ));

        Task overlapping = new Task(
                "Task 2",
                "Desc",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 4, 1, 10, 30)
        );

        String json = gson.toJson(overlapping);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


        assertEquals(HttpURLConnection.HTTP_NOT_ACCEPTABLE, response.statusCode());
        assertEquals(1, manager.getAllTasks().size());
    }
}

