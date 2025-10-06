package micheal.must.signuplogin.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class MoodEntry {
    @DocumentId
    private String id;
    private int moodScore; // 1-5 scale
    private String note;
    private String emoji;
    @ServerTimestamp
    private Date timestamp;

    // No-args constructor needed for Firestore
    public MoodEntry() {}

    public MoodEntry(int moodScore, String note, String emoji) {
        this.moodScore = moodScore;
        this.note = note;
        this.emoji = emoji;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMoodScore() {
        return moodScore;
    }

    public void setMoodScore(int moodScore) {
        this.moodScore = moodScore;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    // Helper method to get emoji based on mood score
    public static String getEmojiForMoodScore(int score) {
        switch (score) {
            case 1: return "😔";
            case 2: return "😕";
            case 3: return "😐";
            case 4: return "🙂";
            case 5: return "😊";
            default: return "😐";
        }
    }
}
