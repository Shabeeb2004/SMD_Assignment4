package com.example.myapplication;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import java.util.List;

public class TaskAdapter extends BaseAdapter {
    public interface OnToggle { void onToggle(Task t, boolean checked); }

    private final Context ctx;
    private final List<Task> data;
    private final OnToggle listener;

    public TaskAdapter(Context ctx, List<Task> data, OnToggle listener) {
        this.ctx = ctx; this.data = data; this.listener = listener;
    }

    @Override public int getCount() { return data.size(); }
    @Override public Object getItem(int i) { return data.get(i); }
    @Override public long getItemId(int i) { return data.get(i).getId(); }

    @Override
    public View getView(int pos, View cv, ViewGroup parent) {
        VH h;
        if (cv == null) {
            cv = LayoutInflater.from(ctx).inflate(R.layout.item_task, parent, false);
            h = new VH(cv);
            cv.setTag(h);
        } else h = (VH) cv.getTag();

        final Task t = data.get(pos);
        boolean done = t.isDone();
        h.tvDesc.setText(t.getDescription());
        int flags = h.tvDesc.getPaintFlags();
        h.tvDesc.setPaintFlags(done ? (flags | Paint.STRIKE_THRU_TEXT_FLAG) : (flags & ~Paint.STRIKE_THRU_TEXT_FLAG));
        h.tvDesc.setTextColor(done ? 0xFF6B7280 : 0xFF111827);
        h.tvStatus.setText(t.getStatus());
        h.tvPriority.setText(t.getPriority());

        int today = DateUtil.today();
        boolean overdue = t.isOverdue(today);
        boolean dueToday = t.getDueDate() == today;
        h.tvDue.setText(t.getDueDate() == 0 ? "No due date" : dueToday ? "Due today" : DateUtil.format(t.getDueDate()));
        h.tvDue.setTextColor(overdue ? 0xFFDC2626 : (dueToday && !done) ? 0xFFD97706 : 0xFF6B7280);
        h.tvOverdue.setVisibility(overdue ? View.VISIBLE : View.GONE);
        cv.setAlpha(done ? 0.75f : 1f);

        h.cb.setOnCheckedChangeListener(null);
        h.cb.setChecked(done);
        h.cb.setOnCheckedChangeListener((b, checked) -> listener.onToggle(t, checked));
        return cv;
    }

    static class VH {
        CheckBox cb;
        TextView tvDesc, tvStatus, tvPriority, tvDue, tvOverdue;
        VH(View v) {
            cb = v.findViewById(R.id.cbDone);
            tvDesc = v.findViewById(R.id.tvTaskDesc);
            tvStatus = v.findViewById(R.id.tvStatus);
            tvPriority = v.findViewById(R.id.tvPriority);
            tvDue = v.findViewById(R.id.tvDue);
            tvOverdue = v.findViewById(R.id.tvOverdue);
        }
    }
}