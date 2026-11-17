package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FilesFragment extends Fragment implements Refreshable {

    private ProjectHost host;
    private String query = "";
    private int typeFilter = 0; // 0 all, 1 images, 2 text
    private final List<AttachmentLink> shown = new ArrayList<>();
    private AttachmentAdapter adapter;
    private EditText etSearch;
    private TextView chipAll, chipImages, chipText, tvShowing, tvEmptyTitle;
    private Button btnEmptyAction;
    private RecyclerView rv;
    private View layoutEmpty;

    public static FilesFragment newInstance(int projectId) {
        FilesFragment f = new FilesFragment();
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
        return inf.inflate(R.layout.fragment_files, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);
        etSearch = v.findViewById(R.id.etFileSearch);
        chipAll = v.findViewById(R.id.chipAll);
        chipImages = v.findViewById(R.id.chipImages);
        chipText = v.findViewById(R.id.chipText);
        tvShowing = v.findViewById(R.id.tvFilesShowing);
        tvEmptyTitle = v.findViewById(R.id.tvFilesEmptyTitle);
        btnEmptyAction = v.findViewById(R.id.btnFilesEmptyAction);
        rv = v.findViewById(R.id.rvFiles);
        layoutEmpty = v.findViewById(R.id.layoutFilesEmpty);

        adapter = new AttachmentAdapter(new AttachmentAdapter.Listener() {
            @Override public void onPreview(AttachmentLink link) { showPreview(link); }
            @Override public void onRemove(AttachmentLink link) { confirmRemove(link); }
        });
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        new ItemTouchHelper(new AttachmentSwipeCallback(requireContext(), adapter, link -> confirmRemove(link))).attachToRecyclerView(rv); // SWIPE

        etSearch.addTextChangedListener(new SimpleTextWatcher() {
            @Override public void onChanged(String text) { query = text; refresh(); }
        });
        chipAll.setOnClickListener(x -> { typeFilter = 0; refresh(); });
        chipImages.setOnClickListener(x -> { typeFilter = 1; refresh(); });
        chipText.setOnClickListener(x -> { typeFilter = 2; refresh(); });
        v.findViewById(R.id.btnAddAttachment).setOnClickListener(x -> addAttachment());
        btnEmptyAction.setOnClickListener(x -> {
            if (host.getProject().getAttachments().isEmpty()) addAttachment();
            else { etSearch.setText(""); query = ""; typeFilter = 0; refresh(); }
        });

        refresh();
    }

    @Override public void onHiddenChanged(boolean hidden) { if (!hidden) refresh(); }
    @Override public void onResume() { super.onResume(); refresh(); }

    private void styleChip(TextView chip, boolean selected) {
        chip.setBackgroundResource(selected ? R.drawable.bg_chip_selected : R.drawable.bg_chip);
        chip.setTextColor(selected ? 0xFFFFFFFF : 0xFF111827);
    }

    @Override
    public void refresh() {
        if (getView() == null) return;
        Project p = host.getProject();

        int imgs = 0, txts = 0;
        for (AttachmentLink a : p.getAttachments()) {
            CatalogItem c = Catalog.find(a.getCatalogId());
            if (c != null && c.isImage()) imgs++; else txts++;
        }
        chipAll.setText("All (" + p.getAttachments().size() + ")");
        chipImages.setText("Images (" + imgs + ")");
        chipText.setText("Text (" + txts + ")");
        styleChip(chipAll, typeFilter == 0);
        styleChip(chipImages, typeFilter == 1);
        styleChip(chipText, typeFilter == 2);

        String q = query.toLowerCase(Locale.ROOT);
        shown.clear();
        for (AttachmentLink a : p.getAttachments()) {
            CatalogItem c = Catalog.find(a.getCatalogId());
            if (c == null) continue;
            if (!q.isEmpty() && !c.name.toLowerCase(Locale.ROOT).contains(q)) continue;
            if (typeFilter == 1 && !c.isImage()) continue;
            if (typeFilter == 2 && c.isImage()) continue;
            shown.add(a);
        }
        adapter.submitList(new ArrayList<>(shown));   // a new list every time; DiffUtil updates only changed rows
        tvShowing.setText(androidx.core.text.HtmlCompat.fromHtml(
                "Showing <b>" + shown.size() + " of " + p.getAttachments().size() + "</b> attachments",
                androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY));

        boolean none = shown.isEmpty();
        rv.setVisibility(none ? View.GONE : View.VISIBLE);
        layoutEmpty.setVisibility(none ? View.VISIBLE : View.GONE);
        if (none) {
            if (p.getAttachments().isEmpty()) {
                tvEmptyTitle.setText("No attachments yet");
                btnEmptyAction.setText("Add Attachment");
            } else {
                tvEmptyTitle.setText("No attachments match your search or type");
                btnEmptyAction.setText("Reset");
            }
        }
    }

    private void addAttachment() {
        final Project p = host.getProject();
        List<Integer> linked = new ArrayList<>();
        for (AttachmentLink a : p.getAttachments()) linked.add(a.getCatalogId());
        CatalogDialog.show(requireContext(), linked, item -> {
            p.getAttachments().add(new AttachmentLink(DataStore.getInstance().nextAttachmentId(), item.id, item.resRef));
            host.onProjectChanged();
        });
    }

    /** Preview shows the real content: an ImageView for images, a TextView for text files. */
    private void showPreview(AttachmentLink link) {
        CatalogItem item = Catalog.find(link.getCatalogId());
        View v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_preview, null);
        ImageView iv = v.findViewById(R.id.ivPreview);
        TextView tv = v.findViewById(R.id.tvPreviewText);
        if (item.isImage()) {
            iv.setVisibility(View.VISIBLE);
            tv.setVisibility(View.GONE);
            iv.setImageResource(Catalog.drawableId(requireContext(), link.getResourceRef()));
        } else {
            iv.setVisibility(View.GONE);
            tv.setVisibility(View.VISIBLE);
            tv.setText(item.content);
        }
        new AlertDialog.Builder(requireContext()).setTitle(item.name).setView(v)
                .setPositiveButton("Close", null).show();
    }

    private void restoreRow(int linkId) {
        int pos = adapter.indexOfId(linkId);
        if (pos >= 0) adapter.notifyItemChanged(pos);   // brings a swiped row back
    }

    /**
     * The user must type the attachment name. Removed only if the name matches and Remove is pressed.
     * Cancel, a wrong name, or tapping outside keeps the attachment and restores the row.
     */
    private void confirmRemove(final AttachmentLink link) {
        final String name = Catalog.find(link.getCatalogId()).name;
        final EditText et = new EditText(requireContext());
        et.setHint(name);
        et.setSingleLine(true);
        FrameLayout box = new FrameLayout(requireContext());
        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        box.setPadding(pad, 8, pad, 0);
        box.addView(et);

        final boolean[] removed = {false};
        AlertDialog d = new AlertDialog.Builder(requireContext())
                .setTitle("Remove attachment")
                .setMessage("To remove this attachment, type its name exactly: " + name)
                .setView(box)
                .setPositiveButton("Remove", null)
                .setNegativeButton("Cancel", null)
                .create();
        d.setOnDismissListener(x -> { if (!removed[0]) restoreRow(link.getId()); });
        d.show();

        d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(x -> {
            if (et.getText().toString().trim().equals(name)) {
                removed[0] = true;
                host.getProject().removeAttachmentById(link.getId());
                host.onProjectChanged();
            } else {
                Toast.makeText(requireContext(), "Name does not match. Attachment was not removed.",
                        Toast.LENGTH_SHORT).show();
            }
            d.dismiss();
        });
    }
}