package manager;

import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager>  {

    @Override
    protected FileBackedTaskManager createManager() {
        try {
            File file = File.createTempFile("tasks", ".csv");
            return new FileBackedTaskManager(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldSaveAndLoadEmptyFile() throws IOException {
        File file = File.createTempFile("tasks", ".csv");

        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertTrue(loadedManager.getAllTasks().isEmpty(),
                "После загрузки пустого файла список задач должен быть пуст");
        assertTrue(loadedManager.getAllEpics().isEmpty(),
                "После загрузки пустого файла список эпиков должен быть пуст");
        assertTrue(loadedManager.getAllSubtasks().isEmpty(),
                "После загрузки пустого файла список подзадач должен быть пуст");
    }

    @Test
    void shouldLoadTasksWithCorrectFields() throws IOException {
        File file = File.createTempFile("tasks", ".csv");

        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task = manager.createTask(new Task(
                "Задача",
                "Описание задачи",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 4, 1, 10, 0)));

        Epic epic = manager.createEpic(new Epic("Эпик", "Описание эпика"));
        Subtask subtask = manager.createSubtask(
                new Subtask("Подзадача",
                        "Описание подзадачи",
                        Status.DONE,
                        epic.getId(),
                        Duration.ofMinutes(15),
                        LocalDateTime.of(2026, 4, 1, 11, 0))
        );

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        Task loadedTask = loadedManager.getTaskById(task.getId());
        Epic loadedEpic = loadedManager.getEpicById(epic.getId());
        Subtask loadedSubtask = loadedManager.getSubtaskById(subtask.getId());

        assertEquals(task.getTitle(), loadedTask.getTitle(),
                "Название задачи должно сохраняться");
        assertEquals(task.getDescription(), loadedTask.getDescription(),
                "Описание задачи должно сохраняться");
        assertEquals(task.getStatus(), loadedTask.getStatus(),
                "Статус задачи должен сохраняться");
        assertEquals(task.getStartTime(), loadedTask.getStartTime(),
                "Время начала задачи должно сохраняться");
        assertEquals(task.getDuration(), loadedTask.getDuration(),
                "Продолжительность задачи должна сохраняться");
        assertEquals(task.getEndTime(), loadedTask.getEndTime(),
                "Время окончания задачи должно вычисляться корректно");

        assertEquals(epic.getTitle(), loadedEpic.getTitle(),
                "Название эпика должно сохраняться");
        assertEquals(epic.getDescription(), loadedEpic.getDescription(),
                "Описание эпика должно сохраняться");
        assertEquals(Status.DONE, loadedEpic.getStatus(),
                "Статус эпика должен пересчитываться по подзадачам");

        assertEquals(subtask.getTitle(), loadedSubtask.getTitle(),
                "Название подзадачи должно сохраняться");
        assertEquals(subtask.getDescription(), loadedSubtask.getDescription(),
                "Описание подзадачи должно сохраняться");
        assertEquals(subtask.getStatus(), loadedSubtask.getStatus(),
                "Статус подзадачи должен сохраняться");
        assertEquals(subtask.getEpicId(), loadedSubtask.getEpicId(),
                "EpicId подзадачи должен сохраняться");
        assertEquals(subtask.getStartTime(), loadedSubtask.getStartTime(),
                "Время начала подзадачи должно сохраняться");
        assertEquals(subtask.getDuration(), loadedSubtask.getDuration(),
                "Продолжительность подзадачи должна сохраняться");
        assertEquals(subtask.getEndTime(), loadedSubtask.getEndTime(),
                "Время окончания подзадачи должно вычисляться корректно");

        assertNotNull(loadedTask,
                "Задача должна загружаться из файла");
        assertNotNull(loadedEpic,
                "Эпик должен загружаться из файла");
        assertNotNull(loadedSubtask,
                "Подзадача должна загружаться из файла");
    }

    @Test
    void shouldRestoreEpicCalculatedTimeFields() throws IOException {
        File file = File.createTempFile("tasks", ".csv");

        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Epic epic = manager.createEpic(new Epic("Эпик", "Описание эпика"));

        manager.createSubtask(new Subtask(
                "Подзадача 1",
                "Описание 1",
                Status.NEW,
                epic.getId(),
                Duration.ofMinutes(20),
                LocalDateTime.of(2026, 4, 1, 12, 0)
        ));

        manager.createSubtask(new Subtask(
                "Подзадача 2",
                "Описание 2",
                Status.DONE,
                epic.getId(),
                Duration.ofMinutes(40),
                LocalDateTime.of(2026, 4, 1, 13, 0)
        ));

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        Epic loadedEpic = loadedManager.getEpicById(epic.getId());

        assertNotNull(loadedEpic, "Эпик должен быть загружен");
        assertEquals(LocalDateTime.of(2026, 4, 1, 12, 0), loadedEpic.getStartTime(),
                "Время начала эпика должно быть временем самой ранней подзадачи");
        assertEquals(Duration.ofMinutes(60), loadedEpic.getDuration(),
                "Продолжительность эпика должна быть суммой продолжительностей подзадач");
        assertEquals(LocalDateTime.of(2026, 4, 1, 13, 40), loadedEpic.getEndTime(),
                "Время окончания эпика должно быть временем окончания самой поздней подзадачи");
    }

    @Test
    void shouldNotThrowWhenWorkingWithTempFile() throws IOException {
        File file = File.createTempFile("tasks", ".csv");

        assertDoesNotThrow(() -> {
            FileBackedTaskManager manager = new FileBackedTaskManager(file);
            manager.createTask(new Task(
                    "Задача",
                    "Описание",
                    Status.NEW,
                    Duration.ofMinutes(30),
                    LocalDateTime.of(2026, 4, 1, 10, 0)
            ));
            FileBackedTaskManager.loadFromFile(file);
        }, "Работа с временным файлом не должна выбрасывать исключение");
    }
}