package manager;

import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTaskManager(File file) {
        super(Managers.getDefaultHistory());
        this.file = file;
    }

    private void save() {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("id,type,name,status,description,epic,startTime,duration\n");

            for (Task task : getAllTasks()) {
                builder.append(toString(task)).append("\n");
            }

            for (Epic epic : getAllEpics()) {
                builder.append(toString(epic)).append("\n");
            }

            for (Subtask subtask : getAllSubtasks()) {
                builder.append(toString(subtask)).append("\n");
            }

            Files.writeString(file.toPath(), builder.toString());
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл", e);
        }
    }

    private String toString(Task task) {
        TaskType type;
        String epicId;

        if (task instanceof Epic) {
            type = TaskType.EPIC;
            epicId = "";
        } else if (task instanceof Subtask) {
            type = TaskType.SUBTASK;
            epicId = String.valueOf(((Subtask) task).getEpicId());
        } else {
            type = TaskType.TASK;
            epicId = "";
        }

        String startTime = task.getStartTime() == null ? "" : task.getStartTime().toString();
        String duration = task.getDuration() == null ? "" : String.valueOf(task.getDuration().toMinutes());

        return task.getId() + "," +
                type + "," +
                task.getTitle() + "," +
                task.getStatus() + "," +
                task.getDescription() + "," +
                epicId + "," +
                startTime + "," +
                duration;
    }

    private static Task fromString(String value) {
        String[] fields = value.split(",", -1);

        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];
        String epicField = fields[5];

        LocalDateTime startTime = fields.length > 6 && !fields[6].isBlank()
                ? LocalDateTime.parse(fields[6])
                : null;

        Duration duration = fields.length > 7 && !fields[7].isBlank()
                ? Duration.ofMinutes(Long.parseLong(fields[7]))
                : Duration.ZERO;

        switch (type) {
            case TASK:
                Task task = new Task(name, description, status, duration, startTime);
                task.setId(id);
                return task;

            case EPIC:
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStatus(status);
                epic.setDuration(duration);
                epic.setStartTime(startTime);
                return epic;

            case SUBTASK:
                int epicId = Integer.parseInt(epicField);
                Subtask subtask = new Subtask(name, description, status, epicId, duration, startTime);
                subtask.setId(id);
                return subtask;

            default:
                throw new ManagerSaveException("Неизвестный тип задачи в файле: " + type);
        }
    }

    private Task createTaskWithoutSave(Task task) {
        return super.createTask(task);
    }

    private Epic createEpicWithoutSave(Epic epic) {
        return super.createEpic(epic);
    }

    private Subtask createSubtaskWithoutSave(Subtask subtask) {
        return super.createSubtask(subtask);
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try {
            List<String> lines = Files.readAllLines(file.toPath());

            if (lines.size() <= 1) {
                return manager;
            }

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);

                if (line.isBlank()) {
                    continue;
                }

                Task task = fromString(line);

                if (task instanceof Epic) {
                    manager.createEpicWithoutSave((Epic) task);
                } else if (task instanceof Subtask) {
                    manager.createSubtaskWithoutSave((Subtask) task);
                } else {
                    manager.createTaskWithoutSave(task);
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла", e);
        }

        return manager;
    }

    @Override
    public Task createTask(Task task) {
        Task result = super.createTask(task);
        save();
        return result;
    }

    @Override
    public Task updateTask(Task task) {
        Task result = super.updateTask(task);
        save();
        return result;
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic result = super.createEpic(epic);
        save();
        return result;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic result = super.updateEpic(epic);
        save();
        return result;
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask result = super.createSubtask(subtask);
        save();
        return result;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        Subtask result = super.updateSubtask(subtask);
        save();
        return result;
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    public static void main(String[] args) {
        File file = new File("tasks.csv");

        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task1 = manager.createTask(new Task(
                "Задача 1",
                "Описание задачи 1",
                Status.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.of(2026, 4, 1, 10, 0)));
        Task task2 = manager.createTask(new Task("Задача 2",
                "Описание задачи 2",
                Status.IN_PROGRESS,
                Duration.ofMinutes(45),
                LocalDateTime.of(2026, 4, 1, 11, 0)));

        Epic epic1 = manager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        Epic epic2 = manager.createEpic(new Epic("Эпик 2", "Описание эпика 2"));

        Subtask subtask1 = manager.createSubtask(
                new Subtask("Подзадача 1",
                        "Описание подзадачи 1",
                        Status.NEW,
                        epic1.getId(),
                        Duration.ofMinutes(20),
                        LocalDateTime.of(2026, 4, 1, 12, 0)));
        Subtask subtask2 = manager.createSubtask(
                new Subtask("Подзадача 2",
                        "Описание подзадачи 2",
                        Status.DONE,
                        epic1.getId(),
                        Duration.ofMinutes(30),
                        LocalDateTime.of(2026, 4, 1, 13, 0)));
        Subtask subtask3 = manager.createSubtask(
                new Subtask("Подзадача 3",
                        "Описание подзадачи 3",
                        Status.IN_PROGRESS,
                        epic2.getId(),
                        Duration.ofMinutes(25),
                        LocalDateTime.of(2026, 4, 1, 14, 0)));

        System.out.println("Исходный менеджер:");
        System.out.println("Задачи: " + manager.getAllTasks());
        System.out.println("Эпики: " + manager.getAllEpics());
        System.out.println("Подзадачи: " + manager.getAllSubtasks());

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        System.out.println();
        System.out.println("Загруженный менеджер:");
        System.out.println("Задачи: " + loadedManager.getAllTasks());
        System.out.println("Эпики: " + loadedManager.getAllEpics());
        System.out.println("Подзадачи: " + loadedManager.getAllSubtasks());

        System.out.println();
        System.out.println("Проверка:");
        System.out.println("Задача 1 есть: " + (loadedManager.getTaskById(task1.getId()) != null));
        System.out.println("Задача 2 есть: " + (loadedManager.getTaskById(task2.getId()) != null));
        System.out.println("Эпик 1 есть: " + (loadedManager.getEpicById(epic1.getId()) != null));
        System.out.println("Эпик 2 есть: " + (loadedManager.getEpicById(epic2.getId()) != null));
        System.out.println("Подзадача 1 есть: " + (loadedManager.getSubtaskById(subtask1.getId()) != null));
        System.out.println("Подзадача 2 есть: " + (loadedManager.getSubtaskById(subtask2.getId()) != null));
        System.out.println("Подзадача 3 есть: " + (loadedManager.getSubtaskById(subtask3.getId()) != null));
    }
}