package tasks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TaskTest {
    @Test
    void taskEqualsById() {
        Task t1 = new Task("t1", "d1", Status.NEW);
        Task t2 = new Task("t2", "d2", Status.DONE);

        t1.setId(1);
        t2.setId(1);

        assertEquals(t1, t2, "Task должны быть равны, если равен id");
        assertEquals(t1.hashCode(), t2.hashCode(), "hashCode должен совпадать при равных id");
    }

    @Test
    void tasksNotEqualIfDifferentId() {
        Task t1 = new Task("t1", "d1", Status.NEW);
        Task t2 = new Task("t2", "d2", Status.NEW);

        t1.setId(1);
        t2.setId(2);

        assertNotEquals(t1, t2, "Task не должны быть равны при разных id");
    }
}