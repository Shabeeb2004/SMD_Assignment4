package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class OverviewFragment extends Fragment implements Refreshable {

    private ProjectHost host;
    private TextView tvDescription, tvCompleted, tvPercent, tvTodo, tvInProgress, tvDone, tvOverdue, tvAttSub;
    private ProgressBar pb;

    public static OverviewFragment newInstance(int projectId) {
        OverviewFragment f = new OverviewFragment();
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
        return inf.inflate(R.layout.fragment_overview, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);
        tvDescription = v.findViewById(R.id.tvDescription);
        tvCompleted = v.findViewById(R.id.tvCompleted);
        tvPercent = v.findViewById(R.id.tvPercent);
        pb = v.findViewById(R.id.pbProgress);
        tvTodo = v.findViewById(R.id.tvTodoCount);
        tvInProgress = v.findViewById(R.id.tvInProgressCount);
        tvDone = v.findViewById(R.id.tvDoneCount);
        tvOverdue = v.findViewById(R.id.tvOverdueCount);
        tvAttSub = v.findViewById(R.id.tvAttachmentsSub);

        v.findViewById(R.id.cardTodo).setOnClickListener(x -> host.openTasks(Task.TODO, null));
        v.findViewById(R.id.cardInProgress).setOnClickListener(x -> host.openTasks(Task.IN_PROGRESS, null));
        v.findViewById(R.id.cardDone).setOnClickListener(x -> host.openTasks(Task.DONE, null));
        v.findViewById(R.id.cardOverdue).setOnClickListener(x ->
                host.openTasks(TasksFragment.STATUS_OPTIONS[0], TasksFragment.DATE_OPTIONS[2]));
        v.findViewById(R.id.rowAttachments).setOnClickListener(x -> host.selectTab(2));
        v.findViewById(R.id.rowDiscussion).setOnClickListener(x -> host.selectTab(3));
        v.findViewById(R.id.btnAddTask).setOnClickListener(x -> host.showTaskDialog(null));
        v.findViewById(R.id.btnEditProject).setOnClickListener(x -> host.showEditProjectDialog());

        refresh();
    }

    @Override public void onHiddenChanged(boolean hidden) { if (!hidden) refresh(); }
    @Override public void onResume() { super.onResume(); refresh(); }

    @Override
    public void refresh() {
        if (getView() == null) return;
        Project p = host.getProject();
        tvDescription.setText(p.getDescription());
        tvCompleted.setText(p.getDoneCount() + " of " + p.getTasks().size() + " tasks completed");
        tvPercent.setText(p.getPercent() + "%");
        pb.setMax(100);
        pb.setProgress(p.getPercent());
        tvTodo.setText(String.valueOf(p.countByStatus(Task.TODO)));
        tvInProgress.setText(String.valueOf(p.countByStatus(Task.IN_PROGRESS)));
        tvDone.setText(String.valueOf(p.countByStatus(Task.DONE)));
        tvOverdue.setText(String.valueOf(p.getOverdueCount(DateUtil.today())));

        int n = p.getAttachments().size();
        if (n == 0) {
            tvAttSub.setText("No files yet");
        } else {
            StringBuilder names = new StringBuilder();
            for (int i = 0; i < n && i < 2; i++) {
                CatalogItem c = Catalog.find(p.getAttachments().get(i).getCatalogId());
                if (c == null) continue;
                if (names.length() > 0) names.append(", ");
                names.append(c.name);
            }
            tvAttSub.setText(n + (n == 1 ? " file" : " files") + " • " + names);
        }
    }
}