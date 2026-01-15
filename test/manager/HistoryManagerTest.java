package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HistoryManagerTest {

    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
    }

    @Test
    void historyIsEmptyInitially() {
        assertTrue(manager.getHistory().isEmpty(), "История должна быть пустой в начале");
    }

    @Test
    void historyAddsItemsOnGetById() {
        Task t = manager.createTask(new Task("T", "D", Status.NEW));
        Epic e = manager.createEpic(new Epic("E", "ED"));
        Subtask s = manager.createSubtask(new Subtask("S", "SD", Status.NEW, e.getId()));

        manager.getTaskById(t.getId());
        manager.getEpicById(e.getId());
        manager.getSubtaskById(s.getId());

        List<Task> history = manager.getHistory();
        assertEquals(3, history.size(), "Должно быть 3 просмотра в истории");
        assertEquals(t.getId(), history.get(0).getId());
        assertEquals(e.getId(), history.get(1).getId());
        assertEquals(s.getId(), history.get(2).getId());
    }

    @Test
    void historyStoresOnlyLast10() {
        for (int i = 0; i < 12; i++) {
            Task t = manager.createTask(new Task("T" + i, "D" + i, Status.NEW));
            manager.getTaskById(t.getId());
        }
        List<Task> history = manager.getHistory();
        assertEquals(10, history.size(), "История должна хранить максимум 10 элементов");
    }
}