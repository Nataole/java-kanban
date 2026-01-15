package tasks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EpicTest {
    @Test
    void epicEqualsById() {
        Epic e1 = new Epic("e1", "d1");
        Epic e2 = new Epic("e2", "d2");
        e1.setId(10);
        e2.setId(10);

        assertEquals(e1, e2, "Epic должны быть равны, если равен id");
    }
}