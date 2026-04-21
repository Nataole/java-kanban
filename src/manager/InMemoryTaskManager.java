package manager;

import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class InMemoryTaskManager implements TaskManager {

    private int nextId = 1;

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();

    private final HistoryManager historyManager;

    private final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime)
                    .thenComparing(Task::getId)
    );

    private void addToPrioritizedTasks(Task task) {
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    private void removeFromPrioritizedTasks(Task task) {
        if (task != null && task.getStartTime() != null) {
            prioritizedTasks.remove(task);
        }
    }

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    public boolean isTasksOverlap(Task first, Task second) {
        if (first.getStartTime() == null || second.getStartTime() == null) {
            return false;
        }
        if (first.getEndTime() == null || second.getEndTime() == null) {
            return false;
        }

        return first.getStartTime().isBefore(second.getEndTime())
                && second.getStartTime().isBefore(first.getEndTime());
    }

    private boolean hasOverlap(Task task) {
        return prioritizedTasks.stream()
                .filter(existing -> existing.getId() != task.getId())
                .anyMatch(existing -> isTasksOverlap(existing, task));
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    //История

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    // Методы для задач

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        for (Task task : tasks.values()) {
            removeFromPrioritizedTasks(task);
            historyManager.remove(task.getId());
        }
        tasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    private int generateIdIfNeeded(int id) {
        if (id != 0) {
            if (tasks.containsKey(id) || epics.containsKey(id) || subtasks.containsKey(id)) {
                throw new IllegalArgumentException("ID уже занят: " + id);
            }
            nextId = Math.max(nextId, id + 1);
            return id;
        }
        return nextId++;
    }

    @Override
    public Task createTask(Task task) {
        task.setId(generateIdIfNeeded(task.getId()));

        if (task.getStartTime() != null && hasOverlap(task)) {
            throw new IllegalArgumentException("Задача пересекается по времени с другой задачей");
        }

        Task copy = task.copy();
        tasks.put(copy.getId(), copy);
        addToPrioritizedTasks(copy);
        return task;
    }

    @Override
    public Task updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            Task oldTask = tasks.get(task.getId());
            removeFromPrioritizedTasks(oldTask);

            if (task.getStartTime() != null && hasOverlap(task)) {
                addToPrioritizedTasks(oldTask);
                throw new IllegalArgumentException("Задача пересекается по времени с другой задачей");
            }

            Task copy = task.copy();
            tasks.put(task.getId(), copy);
            addToPrioritizedTasks(copy);
            return task;
        }
        return null;
    }


    @Override
    public void deleteTaskById(int id) {
        Task task = tasks.remove(id);
        removeFromPrioritizedTasks(task);
        historyManager.remove(id);
    }

    // Методы для эпиков

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
        for (Epic epic : epics.values()) {
            historyManager.remove(epic.getId());
        }

        for (Subtask subtask : subtasks.values()) {
            removeFromPrioritizedTasks(subtask);
            historyManager.remove(subtask.getId());
        }
        epics.clear();
        subtasks.clear();
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }


    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(generateIdIfNeeded(epic.getId()));
        Epic copy = epic.copy();
        epics.put(copy.getId(), copy);
        return epic;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            Epic existing = epics.get(epic.getId());
            existing.setTitle(epic.getTitle());
            existing.setDescription(epic.getDescription());
            return existing;
        }
        return null;
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        historyManager.remove(id);
        if (epic != null) {
            for (int subtaskId : epic.getSubtaskIds()) {
                Subtask subtask = subtasks.remove(subtaskId);
                removeFromPrioritizedTasks(subtask);
                historyManager.remove(subtaskId);
            }
        }
    }

    // Методы для подзадач

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllSubtasks() {
        for (Subtask subtask : subtasks.values()) {
            removeFromPrioritizedTasks(subtask);
            historyManager.remove(subtask.getId());
        }
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtaskIds();
            updateEpicStatus(epic.getId());
        }
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            return null;
        }

        if (subtask.getId() != 0 && subtask.getId() == subtask.getEpicId()) {
            throw new IllegalArgumentException("Подзадача не может быть своим же эпиком");
        }

        subtask.setId(generateIdIfNeeded(subtask.getId()));

        if (subtask.getStartTime() != null && hasOverlap(subtask)) {
            throw new IllegalArgumentException("Подзадача пересекается по времени с другой задачей");
        }

        Subtask copy = subtask.copy();
        subtasks.put(copy.getId(), copy);
        addToPrioritizedTasks(copy);

        epic.addSubtaskId(copy.getId());
        updateEpicStatus(epic.getId());

        return subtask;
    }


    @Override
    public Subtask updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            Subtask oldSubtask = subtasks.get(subtask.getId());
            removeFromPrioritizedTasks(oldSubtask);

            if (subtask.getStartTime() != null && hasOverlap(subtask)) {
                addToPrioritizedTasks(oldSubtask);
                throw new IllegalArgumentException("Подзадача пересекается по времени с другой задачей");
            }

            Subtask copy = subtask.copy();
            subtasks.put(subtask.getId(), copy);
            addToPrioritizedTasks(copy);
            updateEpicStatus(subtask.getEpicId());

            return subtask;
        }
        return null;
    }


    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        removeFromPrioritizedTasks(subtask);
        historyManager.remove(id);

        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic.getId());
            }
        }
    }

    // Дополнительные методы

    @Override
    public List<Subtask> getSubtasksByEpicId(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return new ArrayList<>();
        }

        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .toList();
    }

    // Вспомогательные методы

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }


        List<Subtask> epicSubtasks = getSubtasksByEpicId(epicId);
        Duration totalDuration = Duration.ZERO;
        LocalDateTime minStartTime = null;
        LocalDateTime maxEndTime = null;

        if (epicSubtasks.isEmpty()) {
            epic.setStatus(Status.NEW);
            epic.setDuration(Duration.ZERO);
            epic.setStartTime(null);
            epic.setEndTime(null);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Subtask subtask : epicSubtasks) {
            if (subtask.getDuration() != null) {
                totalDuration = totalDuration.plus(subtask.getDuration());
            }
            if (subtask.getStartTime() != null) {
                if (minStartTime == null || subtask.getStartTime().isBefore(minStartTime)) {
                    minStartTime = subtask.getStartTime();
                }

                LocalDateTime subtaskEndTime = subtask.getEndTime();
                if (subtaskEndTime != null && (maxEndTime == null || subtaskEndTime.isAfter(maxEndTime))) {
                    maxEndTime = subtaskEndTime;
                }
            }
            if (subtask.getStatus() == Status.IN_PROGRESS) {
                epic.setStatus(Status.IN_PROGRESS);
                epic.setDuration(totalDuration);
                epic.setStartTime(minStartTime);
                epic.setEndTime(maxEndTime);
                return;
            }
            if (subtask.getStatus() != Status.NEW) {
                allNew = false;
            }
            if (subtask.getStatus() != Status.DONE) {
                allDone = false;
            }

        }

        epic.setDuration(totalDuration);
        epic.setStartTime(minStartTime);
        epic.setEndTime(maxEndTime);

        if (allDone) {
            epic.setStatus(Status.DONE);

        } else if (allNew) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

}