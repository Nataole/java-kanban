package manager;

import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

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
    void shouldSaveSeveralTasks() throws IOException {
        File file = File.createTempFile("tasks", ".csv");

        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task = manager.createTask(new Task("Задача", "Описание задачи", Status.NEW));
        Epic epic = manager.createEpic(new Epic("Эпик", "Описание эпика"));
        Subtask subtask = manager.createSubtask(
                new Subtask("Подзадача", "Описание подзадачи", Status.IN_PROGRESS, epic.getId())
        );

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertEquals(1, loadedManager.getAllTasks().size(),
                "После загрузки должна быть 1 задача");
        assertEquals(1, loadedManager.getAllEpics().size(),
                "После загрузки должен быть 1 эпик");
        assertEquals(1, loadedManager.getAllSubtasks().size(),
                "После загрузки должна быть 1 подзадача");

        assertNotNull(loadedManager.getTaskById(task.getId()),
                "Задача должна загружаться из файла");
        assertNotNull(loadedManager.getEpicById(epic.getId()),
                "Эпик должен загружаться из файла");
        assertNotNull(loadedManager.getSubtaskById(subtask.getId()),
                "Подзадача должна загружаться из файла");
    }

    @Test
    void shouldLoadTasksWithCorrectFields() throws IOException {
        File file = File.createTempFile("tasks", ".csv");

        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task = manager.createTask(new Task("Задача", "Описание задачи", Status.NEW));
        Epic epic = manager.createEpic(new Epic("Эпик", "Описание эпика"));
        Subtask subtask = manager.createSubtask(
                new Subtask("Подзадача", "Описание подзадачи", Status.DONE, epic.getId())
        );

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        Task loadedTask = loadedManager.getTaskById(task.getId());
        Epic loadedEpic = loadedManager.getEpicById(epic.getId());
        Subtask loadedSubtask = loadedManager.getSubtaskById(subtask.getId());

        assertNotNull(loadedTask, "Задача должна быть загружена");
        assertNotNull(loadedEpic, "Эпик должен быть загружен");
        assertNotNull(loadedSubtask, "Подзадача должна быть загружена");

        assertEquals(task.getTitle(), loadedTask.getTitle(),
                "Название задачи должно сохраняться");
        assertEquals(task.getDescription(), loadedTask.getDescription(),
                "Описание задачи должно сохраняться");
        assertEquals(task.getStatus(), loadedTask.getStatus(),
                "Статус задачи должен сохраняться");

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
    }
}