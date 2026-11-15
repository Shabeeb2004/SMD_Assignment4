package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private List<Project> all;          // the full project collection
    private ProjectAdapter adapter;
    private EditText etSearch;
    private TextView tvRecords;

    @SuppressWarnings("deprecation")
    private final ActivityResultLauncher<Intent> projectLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent data = result.getData();
                if (data == null) return;
                Project updated = (Project) data.getSerializableExtra(ProjectActivity.EXTRA_PROJECT);
                if (updated == null) return;
                // update the existing project by ID
                for (int i = 0; i < all.size(); i++) {
                    if (all.get(i).getId() == updated.getId()) { all.set(i, updated); break; }
                }
                applyFilter();   // submits a new list; DiffUtil redraws only the changed row
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        all = DataStore.getInstance().getProjects();
        etSearch = findViewById(R.id.etSearch);
        tvRecords = findViewById(R.id.tvRecords);

        RecyclerView rv = findViewById(R.id.rvProjects);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProjectAdapter(project -> {
            Intent i = new Intent(this, ProjectActivity.class);
            i.putExtra(ProjectActivity.EXTRA_PROJECT, project);   // passed through intent extras
            projectLauncher.launch(i);
        });
        rv.setAdapter(adapter);

        etSearch.addTextChangedListener(new SimpleTextWatcher() {
            @Override public void onChanged(String text) { applyFilter(); }
        });

        findViewById(R.id.btnNewProject).setOnClickListener(v ->
                ProjectDialog.show(this, false, "", "", "", (name, tag, desc, att) -> {
                    DataStore ds = DataStore.getInstance();
                    Project p = new Project(ds.nextProjectId(), tag, name, desc);
                    if (att != null) p.getAttachments().add(new AttachmentLink(ds.nextAttachmentId(), att.id, att.resRef));
                    all.add(p);
                    applyFilter();
                }));

        applyFilter();
    }

    /** Builds a NEW list every time (ListAdapter ignores the same list instance). */
    private void applyFilter() {
        String q = etSearch.getText().toString().toLowerCase(Locale.ROOT);
        List<Project> filtered = new ArrayList<>();
        for (Project p : all) {
            if (q.isEmpty() || p.getName().toLowerCase(Locale.ROOT).contains(q)
                    || p.getTag().toLowerCase(Locale.ROOT).contains(q)) filtered.add(p);
        }
        adapter.submitList(filtered);
        tvRecords.setText("Showing " + filtered.size() + " records");
    }
}