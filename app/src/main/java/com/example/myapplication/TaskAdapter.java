package com.example.myapplication;

import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

/** One adapter, two view types: section headers and task rows. */
public class TaskAdapter extends ListAdapter<TaskListItem, RecyclerView.ViewHolder> {

    public interface Listener {
        void onToggle(Task task, boolean checked);
        void onClick(Task task);
    }

    private final Listener listener;

    public TaskAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<TaskListItem> DIFF = new DiffUtil.ItemCallback<TaskListItem>() {
        @Override public boolean areItemsTheSame(@NonNull TaskListItem a, @NonNull TaskListItem b) {
            return a.sameItemAs(b);
        }
        @Override public boolean areContentsTheSame(@NonNull TaskListItem a, @NonNull TaskListItem b) {
            return a.sameContentAs(b);
        }
    };

    @Override
    public int getItemViewType(int position) {
        return getItem(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TaskListItem.TYPE_HEADER)
            return new HeaderVH(inf.inflate(R.layout.item_task_header, parent, false));
        return new TaskVH(inf.inflate(R.layout.item_task, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TaskListItem item = getItem(position);
        if (holder instanceof HeaderVH) ((HeaderVH) holder).bind(item);
        else ((TaskVH) holder).bind(item);
    }

    // ---------- header row ----------
    static class HeaderVH extends RecyclerView.ViewHolder {
        final TextView tvTitle, tvCount;

        HeaderVH(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvHeaderTitle);
            tvCount = v.findViewById(R.id.tvHeaderCount);
        }

        void bind(TaskListItem item) {
            tvTitle.setText(item.groupKey);
            tvCount.setText(String.valueOf(item.count));
            tvTitle.setCompoundDrawableTintList(ColorStateList.valueOf(colorFor(item.groupKey)));
        }

        private static int colorFor(String key) {
            switch (key) {
                case TaskGrouper.OVERDUE: return 0xFFEF4444;
                case TaskGrouper.IN_PROGRESS: return 0xFF2E9BF5;
                case TaskGrouper.DONE: return 0xFF10B981;
                default: return 0xFF9CA3AF;
            }
        }
    }

    // ---------- task row ----------
    class TaskVH extends RecyclerView.ViewHolder {
        final CheckBox cb;
        final TextView tvDesc, tvStatus, tvPriority, tvDue, tvOverdue;
        TaskListItem current;

        TaskVH(View v) {
            super(v);
            cb = v.findViewById(R.id.cbDone);
            tvDesc = v.findViewById(R.id.tvTaskDesc);
            tvStatus = v.findViewById(R.id.tvStatus);
            tvPriority = v.findViewById(R.id.tvPriority);
            tvDue = v.findViewById(R.id.tvDue);
            tvOverdue = v.findViewById(R.id.tvOverdue);
            v.setOnClickListener(x -> { if (current != null) listener.onClick(current.task); });
        }

        void bind(TaskListItem item) {
            current = item;
            int today = DateUtil.today();
            boolean dueToday = item.dueDate == today;

            tvDesc.setText(item.description);
            int flags = tvDesc.getPaintFlags();
            tvDesc.setPaintFlags(item.done ? (flags | Paint.STRIKE_THRU_TEXT_FLAG)
                    : (flags & ~Paint.STRIKE_THRU_TEXT_FLAG));
            tvDesc.setTextColor(item.done ? 0xFF6B7280 : 0xFF111827);
            tvStatus.setText(item.status);
            tvPriority.setText(item.priority);
            tvDue.setText(item.dueDate == 0 ? "No due date" : dueToday ? "Due today" : DateUtil.format(item.dueDate));
            tvDue.setTextColor(item.overdue ? 0xFFDC2626 : (dueToday && !item.done) ? 0xFFD97706 : 0xFF6B7280);
            tvOverdue.setVisibility(item.overdue ? View.VISIBLE : View.GONE);
            itemView.setAlpha(item.done ? 0.75f : 1f);

            cb.setOnCheckedChangeListener(null);   // avoid firing while binding
            cb.setChecked(item.done);
            cb.setOnCheckedChangeListener((b, checked) -> listener.onToggle(item.task, checked));
        }
    }
}