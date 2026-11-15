package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.tabs.TabLayout;

public class ProjectActivity extends AppCompatActivity implements ProjectHost {

    public static final String EXTRA_PROJECT = "project";
    private static final String STATE_TAB = "tab";
    private static final String[] TAGS = {"overview", "tasks", "files", "discussion"};

    private Project project;
    private TabLayout tabLayout;
    private TextView tvName, tvTag;
    private final Fragment[] fragments = new Fragment[4];

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_project);

        project = (Project) (s != null ? s.getSerializable(EXTRA_PROJECT)
                : getIntent().getSerializableExtra(EXTRA_PROJECT));
        if (project == null) { finish(); return; }
        DataStore.getInstance().syncCounters(project);

        tvName = findViewById(R.id.tvName);
        tvTag = findViewById(R.id.tvTag);
        tabLayout = findViewById(R.id.tabLayout);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        for (String t : new String[]{"Overview", "Tasks", "Files", "Discussion"})
            tabLayout.addTab(tabLayout.newTab().setText(t));

        FragmentManager fm = getSupportFragmentManager();
        int current = 0;
        if (s == null) {
            int pid = project.getId();
            fragments[0] = OverviewFragment.newInstance(pid);
            fragments[1] = TasksFragment.newInstance(pid);
            fragments[2] = FilesFragment.newInstance(pid);
            fragments[3] = DiscussionFragment.newInstance(pid);
            FragmentTransaction ft = fm.beginTransaction();
            for (int i = 0; i < 4; i++) {
                ft.add(R.id.fragmentContainer, fragments[i], TAGS[i]);
                if (i != 0) ft.hide(fragments[i]);
            }
            ft.commitNow();
        } else {
            for (int i = 0; i < 4; i++) fragments[i] = fm.findFragmentByTag(TAGS[i]);
            current = s.getInt(STATE_TAB, 0);
        }

        TabLayout.Tab t = tabLayout.getTabAt(current);
        if (t != null) t.select();
        showFragment(current);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { showFragment(tab.getPosition()); }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        updateHeader();
    }

    private void showFragment(int index) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            if (fragments[i] == null) continue;
            if (i == index) ft.show(fragments[i]); else ft.hide(fragments[i]);
        }
        ft.commit();
    }

    private void updateHeader() {
        tvName.setText(project.getName());
        tvTag.setText(project.getTag());
        String[] titles = {"Overview",
                "Tasks (" + project.getTasks().size() + ")",
                "Files (" + project.getAttachments().size() + ")",
                "Discussion (" + project.getCommentCount() + ")"};
        for (int i = 0; i < titles.length; i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) tab.setText(titles[i]);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putSerializable(EXTRA_PROJECT, project);
        out.putInt(STATE_TAB, tabLayout.getSelectedTabPosition());
    }

    @Override
    public void finish() {
        if (project != null) {
            Intent r = new Intent();
            r.putExtra(EXTRA_PROJECT, project);
            setResult(RESULT_OK, r);
        }
        super.finish();
    }

    // ---------- ProjectHost ----------

    @Override public Project getProject() { return project; }

    @Override
    public void onProjectChanged() {
        updateHeader();
        for (Fragment f : fragments) if (f instanceof Refreshable) ((Refreshable) f).refresh();
    }

    @Override
    public void selectTab(int index) {
        TabLayout.Tab t = tabLayout.getTabAt(index);
        if (t != null) t.select();
    }

    @Override
    public void openTasks(String status, String dateFilter) {
        ((TasksFragment) fragments[1]).setFilters(status, dateFilter);
        selectTab(1);
    }

    @Override
    public void showEditProjectDialog() {
        ProjectDialog.show(this, true, project.getName(), project.getTag(), project.getDescription(),
                (n, t, d, a) -> {
                    project.setName(n);
                    project.setTag(t);
                    project.setDescription(d);
                    onProjectChanged();
                });
    }

    private void setDueLabel(TextView tv, View clearBtn, int due) {
        if (due == 0) {
            tv.setText("mm/dd/yyyy");
            tv.setTextColor(0xFF9CA3AF);
            clearBtn.setVisibility(View.GONE);
        } else {
            tv.setText(DateUtil.formatMDY(due));
            tv.setTextColor(0xFF111827);
            clearBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showTaskDialog(final Task existing) {
        View v = getLayoutInflater().inflate(R.layout.dialog_task, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(v).create();

        TextView tvTitle = v.findViewById(R.id.tvSheetTitle);
        TextView tvSub = v.findViewById(R.id.tvSheetSub);
        EditText etDesc = v.findViewById(R.id.etTaskDesc);
        TextView tvCounter = v.findViewById(R.id.tvCounter);
        Spinner spPriority = v.findViewById(R.id.spTaskPriority);
        Spinner spStatus = v.findViewById(R.id.spTaskStatus);
        TextView tvDue = v.findViewById(R.id.tvDueDate);
        View btnClearDate = v.findViewById(R.id.btnClearDate);
        TextView btnSubmit = v.findViewById(R.id.btnSubmit);
        View btnDelete = v.findViewById(R.id.btnDelete);

        tvTitle.setText(existing == null ? "Add New Task" : "Edit Task");
        tvSub.setText(project.getName() + " project");
        btnSubmit.setText(existing == null ? "+  Add Task" : "Save");
        btnDelete.setVisibility(existing == null ? View.GONE : View.VISIBLE);

        etDesc.setFilters(new InputFilter[]{new InputFilter.LengthFilter(500)});
        ArrayAdapter<String> pa = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Task.PRIORITIES);
        pa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPriority.setAdapter(pa);
        ArrayAdapter<String> sa = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Task.STATUSES);
        sa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStatus.setAdapter(sa);

        final int[] due = {0};
        if (existing != null) {
            etDesc.setText(existing.getDescription());
            spPriority.setSelection(Task.indexOf(Task.PRIORITIES, existing.getPriority()));
            spStatus.setSelection(Task.indexOf(Task.STATUSES, existing.getStatus()));
            due[0] = existing.getDueDate();
        } else {
            spPriority.setSelection(Task.indexOf(Task.PRIORITIES, Task.NORMAL));
            spStatus.setSelection(0);
        }
        tvCounter.setText(etDesc.length() + "/500");
        etDesc.addTextChangedListener(new SimpleTextWatcher() {
            @Override public void onChanged(String text) { tvCounter.setText(text.length() + "/500"); }
        });
        setDueLabel(tvDue, btnClearDate, due[0]);

        v.findViewById(R.id.layoutDueDate).setOnClickListener(x -> {
            int init = due[0] != 0 ? due[0] : DateUtil.today();
            new DatePickerDialog(this, (dp, y, m, d) -> {
                due[0] = DateUtil.of(y, m + 1, d);
                setDueLabel(tvDue, btnClearDate, due[0]);
            }, init / 10000, (init / 100) % 100 - 1, init % 100).show();
        });
        btnClearDate.setOnClickListener(x -> { due[0] = 0; setDueLabel(tvDue, btnClearDate, 0); });

        v.findViewById(R.id.btnSheetClose).setOnClickListener(x -> dialog.dismiss());
        v.findViewById(R.id.btnCancel).setOnClickListener(x -> dialog.dismiss());

        btnSubmit.setOnClickListener(x -> {
            String desc = etDesc.getText().toString().trim();
            if (desc.length() < 1 || desc.length() > 500) {
                etDesc.setError("Description must be 1-500 characters");
                return;
            }
            String status = Task.STATUSES[spStatus.getSelectedItemPosition()];
            String priority = Task.PRIORITIES[spPriority.getSelectedItemPosition()];
            if (existing == null) {
                project.getTasks().add(new Task(DataStore.getInstance().nextTaskId(), desc, status, priority, due[0]));
            } else {
                existing.setDescription(desc);
                existing.setStatus(status);
                existing.setPriority(priority);
                existing.setDueDate(due[0]);
            }
            dialog.dismiss();
            onProjectChanged();
        });

        if (existing != null) {
            btnDelete.setOnClickListener(x ->
                    new AlertDialog.Builder(this)
                            .setTitle("Delete task?")
                            .setMessage("This task will be removed from the project.")
                            .setPositiveButton("Delete", (dd, w) -> {
                                project.removeTaskById(existing.getId());
                                dialog.dismiss();
                                onProjectChanged();
                            })
                            .setNegativeButton("Cancel", null)
                            .show());
        }

        dialog.show();
        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            w.setGravity(Gravity.BOTTOM);
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }
}