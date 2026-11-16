package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TasksFragment extends Fragment implements Refreshable {

    public static final String[] STATUS_OPTIONS = {"All statuses", "To do", "In progress", "Done"};
    public static final String[] PRIORITY_OPTIONS = {"All priorities", "High", "Normal", "Low"};
    public static final String[] DATE_OPTIONS = {"All dates", "Today", "Overdue", "No due date"};
    public static final String[] SORT_OPTIONS = {"Due date ascending", "Due date descending",
            "Priority (high to low)", "Newest first", "Custom order"};

    private interface Pick { void on(String value); }

    private ProjectHost host;
    private String query = "";
    private String status = STATUS_OPTIONS[0], priority = PRIORITY_OPTIONS[0],
            dateFilter = DATE_OPTIONS[0], sort = SORT_OPTIONS[0];
    private final List<Task> shown = new ArrayList<>();   // filtered + sorted tasks, before grouping
    private TaskAdapter adapter;
    private EditText etSearch;
    private Spinner spStatus, spPriority, spDate, spSort;
    private TextView tvShowing, tvEmptyTitle;
    private Button btnEmptyAction;
    private RecyclerView rv;
    private View layoutEmpty;

    public static TasksFragment newInstance(int projectId) {
        TasksFragment f = new TasksFragment();
        Bundle b = new Bundle();
        b.putInt("project_id", projectId);
        f.setArguments(b);
        return f;
    }

    @Override public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        host = (ProjectHost) context;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        return inf.inflate(R.layout.fragment_tasks, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);
        etSearch = v.findViewById(R.id.etTaskSearch);
        spStatus = v.findViewById(R.id.spStatus);
        spPriority = v.findViewById(R.id.spPriority);
        spDate = v.findViewById(R.id.spDate);
        spSort = v.findViewById(R.id.spSort);
        tvShowing = v.findViewById(R.id.tvShowing);
        tvEmptyTitle = v.findViewById(R.id.tvEmptyTitle);
        btnEmptyAction = v.findViewById(R.id.btnEmptyAction);
        rv = v.findViewById(R.id.rvTasks);
        layoutEmpty = v.findViewById(R.id.layoutEmpty);

        adapter = new TaskAdapter(new TaskAdapter.Listener() {
            @Override public void onToggle(Task task, boolean checked) {
                task.setDone(checked);
                host.onProjectChanged();
            }
            @Override public void onClick(Task task) { host.showTaskDialog(task); }
        });
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        etSearch.addTextChangedListener(new SimpleTextWatcher() {
            @Override public void onChanged(String text) { query = text; refresh(); }
        });
        bindSpinner(spStatus, STATUS_OPTIONS, status, "Status: ", true, val -> { status = val; refresh(); });
        bindSpinner(spPriority, PRIORITY_OPTIONS, priority, "Priority: ", true, val -> { priority = val; refresh(); });
        bindSpinner(spDate, DATE_OPTIONS, dateFilter, "Date: ", true, val -> { dateFilter = val; refresh(); });
        bindSpinner(spSort, SORT_OPTIONS, sort, "Sort: ", false, val -> { sort = val; refresh(); });

        v.findViewById(R.id.btnResetFilters).setOnClickListener(x -> resetFilters());
        v.findViewById(R.id.btnAddTask).setOnClickListener(x -> host.showTaskDialog(null));
        btnEmptyAction.setOnClickListener(x -> {
            if (host.getProject().getTasks().isEmpty()) host.showTaskDialog(null);
            else resetFilters();
        });

        refresh();
    }

    private void bindSpinner(Spinner sp, final String[] options, String current,
                             final String prefix, final boolean shortFirst, final Pick pick) {
        ArrayAdapter<String> a = new ArrayAdapter<String>(requireContext(),
                android.R.layout.simple_spinner_item, options) {
            @NonNull @Override
            public View getView(int position, @Nullable View cv, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, cv, parent);
                tv.setText(prefix + ((shortFirst && position == 0) ? "All" : options[position]));
                tv.setTextSize(13);
                tv.setTextColor(0xFF111827);
                tv.setSingleLine(true);
                tv.setEllipsize(TextUtils.TruncateAt.END);
                return tv;
            }
        };
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(a);
        sp.setSelection(Task.indexOf(options, current), false);
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) { pick.on(options[pos]); }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /** Called from Overview. null means "leave this filter as it is". */
    public void setFilters(String newStatus, String newDate) {
        if (newStatus != null) status = newStatus;
        if (newDate != null) dateFilter = newDate;
        if (getView() != null) {
            spStatus.setSelection(Task.indexOf(STATUS_OPTIONS, status));
            spDate.setSelection(Task.indexOf(DATE_OPTIONS, dateFilter));
            refresh();
        }
    }

    private void resetFilters() {
        query = "";
        status = STATUS_OPTIONS[0];
        priority = PRIORITY_OPTIONS[0];
        dateFilter = DATE_OPTIONS[0];
        sort = SORT_OPTIONS[0];
        etSearch.setText("");
        spStatus.setSelection(0);
        spPriority.setSelection(0);
        spDate.setSelection(0);
        spSort.setSelection(0);
        refresh();
    }

    @Override public void onHiddenChanged(boolean hidden) { if (!hidden) refresh(); }
    @Override public void onResume() { super.onResume(); refresh(); }

    @Override
    public void refresh() {
        if (getView() == null) return;
        Project p = host.getProject();
        int today = DateUtil.today();
        String q = query.toLowerCase(Locale.ROOT);

        // 1. filter
        shown.clear();
        for (Task t : p.getTasks()) {
            if (!q.isEmpty() && !t.getDescription().toLowerCase(Locale.ROOT).contains(q)) continue;
            if (!status.equals(STATUS_OPTIONS[0]) && !t.getStatus().equals(status)) continue;
            if (!priority.equals(PRIORITY_OPTIONS[0]) && !t.getPriority().equals(priority)) continue;
            if (dateFilter.equals(DATE_OPTIONS[1]) && t.getDueDate() != today) continue;
            if (dateFilter.equals(DATE_OPTIONS[2]) && !t.isOverdue(today)) continue;
            if (dateFilter.equals(DATE_OPTIONS[3]) && t.getDueDate() != 0) continue;
            shown.add(t);
        }

        // 2. sort ("Custom order" keeps the project's own task order)
        if (!sort.equals(SORT_OPTIONS[4])) {
            Collections.sort(shown, (a, b) -> {
                int r;
                if (sort.equals(SORT_OPTIONS[0])) r = cmpDue(a, b, true);
                else if (sort.equals(SORT_OPTIONS[1])) r = cmpDue(a, b, false);
                else if (sort.equals(SORT_OPTIONS[2])) r = Integer.compare(prio(a), prio(b));
                else r = Integer.compare(b.getId(), a.getId());
                return r != 0 ? r : Integer.compare(a.getId(), b.getId());
            });
        }

        // 3. group by status and submit one new list to the single adapter
        adapter.submitList(TaskGrouper.build(shown, today));
        tvShowing.setText("Showing " + shown.size() + " of " + p.getTasks().size() + " tasks");

        boolean none = shown.isEmpty();
        rv.setVisibility(none ? View.GONE : View.VISIBLE);
        layoutEmpty.setVisibility(none ? View.VISIBLE : View.GONE);
        if (none) {
            if (p.getTasks().isEmpty()) {
                tvEmptyTitle.setText("No tasks yet");
                btnEmptyAction.setText("Add Task");
            } else {
                tvEmptyTitle.setText("No tasks match these filters");
                btnEmptyAction.setText("Reset Filters");
            }
        }
    }

    private static int prio(Task t) {
        return Task.HIGH.equals(t.getPriority()) ? 0 : Task.NORMAL.equals(t.getPriority()) ? 1 : 2;
    }

    private static int cmpDue(Task a, Task b, boolean asc) {
        int da = a.getDueDate(), db = b.getDueDate();
        if (da == 0 && db == 0) return 0;
        if (da == 0) return 1;   // undated tasks always last
        if (db == 0) return -1;
        return asc ? Integer.compare(da, db) : Integer.compare(db, da);
    }
}