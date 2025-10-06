package micheal.must.signuplogin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText loginUsername, loginPassword;
    private Button loginButton;
    private TextView toSignup;
    private TextView forgotPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Bind UI elements - updated to match XML IDs
        loginUsername = findViewById(R.id.login_username);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        toSignup = findViewById(R.id.to_signup);
        forgotPassword = findViewById(R.id.forgot_password);

        // Handle system window insets - updated to use the correct ID
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Login button click
        loginButton.setOnClickListener(v -> {
            if (!validateUsername() || !validatePassword()) return;
            checkUser();
        });

        // Redirect to signup activity
        toSignup.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignupActivity.class)));

        // Forgot password functionality
        forgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });
    }

    /** Validate username input */
    private boolean validateUsername() {
        String usernameInput = loginUsername.getText().toString().trim();
        if (usernameInput.isEmpty()) {
            loginUsername.setError("Username is required");
            return false;
        } else {
            loginUsername.setError(null);
            return true;
        }
    }

    /** Validate password input */
    private boolean validatePassword() {
        String passwordInput = loginPassword.getText().toString().trim();
        if (passwordInput.isEmpty()) {
            loginPassword.setError("Password is required");
            return false;
        } else {
            loginPassword.setError(null);
            return true;
        }
    }

    /** Check if user exists and authenticate */
    private void checkUser() {
        String userUsername = loginUsername.getText().toString().trim();
        String userPassword = loginPassword.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query query = reference.orderByChild("username").equalTo(userUsername);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    loginUsername.setError("User does not exist");
                    loginUsername.requestFocus();
                    return;
                }

                DataSnapshot userSnapshot = snapshot.getChildren().iterator().next();
                String passwordFromDB = userSnapshot.child("password").getValue(String.class);
                String nameFromDB = userSnapshot.child("name").getValue(String.class);
                String emailFromDB = userSnapshot.child("email").getValue(String.class);

                if (passwordFromDB != null && passwordFromDB.equals(userPassword)) {
                    // Password matches, now create a Firebase Auth session
                    if (emailFromDB != null && !emailFromDB.isEmpty()) {
                        createFirebaseAuthSession(emailFromDB, userPassword, nameFromDB, userUsername);
                    } else {
                        // If no email found, use a generated email based on username
                        // This is a workaround since Firebase Auth requires email format
                        String generatedEmail = userUsername + "@mindmate.app";
                        createFirebaseAuthSession(generatedEmail, userPassword, nameFromDB, userUsername);
                    }
                } else {
                    loginPassword.setError("Invalid credentials");
                    loginPassword.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Creates a Firebase Auth session using email and password
     */
    private void createFirebaseAuthSession(String email, String password, String name, String username) {
        // First check if user exists in Firebase Auth
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        FirebaseUser user = mAuth.getCurrentUser();
                        proceedToDashboard(user, name, username);
                    } else {
                        // If sign in fails, user might not exist in Firebase Auth yet
                        // Try to create the user
                        createNewFirebaseUser(email, password, name, username);
                    }
                });
    }

    /**
     * Creates a new Firebase Auth user if one doesn't exist
     */
    private void createNewFirebaseUser(String email, String password, String name, String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // User created successfully, now sign in
                        FirebaseUser user = mAuth.getCurrentUser();
                        proceedToDashboard(user, name, username);
                    } else {
                        // If creation fails, show an error message
                        Toast.makeText(LoginActivity.this,
                                "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Proceeds to dashboard after successful authentication
     */
    private void proceedToDashboard(FirebaseUser user, String name, String username) {
        if (user != null) {
            Toast.makeText(LoginActivity.this, "Welcome, " + name, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("name", name);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(LoginActivity.this, "Authentication error", Toast.LENGTH_SHORT).show();
        }
    }
}