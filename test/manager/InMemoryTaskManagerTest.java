package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
    }

    @Test
    void addAndFindTaskById() {
        Task created = manager.createTask(new Task("T", "D", Status.NEW));
        Task found = manager.getTaskById(created.getId());

        assertNotNull(found, "Задача должна находиться по id");
        assertEquals(created, found, "Найденная задача должна совпадать по id");
    }

    @Test
    void addAndFindEpicAndSubtaskById() {
        Epic epic = manager.createEpic(new Epic("E", "ED"));
        Subtask sub = manager.createSubtask(new Subtask("S", "SD", Status.NEW, epic.getId()));

        assertNotNull(manager.getEpicById(epic.getId()), "Эпик должен находиться по id");
        assertNotNull(manager.getSubtaskById(sub.getId()), "Подзадача должна находиться по id");
    }

    @Test
    void createSubtaskForMissingEpicReturnsNull() {
        Subtask sub = manager.createSubtask(new Subtask("S", "SD", Status.NEW, 9999));
        assertNull(sub, "Если эпика нет, подзадача не должна создаваться");
    }

    @Test
    void epicStatusBecomesDoneWhenAllSubtasksDone() {
        Epic epic = manager.createEpic(new Epic("E", "ED"));
        Subtask s1 = manager.createSubtask(new Subtask("S1", "D1", Status.NEW, epic.getId()));
        Subtask s2 = manager.createSubtask(new Subtask("S2", "D2", Status.NEW, epic.getId()));

        s1.setStatus(Status.DONE);
        s2.setStatus(Status.DONE);
        manager.updateSubtask(s1);
        manager.updateSubtask(s2);

        assertEquals(Status.DONE, manager.getEpicById(epic.getId()).getStatus(), "Эпик должен стать DONE");
    }
}