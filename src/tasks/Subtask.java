package tasks;

public class Subtask extends Task {
        private final int epicId;

        public Subtask(String title, String description, Status status, int epicId) {
            super(title, description, status);
            this.epicId = epicId;
        }

        public int getEpicId() {
            return epicId;
        }



        @Override
        public String toString() {
            return "Subtask{" +
                    "id=" + id +
                    ", title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    ", status=" + status +
                    ", epicId=" + epicId +
                    '}';
        }

    @Override
    public Subtask copy() {
        Subtask copy = new Subtask(this.title, this.description, this.status, this.epicId);
        copy.setId(this.id);
        return copy;
    }

}

