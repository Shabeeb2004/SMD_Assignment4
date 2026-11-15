package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.List;

public class ProjectAdapter extends BaseAdapter {
    private final Context ctx;
    private final List<Project> data;

    public ProjectAdapter(Context ctx, List<Project> data) { this.ctx = ctx; this.data = data; }

    @Override public int getCount() { return data.size(); }
    @Override public Object getItem(int i) { return data.get(i); }
    @Override public long getItemId(int i) { return data.get(i).getId(); }

    @Override
    public View getView(int pos, View cv, ViewGroup parent) {
        VH h;
        if (cv == null) {
            cv = LayoutInflater.from(ctx).inflate(R.layout.item_project, parent, false);
            h = new VH(cv);
            cv.setTag(h);
        } else h = (VH) cv.getTag();

        Project p = data.get(pos);
        h.tvTag.setText(p.getTag());
        h.tvName.setText(p.getName());
        h.tvDesc.setText(p.getDescription());
        h.tvTasks.setText(p.getDoneCount() + "/" + p.getTasks().size() + " tasks");
        h.tvPercent.setText(p.getPercent() + "%");
        h.pb.setMax(100);
        h.pb.setProgress(p.getPercent());
        h.tvAtt.setText(p.getAttachments().size() + " Attachments");
        h.tvComments.setText(p.getCommentCount() + " Comments");
        return cv;
    }

    static class VH {
        TextView tvTag, tvName, tvDesc, tvTasks, tvPercent, tvAtt, tvComments;
        ProgressBar pb;
        VH(View v) {
            tvTag = v.findViewById(R.id.tvTag);
            tvName = v.findViewById(R.id.tvProjectName);
            tvDesc = v.findViewById(R.id.tvProjectDesc);
            tvTasks = v.findViewById(R.id.tvTaskCount);
            tvPercent = v.findViewById(R.id.tvPercent);
            pb = v.findViewById(R.id.pbProject);
            tvAtt = v.findViewById(R.id.tvAttCount);
            tvComments = v.findViewById(R.id.tvCommentCount);
        }
    }
}