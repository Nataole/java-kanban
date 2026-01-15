package manager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ManagersTest {

    @Test
    void getDefaultReturnsInitializedTaskManager() {
        TaskManager manager = Managers.getDefault();

        assertNotNull(manager,
                "Managers.getDefault() не должен возвращать null");
    }

    @Test
    void getDefaultHistoryReturnsInitializedHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        assertNotNull(historyManager,
                "Managers.getDefaultHistory() не должен возвращать null");
        assertNotNull(historyManager.getHistory(),
                "HistoryManager должен быть готов к работе (getHistory != null)");
    }
}