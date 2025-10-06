package micheal.must.signuplogin.utils;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class QuotesManager {
    private static final String QUOTES_COLLECTION = "quotes";
    private static final String DAILY_QUOTES_COLLECTION = "daily_quotes";

    private FirebaseFirestore mFirestore;
    private String userId;

    public QuotesManager() {
        mFirestore = FirebaseFirestore.getInstance();
        userId = FirebaseManager.getInstance().getCurrentUserId();
    }

    public void getDailyQuote(OnCompleteListener<DocumentSnapshot> listener) {
        // Get today's date as string (YYYY-MM-DD)
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // 0-based
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String dateKey = year + "-" + month + "-" + day;

        mFirestore.collection(DAILY_QUOTES_COLLECTION)
                .document(dateKey)
                .get()
                .addOnCompleteListener(listener);
    }

    public void getRandomQuote(OnCompleteListener<QuerySnapshot> listener) {
        mFirestore.collection(QUOTES_COLLECTION)
                .get()
                .addOnCompleteListener(listener);
    }

    public String selectRandomQuoteFromList(List<DocumentSnapshot> quotes) {
        if (quotes == null || quotes.isEmpty()) {
            return "Take a deep breath—you've got this.";
        }

        Random random = new Random();
        int index = random.nextInt(quotes.size());
        DocumentSnapshot quoteDoc = quotes.get(index);

        return quoteDoc.getString("text");
    }
}
