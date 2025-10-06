package micheal.must.signuplogin.utils;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class FirebaseManager {
    private static final String TAG = "FirebaseManager";
    private static final String USERS_COLLECTION = "users";

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseStorage mStorage;

    private static FirebaseManager instance;

    private FirebaseManager() {
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public void createUserProfile(String name, String email, OnCompleteListener<Void> listener) {
        String userId = getCurrentUserId();
        if (userId == null) return;

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("email", email);
        userMap.put("createdAt", System.currentTimeMillis());

        mFirestore.collection(USERS_COLLECTION).document(userId)
                .set(userMap)
                .addOnCompleteListener(listener);
    }

    public void updateUserProfile(String name, Uri photoUri, OnCompleteListener<Void> listener) {
        FirebaseUser user = getCurrentUser();
        if (user == null) return;

        UserProfileChangeRequest.Builder profileUpdates = new UserProfileChangeRequest.Builder();

        if (name != null) {
            profileUpdates.setDisplayName(name);
        }

        if (photoUri != null) {
            profileUpdates.setPhotoUri(photoUri);
        }

        user.updateProfile(profileUpdates.build())
                .addOnCompleteListener(listener);
    }

    public void uploadProfilePicture(Uri imageUri, OnCompleteListener<Uri> listener) {
        String userId = getCurrentUserId();
        if (userId == null || imageUri == null) return;

        StorageReference profileRef = mStorage.getReference()
                .child("profile_images")
                .child(userId + ".jpg");

        profileRef.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return profileRef.getDownloadUrl();
                })
                .addOnCompleteListener(listener);
    }

    public void getUserData(OnCompleteListener<DocumentSnapshot> listener) {
        String userId = getCurrentUserId();
        if (userId == null) return;

        mFirestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnCompleteListener(listener);
    }

    public DocumentReference getUserReference() {
        String userId = getCurrentUserId();
        if (userId == null) return null;

        return mFirestore.collection(USERS_COLLECTION).document(userId);
    }

    public void signOut() {
        mAuth.signOut();
    }
}
