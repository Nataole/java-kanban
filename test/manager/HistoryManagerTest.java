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
    void historyDoesNotStoreDuplicates() {
        Task t = manager.createTask(new Task("T", "D", Status.NEW));

        manager.getTaskById(t.getId());
        manager.getTaskById(t.getId());
        manager.getTaskById(t.getId());

        List<Task> history = manager.getHistory();
        assertEquals(1, history.size(), "История не должна хранить дубликаты");
        assertEquals(t.getId(), history.get(0).getId(), "Должен остаться последний просмотр");
    }

    @Test
    void removingTaskRemovesItFromHistory() {
        Task t = manager.createTask(new Task("T", "D", Status.NEW));
        manager.getTaskById(t.getId());

        manager.deleteTaskById(t.getId());

        assertTrue(manager.getHistory().isEmpty(), "Удаленная задача должна пропасть из истории");
    }

    @Test
    void removingEpicRemovesEpicAndItsSubtasksFromHistory() {
        Epic e = manager.createEpic(new Epic("E", "ED"));
        Subtask s1 = manager.createSubtask(new Subtask("S1", "D1", Status.NEW, e.getId()));
        Subtask s2 = manager.createSubtask(new Subtask("S2", "D2", Status.NEW, e.getId()));

        manager.getEpicById(e.getId());
        manager.getSubtaskById(s1.getId());
        manager.getSubtaskById(s2.getId());

        manager.deleteEpicById(e.getId());

        List<Task> history = manager.getHistory();
        assertTrue(history.isEmpty(), "После удаления эпика из истории должны исчезнуть эпик и его подзадачи");
    }

    @Test
    void deleteAllTasksRemovesTasksFromHistory() {
        Task t1 = manager.createTask(new Task("T1", "D1", Status.NEW));
        Task t2 = manager.createTask(new Task("T2", "D2", Status.NEW));

        manager.getTaskById(t1.getId());
        manager.getTaskById(t2.getId());

        manager.deleteAllTasks();

        assertTrue(manager.getHistory().isEmpty(),
                "После deleteAllTasks история должна быть пустой");
    }

    @Test
    void deleteAllEpicsRemovesEpicsAndSubtasksFromHistory() {
        Epic e = manager.createEpic(new Epic("E", "ED"));
        Subtask s1 = manager.createSubtask(new Subtask("S1", "D1", Status.NEW, e.getId()));
        Subtask s2 = manager.createSubtask(new Subtask("S2", "D2", Status.NEW, e.getId()));

        manager.getEpicById(e.getId());
        manager.getSubtaskById(s1.getId());
        manager.getSubtaskById(s2.getId());

        manager.deleteAllEpics();

        assertTrue(manager.getHistory().isEmpty(),
                "После deleteAllEpics история должна быть пустой");
    }

}