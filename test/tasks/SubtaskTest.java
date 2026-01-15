package tasks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubtaskTest {
    @Test
    void subtaskEqualsById() {
        Subtask s1 = new Subtask("s1", "d1", Status.NEW, 100);
        Subtask s2 = new Subtask("s2", "d2", Status.DONE, 200);
        s1.setId(7);
        s2.setId(7);

        assertEquals(s1, s2, "Subtask должны быть равны, если равен id");
    }
}