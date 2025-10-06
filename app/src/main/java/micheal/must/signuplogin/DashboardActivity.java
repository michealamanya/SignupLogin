package micheal.must.signuplogin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    // UI Components
    private TextView tvGreeting, tvGreetingSubtext, tvDailyQuote;
    private TextView tvMoodScore, tvTipsCount, tvSessionsTime;
    private ShapeableImageView ivProfile, ivSettings;
    private CardView chatbotCard, moodCard, tipsCard, sessionCard;
    private MaterialButton btnCheckin, btnJournal, btnMeditation, btnResources;
    private RecyclerView rvRecommended;
    private BottomNavigationView bottomNavigation;
    private LinearProgressIndicator moodProgress;
    private FloatingActionButton fabCaptureMood;
    private ShapeableImageView ivMoodImage;

    // Data
    private List<RecommendedItem> recommendedItems;

    // Mood detection with TensorFlow Lite
    private static final String TAG = "MoodDetection";
    private static final String MODEL_PATH = "model.tflite";
    private static final String LABELS_PATH = "labels.txt";
    private Interpreter tflite;
    private List<String> labels;
    private int modelInputWidth = 224;
    private int modelInputHeight = 224;

    // Image capture
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        // Initialize UI components
        initViews();

        // Initialize TensorFlow Lite model
        try {
            initializeModel();
        } catch (IOException e) {
            Log.e(TAG, "Error initializing TFLite model: " + e.getMessage());
            Toast.makeText(this, "Error loading mood detection model", Toast.LENGTH_SHORT).show();
        }

        // Initialize activity result launchers
        initActivityResultLaunchers();

        // Set up greeting based on time of day
        setGreeting();

        // Load motivational quote
        loadDailyQuote();

        // Set up click listeners
        setupClickListeners();

        // Set up bottom navigation
        setupBottomNavigation();

        // Load recommended items
        loadRecommendedItems();

        // Initialize statistics
        initializeStats();
    }

    private void initViews() {
        // TextViews
        tvGreeting = findViewById(R.id.tv_greeting);
        tvGreetingSubtext = findViewById(R.id.tv_greeting_subtext);
        tvDailyQuote = findViewById(R.id.tv_daily_quote);
        tvMoodScore = findViewById(R.id.tv_mood_score);
        tvTipsCount = findViewById(R.id.tv_tips_count);
        tvSessionsTime = findViewById(R.id.tv_sessions_time);

        // ImageViews
        ivProfile = findViewById(R.id.iv_profile);
        ivSettings = findViewById(R.id.iv_settings);
        ivMoodImage = findViewById(R.id.iv_mood_image);

        // Cards
        chatbotCard = findViewById(R.id.chatbot_card);
        moodCard = findViewById(R.id.mood_card);
        tipsCard = findViewById(R.id.tips_card);
        sessionCard = findViewById(R.id.session_card);

        // Buttons
        btnCheckin = findViewById(R.id.button_checkin);
        btnJournal = findViewById(R.id.button_journal);
        btnMeditation = findViewById(R.id.button_meditation);
        btnResources = findViewById(R.id.button_resources);
        fabCaptureMood = findViewById(R.id.fab_capture_mood);

        // Progress indicators
        moodProgress = findViewById(R.id.mood_progress);

        // RecyclerView
        rvRecommended = findViewById(R.id.rv_recommended);

        // Bottom Navigation
        bottomNavigation = findViewById(R.id.bottom_navigation);
    }

    private void initializeModel() throws IOException {
        // Load the TFLite model
        MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(this, MODEL_PATH);
        tflite = new Interpreter(tfliteModel);

        // Load labels
        labels = loadLabelsFromAsset(LABELS_PATH);

        Log.d(TAG, "Model loaded successfully with " + labels.size() + " labels");
    }

    private List<String> loadLabelsFromAsset(String filePath) throws IOException {
        List<String> labelList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open(filePath)));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }

    private void initActivityResultLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        analyzeMood(imageBitmap);
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                            analyzeMood(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setGreeting() {
        // Get user's name (from intent or Firebase Auth)
        String userName = getUserName();

        // Get the current hour
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (hourOfDay < 12) {
            greeting = "Good Morning, " + userName + "!";
        } else if (hourOfDay < 17) {
            greeting = "Good Afternoon, " + userName + "!";
        } else {
            greeting = "Good Evening, " + userName + "!";
        }

        tvGreeting.setText(greeting);
    }

    /**
     * Gets the current user's name from various sources
     * @return The user's first name or "Friend" if not available
     */
    private String getUserName() {
        // Try to get name from intent extras first
        String name = getIntent().getStringExtra("name");

        // If name not in intent, try to get from Firebase Auth
        if (name == null || name.isEmpty()) {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null && auth.getCurrentUser().getDisplayName() != null) {
                name = auth.getCurrentUser().getDisplayName();
            }
        }

        // Extract first name if full name is provided
        if (name != null && !name.isEmpty()) {
            // Split by space and get first part as first name
            String[] parts = name.split(" ");
            return parts[0];
        }

        // Default if no name is found
        return "Friend";
    }

    private void loadDailyQuote() {
        // In a real app, this could come from a remote source or database
        String[] quotes = {
                "Take a deep breath—you've got this.",
                "Every step forward is progress, no matter how small.",
                "Self-care isn't selfish, it's necessary.",
                "Your feelings are valid, but they don't define you.",
                "Today is a new opportunity to grow and heal."
        };

        // Simple random selection
        int randomIndex = (int) (Math.random() * quotes.length);
        tvDailyQuote.setText(quotes[randomIndex]);
    }

    private void initializeStats() {
        // Set mood score emoji based on progress
        int moodValue = moodProgress.getProgress();
        if (moodValue >= 75) {
            tvMoodScore.setText("😊");
        } else if (moodValue >= 50) {
            tvMoodScore.setText("😐");
        } else {
            tvMoodScore.setText("😔");
        }

        // Update tip count - could be from preferences or remote data
        int newTips = 3; // Example value
        tvTipsCount.setText(newTips + " New");

        // Update session time - could be calculated from user's schedule
        tvSessionsTime.setText("45:00"); // Example value
    }

    private void setupClickListeners() {
        // Profile click
        ivProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Opening profile", Toast.LENGTH_SHORT).show();
            // Navigate to profile screen
        });

        // Settings click
        ivSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Opening settings", Toast.LENGTH_SHORT).show();
            navigateToMoreOptions(); // Navigate to MoreOptionsActivity
        });

        // Chatbot card click
        chatbotCard.setOnClickListener(v -> {
            // Open chat interface via bottom navigation
            bottomNavigation.setSelectedItemId(R.id.nav_chat);
        });

        // Mood card click
        moodCard.setOnClickListener(v -> {
            showMoodCaptureOptions();
        });

        // Capture mood FAB
        fabCaptureMood.setOnClickListener(v -> {
            showMoodCaptureOptions();
        });

        // Tips card click
        tipsCard.setOnClickListener(v -> {
            showDailyTips();
        });

        // Session card click
        sessionCard.setOnClickListener(v -> {
            Toast.makeText(this, "Starting meditation session", Toast.LENGTH_SHORT).show();
            // Start or schedule a session
        });

        // Quick action buttons
        btnCheckin.setOnClickListener(v -> {
            Toast.makeText(this, "Starting daily check-in", Toast.LENGTH_SHORT).show();
            // Launch check-in flow
        });

        btnJournal.setOnClickListener(v -> {
            // Navigate to journal screen via bottom navigation
            bottomNavigation.setSelectedItemId(R.id.nav_journal);
        });

        btnMeditation.setOnClickListener(v -> {
            Toast.makeText(this, "Opening meditation library", Toast.LENGTH_SHORT).show();
            // Open meditation screen
        });

        btnResources.setOnClickListener(v -> {
            Toast.makeText(this, "Opening resources", Toast.LENGTH_SHORT).show();
            // Open resources screen
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Already on home, refresh data
                refreshDashboardData();
                return true;
            } else if (itemId == R.id.nav_chat) {
                // Navigate to chat screen
                navigateToChatScreen();
                return true;
            } else if (itemId == R.id.nav_journal) {
                // Navigate to journal screen
                navigateToJournalScreen();
                return true;
            } else if (itemId == R.id.nav_community) {
                // Navigate to community screen
                navigateToCommunityScreen();
                return true;
            } else if (itemId == R.id.nav_more) {
                // Navigate to more options
                navigateToMoreOptions();
                return true;
            }

            return false;
        });
    }

    private void loadRecommendedItems() {
        // Create adapter and set up the recycler view
        recommendedItems = getRecommendedActivities();
        RecommendedAdapter adapter = new RecommendedAdapter(recommendedItems);
        rvRecommended.setAdapter(adapter);
    }

    private void refreshDashboardData() {
        // Refresh data on dashboard
        loadDailyQuote();
        initializeStats();
        loadRecommendedItems();
        Toast.makeText(this, "Dashboard refreshed", Toast.LENGTH_SHORT).show();
    }

    private void navigateToChatScreen() {
        // In a real app, this could be an Activity or Fragment transition
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
    }

    private void navigateToJournalScreen() {
        Intent intent = new Intent(this, JournalActivity.class);
        startActivity(intent);
    }

    private void navigateToCommunityScreen() {
        Intent intent = new Intent(this, CommunityActivity.class);
        startActivity(intent);
    }

    private void navigateToMoreOptions() {
        Intent intent = new Intent(this, MoreOptionsActivity.class);
        startActivity(intent);
    }

    private List<RecommendedItem> getRecommendedActivities() {
        List<RecommendedItem> items = new ArrayList<>();

        // In a real app, these would come from a database or API
        items.add(new RecommendedItem("Calm Breathing", "5 min exercise", R.drawable.ic_breathing));
        items.add(new RecommendedItem("Stress Relief", "10 min meditation", R.drawable.ic_meditation));
        items.add(new RecommendedItem("Sleep Better", "Bedtime routine", R.drawable.ic_sleep));
        items.add(new RecommendedItem("Mindful Walking", "15 min outdoor activity", R.drawable.ic_walking));
        items.add(new RecommendedItem("Gratitude Journal", "Write 3 things", R.drawable.ic_journal));

        return items;
    }

    // New methods for mood detection

    private void showMoodCaptureOptions() {
        // Show a dialog with camera and gallery options
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Capture Mood");
        builder.setMessage("Take a photo or select one from gallery for mood analysis");

        builder.setPositiveButton("Camera", (dialog, which) -> {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(cameraIntent);
        });

        builder.setNegativeButton("Gallery", (dialog, which) -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(galleryIntent);
        });

        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void analyzeMood(Bitmap bitmap) {
        // Show loading state
        moodProgress.setIndeterminate(true);
        tvMoodScore.setText("...");

        // Process on background thread
        new Thread(() -> {
            try {
                // Resize the bitmap to match model input size
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, modelInputWidth, modelInputHeight, true);

                // Get model input information to understand what it expects
                int[] inputShape = tflite.getInputTensor(0).shape();
                int dataType = tflite.getInputTensor(0).dataType().ordinal();
                Log.d(TAG, "Model expects input shape: " +
                        inputShape[0] + "x" + inputShape[1] + "x" + inputShape[2] +
                        (inputShape.length > 3 ? "x" + inputShape[3] : "") +
                        " with data type: " + dataType);

                try {
                    // Simpler approach with direct pixel manipulation (more reliable)
                    int batchSize = inputShape[0];
                    int inputChannels = inputShape.length > 3 ? inputShape[3] : 3;

                    // Create a properly dimensioned array matching the model's input requirements
                    if (inputShape.length == 4) {
                        // NHWC format (batch, height, width, channels)
                        float[][][][] inputArray = new float[batchSize][modelInputHeight][modelInputWidth][inputChannels];

                        // Fill the input array with normalized pixel values
                        for (int y = 0; y < modelInputHeight; y++) {
                            for (int x = 0; x < modelInputWidth; x++) {
                                int pixel = resizedBitmap.getPixel(x, y);

                                // For RGB model (most common case)
                                inputArray[0][y][x][0] = ((pixel >> 16) & 0xFF) / 127.5f - 1.0f; // Red (normalized to [-1,1])
                                inputArray[0][y][x][1] = ((pixel >> 8) & 0xFF) / 127.5f - 1.0f;  // Green
                                inputArray[0][y][x][2] = (pixel & 0xFF) / 127.5f - 1.0f;         // Blue
                            }
                        }

                        // Prepare output tensor with proper size
                        float[][] outputProbabilities = new float[1][labels.size()];

                        // Run inference with the manually prepared input
                        tflite.run(inputArray, outputProbabilities);

                        processInferenceResults(outputProbabilities);
                    } else {
                        // Try with TensorImage approach as fallback
                        Log.d(TAG, "Using TensorImage approach instead");
                        TensorImage tensorImage = TensorImage.fromBitmap(resizedBitmap);
                        tensorImage = new ImageProcessor.Builder()
                                .add(new ResizeOp(modelInputHeight, modelInputWidth, ResizeOp.ResizeMethod.BILINEAR))
                                .build()
                                .process(tensorImage);

                        // Prepare output tensor
                        float[][] outputProbabilities = new float[1][labels.size()];

                        // Run inference
                        tflite.run(tensorImage.getBuffer(), outputProbabilities);

                        processInferenceResults(outputProbabilities);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in inference: " + e.getMessage(), e);

                    // Last resort fallback - try with a different buffer setup
                    try {
                        Log.d(TAG, "Attempting one more approach");

                        // Create a byte buffer instead
                        int[] intValues = new int[modelInputWidth * modelInputHeight];
                        resizedBitmap.getPixels(intValues, 0, modelInputWidth, 0, 0, modelInputWidth, modelInputHeight);

                        // Prepare output tensor
                        float[][] outputProbabilities = new float[1][labels.size()];

                        // Create a float buffer directly
                        float[] floatValues = new float[modelInputWidth * modelInputHeight * 3];
                        for (int i = 0; i < intValues.length; i++) {
                            final int val = intValues[i];
                            floatValues[i*3] = ((val >> 16) & 0xFF) / 255.0f;
                            floatValues[i*3+1] = ((val >> 8) & 0xFF) / 255.0f;
                            floatValues[i*3+2] = (val & 0xFF) / 255.0f;
                        }

                        // Run inference
                        tflite.run(floatValues, outputProbabilities);

                        processInferenceResults(outputProbabilities);
                    } catch (Exception fallbackException) {
                        throw new Exception("All input approaches failed: " + e.getMessage() +
                                " AND " + fallbackException.getMessage());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error analyzing mood: " + e.getMessage(), e);

                // Update UI on error
                runOnUiThread(() -> {
                    moodProgress.setIndeterminate(false);
                    moodProgress.setProgress(50);
                    tvMoodScore.setText("😐");
                    Toast.makeText(this, "Error detecting mood: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    // Helper method to process inference results
    private void processInferenceResults(float[][] outputProbabilities) {
        // Find the label with highest probability
        int maxIndex = 0;
        float maxProb = outputProbabilities[0][0];

        for (int i = 1; i < outputProbabilities[0].length; i++) {
            if (outputProbabilities[0][i] > maxProb) {
                maxProb = outputProbabilities[0][i];
                maxIndex = i;
            }
        }

        // Get the mood label
        final String detectedMood = labels.get(maxIndex);
        final int moodIndex = maxIndex;
        final float confidence = maxProb;

        // Log the result
        Log.d(TAG, "Detected mood: " + detectedMood + " with confidence: " + confidence);

        // Update UI on main thread
        runOnUiThread(() -> {
            moodProgress.setIndeterminate(false);
            updateMoodUI(moodIndex, confidence);

            Toast.makeText(this, "Detected mood: " + detectedMood, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateMoodUI(int moodIndex, float confidence) {
        // Calculate progress value (0-100)
        int progressValue;
        String moodEmoji;

        // Map mood index to emoji and progress value
        switch (moodIndex) {
            case 0: // happy
                moodEmoji = "😊";
                progressValue = 90;
                break;
            case 1: // sad
                moodEmoji = "😔";
                progressValue = 30;
                break;
            case 2: // neutral
                moodEmoji = "😐";
                progressValue = 50;
                break;
            case 3: // surprised
                moodEmoji = "😮";
                progressValue = 70;
                break;
            case 4: // angry
                moodEmoji = "😠";
                progressValue = 20;
                break;
            case 5: // disgusted
                moodEmoji = "🤢";
                progressValue = 25;
                break;
            case 6: // fearful
                moodEmoji = "😨";
                progressValue = 35;
                break;
            default:
                moodEmoji = "😐";
                progressValue = 50;
                break;
        }

        // Update UI
        tvMoodScore.setText(moodEmoji);
        moodProgress.setProgress(progressValue);
    }

    /**
     * Shows a dialog with daily mental health tips
     */
    private void showDailyTips() {
        // Create list of tips
        final String[] tips = {
                "Practice deep breathing for 5 minutes when feeling stressed",
                "Write down three things you're grateful for today",
                "Take a 10-minute walk outdoors for a mental refresh",
                "Limit social media use to reduce comparison and anxiety",
                "Stay hydrated - dehydration can affect your mood",
                "Get at least 7-8 hours of sleep for better mental clarity",
                "Try a 5-minute meditation to center yourself",
                "Call a friend or family member for a quick chat",
                "Take regular breaks when working on demanding tasks",
                "Practice positive self-talk when facing challenges"
        };

        // Show simple dialog with tips
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Daily Mental Health Tips")
                .setItems(tips, null)
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss());

        builder.create().show();

        // Update counter after viewing
        tvTipsCount.setText("0 New");
        Toast.makeText(this, "Daily tips refreshed", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tflite != null) {
            tflite.close();
        }
    }
}

