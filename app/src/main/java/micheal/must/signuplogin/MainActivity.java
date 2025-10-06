package micheal.must.signuplogin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Button buttonSignup, buttonLogin, buttonGoogle;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;

    // ActivityResultLauncher to replace startIntentSenderForResult
    private final ActivityResultLauncher<IntentSenderRequest> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    try {
                        SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
                        String idToken = credential.getGoogleIdToken();
                        if (idToken != null) {
                            firebaseAuthWithGoogle(idToken);
                        }
                    } catch (ApiException e) {
                        Log.e(TAG, "Sign-in failed: ", e);
                        Toast.makeText(MainActivity.this, "Sign-In Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Handle system window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("users");

        // Initialize One Tap client
        oneTapClient = Identity.getSignInClient(this);

        // Configure One Tap Sign-In request
        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.default_web_client_id)) // From google-services.json
                        .setFilterByAuthorizedAccounts(false) // Allow choosing other accounts
                        .build())
                .setAutoSelectEnabled(false)
                .build();

        // Bind UI elements
        buttonSignup = findViewById(R.id.button_signup);
        buttonLogin = findViewById(R.id.button_login);
        buttonGoogle = findViewById(R.id.button_google);

        // Set click listeners
        buttonSignup.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SignupActivity.class)));
        buttonLogin.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, LoginActivity.class)));
        buttonGoogle.setOnClickListener(v -> signInWithGoogle());

        // Check if user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User already signed in, proceed to dashboard
            proceedToDashboard(currentUser);
        }
    }

    /** Start Google One Tap sign-in using ActivityResultLauncher */
    private void signInWithGoogle() {
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this, result -> {
                    try {
                        IntentSenderRequest intentSenderRequest =
                                new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender())
                                        .build();
                        signInLauncher.launch(intentSenderRequest);
                    } catch (Exception e) {
                        Log.e(TAG, "Google One Tap Sign-In failed", e);
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Sign-In request failed", e);
                    Toast.makeText(this, "Google Sign-In failed.", Toast.LENGTH_SHORT).show();
                });
    }

    /** Authenticate with Firebase using Google ID token */
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Save user data and proceed to dashboard
                            saveUserDataAndProceed(user);
                        }
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(MainActivity.this, "Firebase Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** Save user data and proceed to dashboard */
    private void saveUserDataAndProceed(FirebaseUser user) {
        // Save user data to Firebase Database
        databaseRef.child(user.getUid()).child("name").setValue(user.getDisplayName());
        databaseRef.child(user.getUid()).child("email").setValue(user.getEmail());

        // Add last login timestamp
        databaseRef.child(user.getUid()).child("lastLogin").setValue(System.currentTimeMillis());

        // Proceed to dashboard
        proceedToDashboard(user);
    }

    /** Navigate to dashboard */
    private void proceedToDashboard(FirebaseUser user) {
        Toast.makeText(this, "Welcome, " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
        startActivity(new Intent(MainActivity.this, DashboardActivity.class));
        finish(); // Close this activity so the user can't go back
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in and update UI accordingly
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            proceedToDashboard(currentUser);
        }
    }
}

