package micheal.must.signuplogin;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvMessages;
    private EditText etMessage;
    private MaterialButton btnSend;
    private Chip chipFeelingAnxious, chipNeedMotivation, chipSleepHelp, chipFeelingDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        rvMessages = findViewById(R.id.rv_messages);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);

        chipFeelingAnxious = findViewById(R.id.chip_feeling_anxious);
        chipNeedMotivation = findViewById(R.id.chip_need_motivation);
        chipSleepHelp = findViewById(R.id.chip_sleep_help);
        chipFeelingDown = findViewById(R.id.chip_feeling_down);
    }

    private void setupClickListeners() {
        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
                etMessage.setText("");
            }
        });

        chipFeelingAnxious.setOnClickListener(v ->
                sendQuickResponse("I'm feeling anxious"));

        chipNeedMotivation.setOnClickListener(v ->
                sendQuickResponse("I need some motivation"));

        chipSleepHelp.setOnClickListener(v ->
                sendQuickResponse("I need help with sleep"));

        chipFeelingDown.setOnClickListener(v ->
                sendQuickResponse("I'm feeling down today"));
    }

    private void sendMessage(String message) {
        // In a real app, you'd add the message to a chat adapter
        // For now, just show a toast
        Toast.makeText(this, "Message sent: " + message, Toast.LENGTH_SHORT).show();
    }

    private void sendQuickResponse(String response) {
        sendMessage(response);
    }
}
