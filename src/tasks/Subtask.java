package tasks;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String title, String description, Status status, int epicId) {
        this(title, description, status, epicId, Duration.ZERO, null);
    }

    public Subtask(String title, String description, Status status, int epicId,
                   Duration duration, LocalDateTime startTime) {
        super(title, description, status, duration, startTime);
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
        Subtask copy = new Subtask(this.title, this.description, this.status, this.epicId, this.duration, this.startTime);
        copy.setId(this.id);
        return copy;
    }

}

