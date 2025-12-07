package tasks;

import java.util.ArrayList;
import java.util.List;

    public class Epic extends Task {
        private final List<Integer> subtaskIds;

        public Epic(String title, String description) {
            super(title, description, Status.NEW);
            this.subtaskIds = new ArrayList<>();
        }

        public List<Integer> getSubtaskIds() {
            return new ArrayList<>(subtaskIds);
        }

        public void addSubtaskId(int subtaskId) {
            subtaskIds.add(subtaskId);
        }

        public void removeSubtaskId(int subtaskId) {
            subtaskIds.remove((Integer) subtaskId);
        }

        public void clearSubtaskIds() {
            subtaskIds.clear();
        }

        @Override
        public String toString() {
            return "Epic{" +
                    "id=" + id +
                    ", title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    ", status=" + status +
                    ", subtaskIds=" + subtaskIds +
                    '}';
        }
    }

