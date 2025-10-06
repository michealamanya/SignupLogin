package micheal.must.signuplogin;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class MoreOptionsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ListView listView;
    private List<SettingsItem> settingsItems;

    // Add Firebase Auth and Google Sign-In client
    private FirebaseAuth mAuth;
    private SignInClient oneTapClient;

    // Add SharedPreferences for storing settings
    private SharedPreferences sharedPreferences;
    private static final String PREF_DARK_MODE = "dark_mode";
    private static final String PREF_REMINDER_FREQ = "reminder_frequency";

    // Define reminder frequency options
    private static final String[] REMINDER_OPTIONS = {"Daily", "Twice Daily", "Weekly", "Monthly", "Never"};
    private static final String[] REMINDER_VALUES = {"daily", "twice_daily", "weekly", "monthly", "never"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_options);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        oneTapClient = Identity.getSignInClient(this);

        // Initialize SharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        toolbar = findViewById(R.id.toolbar);
        listView = findViewById(R.id.settings_list);

        setupToolbar();
        setupSettingsList();

        // Create notification channel for Android 8.0+
        createNotificationChannel();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("More Options");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupSettingsList() {
        settingsItems = new ArrayList<>();

        // Account Section
        settingsItems.add(new SettingsItem(SettingsItem.TYPE_HEADER, "Account", 0, null));
        settingsItems.add(new SettingsItem(SettingsItem.TYPE_ITEM, "Profile", R.drawable.ic_profile,
                "View and edit your profile"));
        settingsItems.add(new SettingsItem(SettingsItem.TYPE_ITEM, "Notifications", R.drawable.ic_notifications,
                "Manage notification settings"));
        settingsItems.add(new SettingsItem(SettingsItem.TYPE_ITEM, "Sign Out", R.drawable.ic_sign_out, null));

        // App Settings Section
        settingsItems.add(new SettingsItem(SettingsItem.TYPE_HEADER, "App Settings", 0, null));
        settingsItems.add(new SettingsItem(SettingsItem.TYPE_ITEM, "Dark Mode", R.drawable.ic_dark_mode,
                "Enable dark theme"));
        settingsItems.add(new SettingsItem(SettingsItem.TYPE_ITEM, "Reminder Frequency", R.drawable.ic_reminder,
                "How often to receive reminders"));

        // About Section
        settingsItems.add(new SettingsItem(SettingsItem.TYPE_HEADER, "About", 0, null));
        settingsItems.add(new SettingsItem(SettingsItem.TYPE_ITEM, "About MindMate", R.drawable.ic_info,
                "Version 1.0.0"));
        settingsItems.add(new SettingsItem(SettingsItem.TYPE_ITEM, "Privacy Policy", R.drawable.ic_privacy, null));
        settingsItems.add(new SettingsItem(SettingsItem.TYPE_ITEM, "Terms of Service", R.drawable.ic_terms, null));

        SettingsAdapter adapter = new SettingsAdapter();
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            SettingsItem item = settingsItems.get(position);
            if (item.type == SettingsItem.TYPE_ITEM) {
                handleSettingClick(item.title);
            }
        });
    }

    private void handleSettingClick(String title) {
        switch (title) {
            case "Profile":
                openProfileSettings();
                break;
            case "Notifications":
                openNotificationSettings();
                break;
            case "Sign Out":
                showSignOutConfirmationDialog();
                break;
            case "Dark Mode":
                toggleDarkMode();
                break;
            case "Reminder Frequency":
                showReminderFrequencyDialog();
                break;
            case "About MindMate":
                showAboutDialog();
                break;
            case "Privacy Policy":
                showPrivacyPolicy();
                break;
            case "Terms of Service":
                showTermsOfService();
                break;
        }
    }
    private void showSignOutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out", (dialog, which) -> {
                    performSignOut();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void performSignOut() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Show sign out message with user name if available
            String displayName = user.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                Toast.makeText(this, "Goodbye, " + displayName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Signing out...", Toast.LENGTH_SHORT).show();
            }

            // Sign out from Firebase
            mAuth.signOut();

            // Sign out from Google Sign-In
            oneTapClient.signOut().addOnCompleteListener(task -> {
                // Redirect to login screen
                Intent intent = new Intent(MoreOptionsActivity.this, MainActivity.class);
                // Clear the back stack so user can't go back to logged-in screens
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish(); // Close this activity
            });
        } else {
            // User is already signed out
            Toast.makeText(this, "Already signed out", Toast.LENGTH_SHORT).show();

            // Still redirect to login screen
            Intent intent = new Intent(MoreOptionsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
    /**
     * Opens the profile settings screen
     */
    private void openProfileSettings() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "You need to be logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_profile, null);
        builder.setView(dialogView);

        TextView tvName = dialogView.findViewById(R.id.tv_profile_name);
        TextView tvEmail = dialogView.findViewById(R.id.tv_profile_email);
        ImageView ivProfile = dialogView.findViewById(R.id.iv_profile_image);

        // Set user info
        tvName.setText(user.getDisplayName() != null ? user.getDisplayName() : "No Name");
        tvEmail.setText(user.getEmail());

        // Load profile image if exists
        if (user.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.default_avatar)
                    .into(ivProfile);
        }

        builder.setTitle("Your Profile")
                .setPositiveButton("Close", null)
                .setNeutralButton("Edit Profile", (dialog, which) -> {
                    // In a full app, this would navigate to a profile editor
                    Toast.makeText(this, "Profile editing coming soon", Toast.LENGTH_SHORT).show();
                });

        builder.create().show();
    }

    /**
     * Opens notification settings
     */
    private void openNotificationSettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_notifications, null);
        builder.setView(dialogView);

        Switch switchCheckIn = dialogView.findViewById(R.id.switch_checkin);
        Switch switchMeditation = dialogView.findViewById(R.id.switch_meditation);
        Switch switchJournal = dialogView.findViewById(R.id.switch_journal);
        Switch switchTips = dialogView.findViewById(R.id.switch_tips);

        // Load current preferences
        switchCheckIn.setChecked(sharedPreferences.getBoolean("notify_checkin", true));
        switchMeditation.setChecked(sharedPreferences.getBoolean("notify_meditation", true));
        switchJournal.setChecked(sharedPreferences.getBoolean("notify_journal", true));
        switchTips.setChecked(sharedPreferences.getBoolean("notify_tips", true));

        builder.setTitle("Notification Settings")
                .setPositiveButton("Save", (dialog, which) -> {
                    // Save notification preferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("notify_checkin", switchCheckIn.isChecked());
                    editor.putBoolean("notify_meditation", switchMeditation.isChecked());
                    editor.putBoolean("notify_journal", switchJournal.isChecked());
                    editor.putBoolean("notify_tips", switchTips.isChecked());
                    editor.apply();

                    Toast.makeText(this, "Notification settings saved", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    /**
     * Toggles dark mode
     */
    private void toggleDarkMode() {
        boolean isDarkMode = sharedPreferences.getBoolean(PREF_DARK_MODE, false);
        isDarkMode = !isDarkMode; // Toggle the value

        // Save the new setting
        sharedPreferences.edit().putBoolean(PREF_DARK_MODE, isDarkMode).apply();

        // Apply the new theme
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            Toast.makeText(this, "Dark mode enabled", Toast.LENGTH_SHORT).show();
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            Toast.makeText(this, "Dark mode disabled", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Shows reminder frequency options dialog
     */
    private void showReminderFrequencyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_reminder_frequency, null);
        builder.setView(dialogView);

        RadioGroup radioGroup = dialogView.findViewById(R.id.radio_group_frequency);

        // Get current frequency setting
        String currentFrequency = sharedPreferences.getString(PREF_REMINDER_FREQ, REMINDER_VALUES[0]);

        // Set up the radio buttons
        for (int i = 0; i < REMINDER_OPTIONS.length; i++) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(REMINDER_OPTIONS[i]);
            radioButton.setId(View.generateViewId());
            radioButton.setTag(REMINDER_VALUES[i]);
            radioGroup.addView(radioButton);

            // Check the current setting
            if (REMINDER_VALUES[i].equals(currentFrequency)) {
                radioButton.setChecked(true);
            }
        }

        builder.setTitle("Reminder Frequency")
                .setPositiveButton("Save", (dialog, which) -> {
                    // Get selected option
                    int selectedId = radioGroup.getCheckedRadioButtonId();
                    if (selectedId != -1) {
                        RadioButton selectedButton = dialogView.findViewById(selectedId);
                        String value = (String) selectedButton.getTag();

                        // Save the frequency setting
                        sharedPreferences.edit().putString(PREF_REMINDER_FREQ, value).apply();

                        Toast.makeText(this, "Reminder frequency updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    /**
     * Shows the about app dialog
     */
    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Create HTML formatted text for app info
        String aboutText = "<b>MindMate</b><br><br>" +
                "Version 1.0.0<br><br>" +
                "MindMate is your personal mental wellness companion, " +
                "designed to help you track your mood, practice mindfulness, " +
                "and improve your overall mental well-being.<br><br>" +
                "Developed by Micheal's Team<br>" +
                "© 2023 All Rights Reserved";

        builder.setTitle("About MindMate")
                .setMessage(Html.fromHtml(aboutText, Html.FROM_HTML_MODE_COMPACT))
                .setPositiveButton("OK", null);

        builder.create().show();
    }

    /**
     * Shows privacy policy
     */
    private void showPrivacyPolicy() {
        // For a full app, this would display a privacy policy document
        // For this example, we'll just open a simple dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Privacy Policy")
                .setMessage("MindMate is committed to protecting your privacy. " +
                        "We only collect data necessary to provide you with " +
                        "the best mental wellness experience. Your data is never sold to third parties.")
                .setPositiveButton("OK", null);

        builder.create().show();

        // Alternative: Open a web URL with the privacy policy
        // Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com/privacy"));
        // startActivity(browserIntent);
    }

    /**
     * Shows terms of service
     */
    private void showTermsOfService() {
        // Similar to privacy policy, this would display terms of service
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Terms of Service")
                .setMessage("By using MindMate, you agree to our terms and conditions. " +
                        "The app is provided 'as is' without warranty of any kind. " +
                        "We reserve the right to modify or discontinue the service at any time.")
                .setPositiveButton("OK", null);

        builder.create().show();
    }

    /**
     * Creates notification channel for Android 8.0+
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "MindMate Notifications";
            String description = "All notifications from MindMate app";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel("mindmate_channel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private class SettingsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return settingsItems.size();
        }

        @Override
        public Object getItem(int position) {
            return settingsItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 2; // Header and Item
        }

        @Override
        public int getItemViewType(int position) {
            return settingsItems.get(position).type;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SettingsItem item = settingsItems.get(position);

            if (item.type == SettingsItem.TYPE_HEADER) {
                if (convertView == null || convertView.getTag() != null) {
                    convertView = LayoutInflater.from(MoreOptionsActivity.this)
                            .inflate(R.layout.item_settings_header, parent, false);
                    convertView.setTag(null); // Mark as header
                }

                TextView tvHeader = convertView.findViewById(R.id.tv_header);
                tvHeader.setText(item.title);

            } else {
                if (convertView == null || convertView.getTag() == null) {
                    convertView = LayoutInflater.from(MoreOptionsActivity.this)
                            .inflate(R.layout.item_settings, parent, false);
                    convertView.setTag(new ViewHolder(convertView));
                }

                ViewHolder holder = (ViewHolder) convertView.getTag();
                holder.title.setText(item.title);
                holder.icon.setImageResource(item.iconResId);

                if (item.summary != null && !item.summary.isEmpty()) {
                    holder.summary.setVisibility(View.VISIBLE);
                    holder.summary.setText(item.summary);
                } else {
                    holder.summary.setVisibility(View.GONE);
                }
            }

            return convertView;
        }

        class ViewHolder {
            final TextView title;
            final TextView summary;
            final ImageView icon;

            ViewHolder(View view) {
                title = view.findViewById(R.id.tv_title);
                summary = view.findViewById(R.id.tv_summary);
                icon = view.findViewById(R.id.iv_icon);
            }
        }
    }

    private static class SettingsItem {
        static final int TYPE_HEADER = 0;
        static final int TYPE_ITEM = 1;

        final int type;
        final String title;
        final int iconResId;
        final String summary;

        SettingsItem(int type, String title, int iconResId, String summary) {
            this.type = type;
            this.title = title;
            this.iconResId = iconResId;
            this.summary = summary;
        }
    }
}
