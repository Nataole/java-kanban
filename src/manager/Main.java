package manager;

import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.util.List;

public class Main {

    public static void main(String[] args) {


        TaskManager manager = Managers.getDefault();

        // 1. Создаем две обычные задачи
        Task task1 = manager.createTask(new Task("Задача 1", "Описание задачи 1", Status.NEW));
        Task task2 = manager.createTask(new Task("Задача 2", "Описание задачи 2", Status.IN_PROGRESS));

        System.out.println("Созданы задачи:");
        System.out.println("  " + task1);
        System.out.println("  " + task2);

        // 2. Создаем эпики
        Epic epic1 = manager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        Epic epic2 = manager.createEpic(new Epic("Эпик 2", "Описание эпика 2"));

        // 3. Создаем подзадачи
        Subtask subtask1 = manager.createSubtask(new Subtask("Подзадача 1", "Описание подзадачи 1", Status.NEW, epic1.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Подзадача 2", "Описание подзадачи 2", Status.NEW, epic1.getId()));
        Subtask subtask3 = manager.createSubtask(new Subtask("Подзадача 3", "Описание подзадачи 3", Status.NEW, epic2.getId()));

        System.out.println("Созданы подзадачи:");
        System.out.println("  " + subtask1);
        System.out.println("  " + subtask2);
        System.out.println("  " + subtask3);

        // 4. Проверяем статусы эпиков
        System.out.println("Статус эпика 1: " + manager.getEpicById(epic1.getId()).getStatus() + " (должен быть NEW)");
        System.out.println("Статус эпика 2: " + manager.getEpicById(epic2.getId()).getStatus() + " (должен быть NEW)");

        // 5. Обновляем подзадачи
        System.out.println("Обновление подзадач");
        subtask1.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(subtask1);

        System.out.println("После изменения статуса подзадачи 1 на IN_PROGRESS:");
        System.out.println("Статус эпика 1: " + manager.getEpicById(epic1.getId()).getStatus() + " (должен быть IN_PROGRESS)");

        // 6. Завершаем все подзадачи эпика 1
        System.out.println("Завершение подзадач эпика 1");
        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.DONE);
        manager.updateSubtask(subtask1);
        manager.updateSubtask(subtask2);

        System.out.println("После завершения всех подзадач эпика 1:");
        System.out.println("Статус эпика 1: " + manager.getEpicById(epic1.getId()).getStatus() + " (должен быть DONE)");

        // 7. Получаем подзадачи эпика
        List<Subtask> epic1Subtasks = manager.getSubtasksByEpicId(epic1.getId());
        System.out.println("Подзадачи эпика 1:");
        for (Subtask subtask : epic1Subtasks) {
            System.out.println("  " + subtask);
        }

        // 8. Удаляем задачи
        manager.deleteTaskById(task1.getId());
        manager.deleteEpicById(epic2.getId());

        System.out.println("После удаления:");
        System.out.println("Задачи: " + manager.getAllTasks().size());
        System.out.println("Эпики: " + manager.getAllEpics().size());
        System.out.println("Подзадачи: " + manager.getAllSubtasks().size());
    }
}