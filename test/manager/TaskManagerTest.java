package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;

    protected abstract T createManager();


    @BeforeEach
    void setUp() {
        manager = createManager();
    }

    @Test
    void shouldAddAndFindTaskById() {
        Task task = manager.createTask(new Task(
                "Задача",
                "Описание",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 4, 1, 10, 0)
        ));

        Task foundTask = manager.getTaskById(task.getId());

        assertNotNull(foundTask, "Задача должна находиться по id");
        assertEquals(task.getId(), foundTask.getId(), "Id должны совпадать");
    }

    @Test
    void shouldAddAndFindEpicAndSubtaskById() {
        Epic epic = manager.createEpic(new Epic("Эпик", "Описание эпика"));
        Subtask subtask = manager.createSubtask(new Subtask(
                "Подзадача",
                "Описание подзадачи",
                Status.NEW,
                epic.getId(),
                Duration.ofMinutes(20),
                LocalDateTime.of(2026, 4, 1, 12, 0)
        ));

        assertNotNull(manager.getEpicById(epic.getId()), "Эпик должен находиться по id");
        assertNotNull(manager.getSubtaskById(subtask.getId()), "Подзадача должна находиться по id");
    }

    @Test
    void shouldNotCreateSubtaskWithoutEpic() {
        Subtask subtask = manager.createSubtask(new Subtask(
                "Подзадача",
                "Описание",
                Status.NEW,
                999,
                Duration.ofMinutes(20),
                LocalDateTime.of(2026, 4, 1, 12, 0)
        ));

        assertNull(subtask, "Подзадача не должна создаваться без эпика");
    }

    @Test
    void subtaskCannotBeItsOwnEpic() {
        Epic epic = manager.createEpic(new Epic("E", "ED"));

        Subtask subtask = new Subtask("S",
                "SD",
                Status.NEW,
                epic.getId(),
                Duration.ofMinutes(20),
                LocalDateTime.of(2026, 4, 1, 12, 0)
        );
        subtask.setId(epic.getId()); // id == epicId

        assertThrows(IllegalArgumentException.class,
                () -> manager.createSubtask(subtask),
                "Подзадача не может быть своим же эпиком");
    }


    @Test
    void taskIsImmutableInsideManager() {
        Task original = new Task("T",
                "D",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 4, 1, 10, 0)
        );

        Task created = manager.createTask(original);

        original.setTitle("CHANGED");
        original.setDescription("CHANGED");
        original.setStatus(Status.DONE);
        original.setDuration(Duration.ofMinutes(999));
        original.setStartTime(LocalDateTime.of(2030, 1, 1, 0, 0));

        Task fromManager = manager.getTaskById(created.getId());

        assertEquals("T", fromManager.getTitle());
        assertEquals("D", fromManager.getDescription());
        assertEquals(Status.NEW, fromManager.getStatus());
        assertEquals(Duration.ofMinutes(30), fromManager.getDuration());
        assertEquals(LocalDateTime.of(2026, 4, 1, 10, 0), fromManager.getStartTime());

    }

    @Test
    void historyStoresPreviousVersionOfTask() {
        Task task = manager.createTask(new Task("T",
                "D",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 4, 1, 10, 0)
        ));

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
    void shouldReturnTasksInPriorityOrder() {
        Task task1 = manager.createTask(new Task(
                "Задача 1",
                "Описание 1",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 4, 1, 12, 0)
        ));

        Task task2 = manager.createTask(new Task(
                "Задача 2",
                "Описание 2",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 4, 1, 10, 0)
        ));

        Task task3 = manager.createTask(new Task(
                "Задача 3",
                "Описание 3",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 4, 1, 11, 0)
        ));

        List<Task> prioritizedTasks = manager.getPrioritizedTasks();

        assertEquals(3, prioritizedTasks.size());
        assertEquals(task2.getId(), prioritizedTasks.get(0).getId());
        assertEquals(task3.getId(), prioritizedTasks.get(1).getId());
        assertEquals(task1.getId(), prioritizedTasks.get(2).getId());
    }

    @Test
    void shouldNotIncludeTasksWithoutStartTimeInPrioritizedList() {
        Task taskWithoutTime = manager.createTask(new Task(
                "Без времени",
                "Описание",
                Status.NEW
        ));

        Task taskWithTime = manager.createTask(new Task(
                "Со временем",
                "Описание",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 4, 1, 10, 0)
        ));

        List<Task> prioritizedTasks = manager.getPrioritizedTasks();

        assertEquals(1, prioritizedTasks.size());
        assertEquals(taskWithTime.getId(), prioritizedTasks.get(0).getId());
        assertNotEquals(taskWithoutTime.getId(), prioritizedTasks.get(0).getId());
    }

    @Test
    void shouldThrowExceptionWhenAddingOverlappingTask() {
        manager.createTask(new Task(
                "Задача 1",
                "Описание 1",
                Status.NEW,
                Duration.ofMinutes(60),
                LocalDateTime.of(2026, 4, 1, 10, 0)
        ));

        Task overlappingTask = new Task(
                "Задача 2",
                "Описание 2",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 4, 1, 10, 30)
        );

        assertThrows(IllegalArgumentException.class,
                () -> manager.createTask(overlappingTask));
    }

    @Test
    void shouldThrowExceptionWhenAddingOverlappingSubtask() {
        Epic epic = manager.createEpic(new Epic("Эпик", "Описание эпика"));

        manager.createTask(new Task(
                "Задача 1",
                "Описание 1",
                Status.NEW,
                Duration.ofMinutes(60),
                LocalDateTime.of(2026, 4, 1, 10, 0)
        ));

        Subtask overlappingSubtask = new Subtask(
                "Подзадача",
                "Описание подзадачи",
                Status.NEW,
                epic.getId(),
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 4, 1, 10, 30)
        );

        assertThrows(IllegalArgumentException.class,
                () -> manager.createSubtask(overlappingSubtask));
    }

    @Test
    void epicStatusShouldBeNewWhenAllSubtasksNew() {
        Epic epic = manager.createEpic(new Epic("Эпик", "Описание"));
        manager.createSubtask(new Subtask(
                "Подзадача 1", "Описание", Status.NEW, epic.getId(),
                Duration.ofMinutes(20), LocalDateTime.of(2026, 4, 1, 12, 0)
        ));
        manager.createSubtask(new Subtask(
                "Подзадача 2", "Описание", Status.NEW, epic.getId(),
                Duration.ofMinutes(20), LocalDateTime.of(2026, 4, 1, 13, 0)
        ));

        assertEquals(Status.NEW, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void epicStatusShouldBeDoneWhenAllSubtasksDone() {
        Epic epic = manager.createEpic(new Epic("Эпик", "Описание"));
        manager.createSubtask(new Subtask(
                "Подзадача 1", "Описание", Status.DONE, epic.getId(),
                Duration.ofMinutes(20), LocalDateTime.of(2026, 4, 1, 12, 0)
        ));
        manager.createSubtask(new Subtask(
                "Подзадача 2", "Описание", Status.DONE, epic.getId(),
                Duration.ofMinutes(20), LocalDateTime.of(2026, 4, 1, 13, 0)
        ));

        assertEquals(Status.DONE, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void epicStatusShouldBeInProgressWhenSubtasksNewAndDone() {
        Epic epic = manager.createEpic(new Epic("Эпик", "Описание"));
        manager.createSubtask(new Subtask(
                "Подзадача 1", "Описание", Status.NEW, epic.getId(),
                Duration.ofMinutes(20), LocalDateTime.of(2026, 4, 1, 12, 0)
        ));
        manager.createSubtask(new Subtask(
                "Подзадача 2", "Описание", Status.DONE, epic.getId(),
                Duration.ofMinutes(20), LocalDateTime.of(2026, 4, 1, 13, 0)
        ));

        assertEquals(Status.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void epicStatusShouldBeInProgressWhenAnySubtaskInProgress() {
        Epic epic = manager.createEpic(new Epic("Эпик", "Описание"));
        manager.createSubtask(new Subtask(
                "Подзадача 1", "Описание", Status.IN_PROGRESS, epic.getId(),
                Duration.ofMinutes(20), LocalDateTime.of(2026, 4, 1, 12, 0)
        ));
        manager.createSubtask(new Subtask(
                "Подзадача 2", "Описание", Status.NEW, epic.getId(),
                Duration.ofMinutes(20), LocalDateTime.of(2026, 4, 1, 13, 0)
        ));

        assertEquals(Status.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus());
    }
}

