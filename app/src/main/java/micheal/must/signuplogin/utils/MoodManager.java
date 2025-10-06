package micheal.must.signuplogin.utils;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.Date;

import micheal.must.signuplogin.models.MoodEntry;

public class MoodManager {
    private static final String MOODS_COLLECTION = "moods";

    private FirebaseFirestore mFirestore;
    private String userId;

    public MoodManager() {
        mFirestore = FirebaseFirestore.getInstance();
        userId = FirebaseManager.getInstance().getCurrentUserId();
    }

    public void saveMoodEntry(MoodEntry entry, OnCompleteListener<DocumentReference> listener) {
        if (userId == null) return;

        mFirestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(MOODS_COLLECTION)
                .add(entry)
                .addOnCompleteListener(listener);
    }

    public void getMoodHistoryForWeek(OnCompleteListener<QuerySnapshot> listener) {
        if (userId == null) return;

        // Get date 7 days ago
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date weekAgo = calendar.getTime();

        mFirestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(MOODS_COLLECTION)
                .whereGreaterThanOrEqualTo("timestamp", weekAgo)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(listener);
    }

    public void getMoodForDay(Date day, OnCompleteListener<QuerySnapshot> listener) {
        if (userId == null) return;

        // Start of day
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(day);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date startOfDay = calendar.getTime();

        // End of day
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date endOfDay = calendar.getTime();

        mFirestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(MOODS_COLLECTION)
                .whereGreaterThanOrEqualTo("timestamp", startOfDay)
                .whereLessThanOrEqualTo("timestamp", endOfDay)
                .get()
                .addOnCompleteListener(listener);
    }

    public void getAverageMoodScore(OnCompleteListener<QuerySnapshot> listener) {
        if (userId == null) return;

        mFirestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(MOODS_COLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10) // Last 10 mood entries
                .get()
                .addOnCompleteListener(listener);
    }

    private static final String USERS_COLLECTION = "users";
}
