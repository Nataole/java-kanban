package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest {

    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
    }


    @Test
    void subtaskCannotBeItsOwnEpic() {
        Epic epic = manager.createEpic(new Epic("E", "ED"));

        Subtask subtask = new Subtask("S", "SD", Status.NEW, epic.getId());
        subtask.setId(epic.getId()); // id == epicId

        assertThrows(IllegalArgumentException.class,
                () -> manager.createSubtask(subtask),
                "Подзадача не может быть своим же эпиком");
    }


    @Test
    void taskIsImmutableInsideManager() {
        Task original = new Task("T", "D", Status.NEW);
        Task created = manager.createTask(original);

        original.setTitle("CHANGED");
        original.setDescription("CHANGED");
        original.setStatus(Status.DONE);

        Task fromManager = manager.getTaskById(created.getId());

        assertEquals("T", fromManager.getTitle());
        assertEquals("D", fromManager.getDescription());
        assertEquals(Status.NEW, fromManager.getStatus());
    }

    @Test
    void historyStoresPreviousVersionOfTask() {
        Task task = manager.createTask(new Task("T", "D", Status.NEW));

        manager.getTaskById(task.getId());

        task.setStatus(Status.DONE);
        manager.updateTask(task);

        Task fromHistory = manager.getHistory().get(0);

        assertEquals(Status.NEW, fromHistory.getStatus(),
                "История должна хранить состояние на момент просмотра");
    }

    @Test
    void manualIdDoesNotConflictWithGeneratedId() {
        Task t1 = new Task("A", "A", Status.NEW);
        t1.setId(100);

        Task created1 = manager.createTask(t1);
        Task created2 = manager.createTask(new Task("B", "B", Status.NEW));

        assertNotEquals(created1.getId(), created2.getId(),
                "Задачи с ручным id и сгенерированным id не должны конфликтовать");
    }

    @Test
    void deletingSubtaskRemovesItsIdFromEpic() {
        Epic epic = manager.createEpic(new Epic("E", "ED"));
        Subtask s = manager.createSubtask(new Subtask("S", "SD", Status.NEW, epic.getId()));

        manager.deleteSubtaskById(s.getId());

        Epic fromManager = manager.getEpicById(epic.getId());
        assertTrue(fromManager.getSubtaskIds().isEmpty(), "После удаления подзадачи ее id не должен оставаться в эпике");
    }

    @Test
    void deleteAllSubtasksClearsSubtaskIdsInEpics() {
        Epic epic = manager.createEpic(new Epic("E", "ED"));
        manager.createSubtask(new Subtask("S1", "D1", Status.NEW, epic.getId()));
        manager.createSubtask(new Subtask("S2", "D2", Status.NEW, epic.getId()));

        manager.deleteAllSubtasks();

        Epic fromManager = manager.getEpicById(epic.getId());
        assertTrue(fromManager.getSubtaskIds().isEmpty(), "После deleteAllSubtasks список id у эпика должен быть пустым");
    }
}
