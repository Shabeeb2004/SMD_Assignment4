package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class AttachmentSwipeCallback extends ItemTouchHelper.SimpleCallback {

    public interface OnSwipeRemove { void onSwipeRemove(AttachmentLink link); }

    private final AttachmentAdapter adapter;
    private final OnSwipeRemove listener;
    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final float density;

    public AttachmentSwipeCallback(Context ctx, AttachmentAdapter adapter, OnSwipeRemove listener) {
        super(0, ItemTouchHelper.RIGHT);   // no drag, swipe right only
        this.adapter = adapter;
        this.listener = listener;
        this.density = ctx.getResources().getDisplayMetrics().density;
        bgPaint.setColor(0xFFEF4444);
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextSize(14 * density);
        textPaint.setFakeBoldText(true);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh,
                          @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direction) {
        int pos = vh.getAdapterPosition();
        if (pos == RecyclerView.NO_POSITION) return;
        listener.onSwipeRemove(adapter.getLinkAt(pos));
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX > 0) {
            View v = vh.itemView;
            float radius = 14 * density;
            rect.set(v.getLeft(), v.getTop(), v.getLeft() + dX, v.getBottom());
            c.drawRoundRect(rect, radius, radius, bgPaint);
            float textY = v.getTop() + v.getHeight() / 2f - (textPaint.descent() + textPaint.ascent()) / 2f;
            c.save();
            c.clipRect(rect);
            c.drawText("Remove", v.getLeft() + 16 * density, textY, textPaint);
            c.restore();
        }
        super.onChildDraw(c, rv, vh, dX, dY, actionState, isCurrentlyActive);
    }
}