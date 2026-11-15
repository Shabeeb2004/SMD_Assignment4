package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private List<Project> all;
    private final List<Project> shown = new ArrayList<>();
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
                for (int i = 0; i < all.size(); i++) {
                    if (all.get(i).getId() == updated.getId()) { all.set(i, updated); break; }
                }
                applyFilter();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        all = DataStore.getInstance().getProjects();
        etSearch = findViewById(R.id.etSearch);
        tvRecords = findViewById(R.id.tvRecords);
        ListView lv = findViewById(R.id.lvProjects);
        adapter = new ProjectAdapter(this, shown);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener((parent, view, position, id) -> {
            Intent i = new Intent(this, ProjectActivity.class);
            i.putExtra(ProjectActivity.EXTRA_PROJECT, shown.get(position));
            projectLauncher.launch(i);
        });

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

    private void applyFilter() {
        String q = etSearch.getText().toString().toLowerCase(Locale.ROOT);
        shown.clear();
        for (Project p : all) {
            if (q.isEmpty() || p.getName().toLowerCase(Locale.ROOT).contains(q)
                    || p.getTag().toLowerCase(Locale.ROOT).contains(q)) shown.add(p);
        }
        adapter.notifyDataSetChanged();
        tvRecords.setText("Showing " + shown.size() + " records");
    }
}