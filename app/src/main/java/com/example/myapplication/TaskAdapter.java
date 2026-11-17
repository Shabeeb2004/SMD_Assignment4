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
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/** One adapter, two view types (header row and task row), chosen in getItemViewType. */
public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface Listener {
        void onToggle(Task task, boolean checked);
        void onClick(Task task);
    }

    private final List<TaskListItem> items = new ArrayList<>();
    private final Listener listener;

    public TaskAdapter(Listener listener) { this.listener = listener; }

    /** Replaces the list. DiffUtil works out the minimal changes; notifyDataSetChanged is never used. */
    public void submit(List<TaskListItem> newItems) {
        final List<TaskListItem> oldList = new ArrayList<>(items);
        final List<TaskListItem> newList = new ArrayList<>(newItems);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return oldList.size(); }
            @Override public int getNewListSize() { return newList.size(); }
            @Override public boolean areItemsTheSame(int o, int n) {
                return oldList.get(o).sameItemAs(newList.get(n));
            }
            @Override public boolean areContentsTheSame(int o, int n) {
                return oldList.get(o).sameContentAs(newList.get(n));
            }
        });
        items.clear();
        items.addAll(newList);
        result.dispatchUpdatesTo(this);
    }

    public TaskListItem getItem(int position) { return items.get(position); }

    @Override public int getItemCount() { return items.size(); }

    @Override public int getItemViewType(int position) { return items.get(position).type; }

    /** Used while dragging: moves one row and notifies the RecyclerView. */
    public void moveItem(int from, int to) {
        TaskListItem moved = items.remove(from);
        items.add(to, moved);
        notifyItemMoved(from, to);
    }

    public int indexOfTask(int taskId) {
        for (int i = 0; i < items.size(); i++) {
            TaskListItem it = items.get(i);
            if (!it.isHeader() && it.taskId == taskId) return i;
        }
        return -1;
    }

    /** Task rows only, in the order they are currently displayed (all groups, top to bottom). */
    public List<Task> tasksInDisplayOrder() {
        List<Task> out = new ArrayList<>();
        for (TaskListItem it : items) if (!it.isHeader()) out.add(it.task);
        return out;
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
        TaskListItem item = items.get(position);
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