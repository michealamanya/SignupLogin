package micheal.must.signuplogin.utils;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.Date;

import micheal.must.signuplogin.models.MeditationSession;

public class SessionManager {
    private static final String SESSIONS_COLLECTION = "meditation_sessions";
    private static final String USERS_COLLECTION = "users";

    private FirebaseFirestore mFirestore;
    private String userId;

    public SessionManager() {
        mFirestore = FirebaseFirestore.getInstance();
        userId = FirebaseManager.getInstance().getCurrentUserId();
    }

    public void saveSession(MeditationSession session, OnCompleteListener<DocumentReference> listener) {
        if (userId == null) return;

        mFirestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(SESSIONS_COLLECTION)
                .add(session)
                .addOnCompleteListener(listener);
    }

    public void getTodaySessions(OnCompleteListener<QuerySnapshot> listener) {
        if (userId == null) return;

        // Start of today
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date startOfDay = calendar.getTime();

        // End of today
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date endOfDay = calendar.getTime();

        mFirestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(SESSIONS_COLLECTION)
                .whereGreaterThanOrEqualTo("timestamp", startOfDay)
                .whereLessThanOrEqualTo("timestamp", endOfDay)
                .get()
                .addOnCompleteListener(listener);
    }

    public void getRecentSessions(int limit, OnCompleteListener<QuerySnapshot> listener) {
        if (userId == null) return;

        mFirestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(SESSIONS_COLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnCompleteListener(listener);
    }
}
