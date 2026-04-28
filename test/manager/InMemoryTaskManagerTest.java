package manager;

import org.junit.jupiter.api.Test;
import tasks.Status;
import tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createManager() {
        return new InMemoryTaskManager(Managers.getDefaultHistory());
    }

    @Test
    void shouldDetectOverlappingTasks() {
        InMemoryTaskManager manager = createManager();

        Task task1 = new Task(
                "Задача 1",
                "Описание 1",
                Status.NEW,
                Duration.ofMinutes(60),
                LocalDateTime.of(2026, 4, 1, 10, 0)
        );

        Task task2 = new Task(
                "Задача 2",
                "Описание 2",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 4, 1, 10, 30)
        );

        assertTrue(manager.isTasksOverlap(task1, task2));
    }

    @Test
    void shouldNotDetectOverlapWhenTasksDoNotIntersect() {
        InMemoryTaskManager manager = createManager();

        Task task1 = new Task(
                "Задача 1",
                "Описание 1",
                Status.NEW,
                Duration.ofMinutes(60),
                LocalDateTime.of(2026, 4, 1, 10, 0)
        );

        Task task2 = new Task(
                "Задача 2",
                "Описание 2",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 4, 1, 11, 0)
        );

        assertFalse(manager.isTasksOverlap(task1, task2));
    }
}

