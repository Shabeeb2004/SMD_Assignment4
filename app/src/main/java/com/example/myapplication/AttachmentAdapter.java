package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

public class AttachmentAdapter extends BaseAdapter {
    public interface Listener {
        void onPreview(AttachmentLink a);
        void onRemove(AttachmentLink a);
    }

    private final Context ctx;
    private final List<AttachmentLink> data;
    private final Listener listener;

    public AttachmentAdapter(Context ctx, List<AttachmentLink> data, Listener listener) {
        this.ctx = ctx; this.data = data; this.listener = listener;
    }

    @Override public int getCount() { return data.size(); }
    @Override public Object getItem(int i) { return data.get(i); }
    @Override public long getItemId(int i) { return data.get(i).getId(); }

    @Override
    public View getView(int pos, View cv, ViewGroup parent) {
        if (cv == null) cv = LayoutInflater.from(ctx).inflate(R.layout.item_attachment, parent, false);
        ImageView iv = cv.findViewById(R.id.ivThumb);
        TextView tvName = cv.findViewById(R.id.tvFileName);
        TextView tvType = cv.findViewById(R.id.tvFileType);

        final AttachmentLink a = data.get(pos);
        CatalogItem c = Catalog.find(a.getCatalogId());
        tvName.setText(c.name);
        tvType.setText(c.type + " • " + c.sizeLabel + " • Linked " + DateUtil.formatMillis(a.getLinkedAt()));
        if (c.isImage()) iv.setImageResource(Catalog.drawableId(ctx, a.getResourceRef()));
        else iv.setImageResource(android.R.drawable.ic_menu_agenda);

        cv.findViewById(R.id.btnPreview).setOnClickListener(v -> listener.onPreview(a));
        cv.findViewById(R.id.btnRemove).setOnClickListener(v -> listener.onRemove(a));
        return cv;
    }
}