package micheal.must.signuplogin;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

public class CommunityActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView rvCommunityPosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        initViews();
        setupToolbar();
        setupTabs();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout);
        rvCommunityPosts = findViewById(R.id.rv_community_posts);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Community");
        }
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("All Posts"));
        tabLayout.addTab(tabLayout.newTab().setText("My Groups"));
        tabLayout.addTab(tabLayout.newTab().setText("Discover"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Update content based on selected tab
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not needed for this example
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Not needed for this example
            }
        });
    }
}
