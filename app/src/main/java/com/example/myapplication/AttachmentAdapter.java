package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AttachmentAdapter extends ListAdapter<AttachmentLink, AttachmentAdapter.VH> {

    public interface Listener {
        void onPreview(AttachmentLink link);
        void onRemove(AttachmentLink link);
    }

    private final Listener listener;

    public AttachmentAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    // Rows are matched by attachment-link ID, never by name or position.
    private static final DiffUtil.ItemCallback<AttachmentLink> DIFF = new DiffUtil.ItemCallback<AttachmentLink>() {
        @Override public boolean areItemsTheSame(@NonNull AttachmentLink a, @NonNull AttachmentLink b) {
            return a.getId() == b.getId();
        }
        @Override public boolean areContentsTheSame(@NonNull AttachmentLink a, @NonNull AttachmentLink b) {
            return a.getCatalogId() == b.getCatalogId() && a.getResourceRef().equals(b.getResourceRef());
        }
    };

    public AttachmentLink getLinkAt(int position) { return getItem(position); }

    public int indexOfId(int linkId) {
        List<AttachmentLink> list = getCurrentList();
        for (int i = 0; i < list.size(); i++) if (list.get(i).getId() == linkId) return i;
        return -1;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attachment, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        AttachmentLink a = getItem(position);
        CatalogItem c = Catalog.find(a.getCatalogId());
        h.tvName.setText(c.name);
        h.tvType.setText(c.type + " • " + c.sizeLabel + " • Linked " + DateUtil.formatMillis(a.getLinkedAt()));
        if (c.isImage())
            h.ivThumb.setImageResource(Catalog.drawableId(h.itemView.getContext(), a.getResourceRef()));
        else
            h.ivThumb.setImageResource(android.R.drawable.ic_menu_agenda);
    }

    class VH extends RecyclerView.ViewHolder {
        final ImageView ivThumb;
        final TextView tvName, tvType;

        VH(View v) {
            super(v);
            ivThumb = v.findViewById(R.id.ivThumb);
            tvName = v.findViewById(R.id.tvFileName);
            tvType = v.findViewById(R.id.tvFileType);
            v.findViewById(R.id.btnPreview).setOnClickListener(x -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) listener.onPreview(getItem(pos));
            });
            v.findViewById(R.id.btnRemove).setOnClickListener(x -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) listener.onRemove(getItem(pos));
            });
        }
    }
}