package micheal.must.signuplogin.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class MeditationSession {
    @DocumentId
    private String id;
    private String title;
    private int durationMinutes;
    private boolean completed;
    @ServerTimestamp
    private Date timestamp;

    // No-args constructor needed for Firestore
    public MeditationSession() {}

    public MeditationSession(String title, int durationMinutes, boolean completed) {
        this.title = title;
        this.durationMinutes = durationMinutes;
        this.completed = completed;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
