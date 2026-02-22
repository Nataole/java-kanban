package manager;

import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        // 1) Создаём две задачи
        Task task1 = manager.createTask(new Task("Задача 1", "Описание 1", Status.NEW));
        Task task2 = manager.createTask(new Task("Задача 2", "Описание 2", Status.IN_PROGRESS));

        // 2) Эпик с тремя подзадачами
        Epic epicWithSubs = manager.createEpic(new Epic("Эпик с подзадачами", "Внутри 3 подзадачи"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Подзадача 1", "Описание 1", Status.NEW, epicWithSubs.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Подзадача 2", "Описание 2", Status.NEW, epicWithSubs.getId()));
        Subtask subtask3 = manager.createSubtask(new Subtask("Подзадача 3", "Описание 3", Status.NEW, epicWithSubs.getId()));

        // 3) Эпик без подзадач
        Epic epicEmpty = manager.createEpic(new Epic("Эпик без подзадач", "Пустой эпик"));

        System.out.println("Создано:");
        System.out.println("  Задача: " + task1);
        System.out.println("  Задача: " + task2);
        System.out.println("  Эпик с подзадачами: " + epicWithSubs.getTitle() + " (id=" + epicWithSubs.getId() + ", статус=" + epicWithSubs.getStatus() + ")");
        System.out.println("  Подзадача: " + subtask1);
        System.out.println("  Подзадача: " + subtask2);
        System.out.println("  Подзадача: " + subtask3);
        System.out.println("  Эпик без подзадач: " + epicEmpty.getTitle() + " (id=" + epicEmpty.getId() + ", статус=" + epicEmpty.getStatus() + ")");

        // 4) Запрашиваем задачи

        System.out.println();
        manager.getTaskById(task1.getId());
        printHistory(manager, "После просмотра задачи 1:");

        manager.getEpicById(epicWithSubs.getId());
        printHistory(manager, "После просмотра эпика с подзадачами:");

        manager.getSubtaskById(subtask1.getId());
        printHistory(manager, "После просмотра подзадачи 1:");

        manager.getTaskById(task2.getId());
        printHistory(manager, "После просмотра задачи 2:");

        manager.getSubtaskById(subtask2.getId());
        printHistory(manager, "После просмотра подзадачи 2:");

        // повторные просмотры
        manager.getTaskById(task1.getId());
        printHistory(manager, "После повторного просмотра задачи 1 (без дублей):");

        manager.getEpicById(epicWithSubs.getId());
        printHistory(manager, "После повторного просмотра эпика с подзадачами (без дублей):");

        // 5) Удаляем задачу, которая есть в истории
        System.out.println();
        System.out.println("Удаляем задачу 2 (id=" + task2.getId() + ")");
        manager.deleteTaskById(task2.getId());
        printHistory(manager, "После удаления задачи 2 (в истории её быть не должно):");

        // 6) Удаляем эпик с тремя подзадачами
        System.out.println();
        System.out.println("Удаляем эпик с подзадачами (id=" + epicWithSubs.getId() + ")");
        manager.deleteEpicById(epicWithSubs.getId());
        printHistory(manager, "После удаления эпика с подзадачами (в истории не должно быть эпика и его подзадач):");

        // эпик без подзадач
        System.out.println();
        manager.getEpicById(epicEmpty.getId());
        printHistory(manager, "После просмотра эпика без подзадач:");
    }

    private static void printHistory(TaskManager manager, String title) {
        System.out.println(title);

        List<Task> history = manager.getHistory();

        if (history.isEmpty()) {
            System.out.println("  История пуста");
            return;
        }

        for (Task task : history) {
            if (task instanceof Subtask) {
                System.out.println("  Подзадача: " + task.getTitle()
                        + " (id=" + task.getId()
                        + ", статус=" + task.getStatus() + ")");
            } else if (task instanceof Epic) {
                Epic epic = (Epic) task;
                if (epic.getSubtaskIds().isEmpty()) {
                    System.out.println("  Эпик без подзадач: " + epic.getTitle()
                            + " (id=" + epic.getId()
                            + ", статус=" + epic.getStatus() + ")");
                } else {
                    System.out.println("  Эпик с подзадачами: " + epic.getTitle()
                            + " (id=" + epic.getId()
                            + ", статус=" + epic.getStatus() + ")");
                }
            } else {
                System.out.println("  Задача: " + task.getTitle()
                        + " (id=" + task.getId()
                        + ", статус=" + task.getStatus() + ")");
            }
        }
    }
}