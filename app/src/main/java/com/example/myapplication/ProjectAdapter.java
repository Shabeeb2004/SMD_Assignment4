package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class ProjectAdapter extends ListAdapter<Project, ProjectAdapter.VH> {

    public interface OnProjectClick { void onClick(Project project); }

    private final OnProjectClick listener;

    public ProjectAdapter(OnProjectClick listener) {
        super(DIFF);
        this.listener = listener;
    }

    // Rows are matched by project ID, never by name or position.
    // A row is redrawn only when something it displays has changed.
    private static final DiffUtil.ItemCallback<Project> DIFF = new DiffUtil.ItemCallback<Project>() {
        @Override
        public boolean areItemsTheSame(@NonNull Project a, @NonNull Project b) {
            return a.getId() == b.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Project a, @NonNull Project b) {
            return a.getTag().equals(b.getTag())
                    && a.getName().equals(b.getName())
                    && a.getDescription().equals(b.getDescription())
                    && a.getTasks().size() == b.getTasks().size()
                    && a.getDoneCount() == b.getDoneCount()
                    && a.getAttachments().size() == b.getAttachments().size()
                    && a.getCommentCount() == b.getCommentCount();
        }
    };

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Project p = getItem(position);
        h.tvTag.setText(p.getTag());
        h.tvName.setText(p.getName());
        h.tvDesc.setText(p.getDescription());
        h.tvTasks.setText(p.getDoneCount() + "/" + p.getTasks().size() + " tasks");
        h.tvPercent.setText(p.getPercent() + "%");
        h.pb.setMax(100);
        h.pb.setProgress(p.getPercent());
        h.tvAtt.setText(p.getAttachments().size() + " Attachments");
        h.tvComments.setText(p.getCommentCount() + " Comments");
    }

    class VH extends RecyclerView.ViewHolder {
        final TextView tvTag, tvName, tvDesc, tvTasks, tvPercent, tvAtt, tvComments;
        final ProgressBar pb;

        VH(View v) {
            super(v);
            tvTag = v.findViewById(R.id.tvTag);
            tvName = v.findViewById(R.id.tvProjectName);
            tvDesc = v.findViewById(R.id.tvProjectDesc);
            tvTasks = v.findViewById(R.id.tvTaskCount);
            tvPercent = v.findViewById(R.id.tvPercent);
            pb = v.findViewById(R.id.pbProject);
            tvAtt = v.findViewById(R.id.tvAttCount);
            tvComments = v.findViewById(R.id.tvCommentCount);

            v.setOnClickListener(x -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) listener.onClick(getItem(pos));
            });
        }
    }
}