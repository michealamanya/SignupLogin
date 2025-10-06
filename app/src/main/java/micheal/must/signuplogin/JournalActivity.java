package micheal.must.signuplogin;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class JournalActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rvJournalEntries;
    private FloatingActionButton fabAddEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);

        initViews();
        setupToolbar();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvJournalEntries = findViewById(R.id.rv_journal_entries);
        fabAddEntry = findViewById(R.id.fab_add_entry);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Journal");
        }
    }

    private void setupClickListeners() {
        fabAddEntry.setOnClickListener(v -> {
            Toast.makeText(this, "Create new journal entry", Toast.LENGTH_SHORT).show();
            // In a real app, this would open a new activity or dialog to create an entry
        });
    }
}
