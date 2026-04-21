package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Status;
import tasks.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void shouldReturnEmptyHistory() {
        assertTrue(historyManager.getHistory().isEmpty(),
                "История должна быть пустой в начале");
    }

    @Test
    void shouldAddTasksToHistory() {
        Task task1 = new Task("Задача 1", "Описание 1", Status.NEW);
        task1.setId(1);

        Task task2 = new Task("Задача 2", "Описание 2", Status.NEW);
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "В истории должно быть 2 задачи");
        assertEquals(1, history.get(0).getId(), "Первой должна быть задача 1");
        assertEquals(2, history.get(1).getId(), "Второй должна быть задача 2");
    }

    @Test
    void shouldNotDuplicateTasksInHistory() {
        Task task = new Task("Задача", "Описание", Status.NEW);
        task.setId(1);

        historyManager.add(task);
        historyManager.add(task);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(),
                "История не должна содержать дубликаты");
        assertEquals(1, history.get(0).getId(),
                "В истории должна остаться одна задача с id = 1");
    }

    @Test
    void shouldRemoveTaskFromBeginningOfHistory() {
        Task task1 = new Task("1", "1", Status.NEW);
        task1.setId(1);

        Task task2 = new Task("2", "2", Status.NEW);
        task2.setId(2);

        Task task3 = new Task("3", "3", Status.NEW);
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "После удаления должно остаться 2 задачи");
        assertEquals(2, history.get(0).getId(), "Первой должна стать задача 2");
        assertEquals(3, history.get(1).getId(), "Второй должна быть задача 3");
    }

    @Test
    void shouldRemoveTaskFromMiddleOfHistory() {
        Task task1 = new Task("1", "1", Status.NEW);
        task1.setId(1);

        Task task2 = new Task("2", "2", Status.NEW);
        task2.setId(2);

        Task task3 = new Task("3", "3", Status.NEW);
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "После удаления должно остаться 2 задачи");
        assertEquals(1, history.get(0).getId(), "Первой должна быть задача 1");
        assertEquals(3, history.get(1).getId(), "Второй должна быть задача 3");
    }

    @Test
    void shouldRemoveTaskFromEndOfHistory() {
        Task task1 = new Task("1", "1", Status.NEW);
        task1.setId(1);

        Task task2 = new Task("2", "2", Status.NEW);
        task2.setId(2);

        Task task3 = new Task("3", "3", Status.NEW);
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(3);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "После удаления должно остаться 2 задачи");
        assertEquals(1, history.get(0).getId(), "Первой должна быть задача 1");
        assertEquals(2, history.get(1).getId(), "Второй должна быть задача 2");
    }
}