package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TaskTouchCallback extends ItemTouchHelper.Callback {

    public interface Actions {
        void onSwipeComplete(Task task);               // swipe LEFT
        void onSwipeDelete(Task task);                 // swipe RIGHT
        void onOrderChanged(List<Task> displayOrder);  // after a drag finishes
    }

    private final TaskAdapter adapter;
    private final Actions actions;
    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final float density;
    private boolean moved = false;

    public TaskTouchCallback(Context ctx, TaskAdapter adapter, Actions actions) {
        this.adapter = adapter;
        this.actions = actions;
        this.density = ctx.getResources().getDisplayMetrics().density;
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextSize(14 * density);
        textPaint.setFakeBoldText(true);
    }

    /** Header rows get no movement flags at all: they can never be swiped or dragged. */
    @Override
    public int getMovementFlags(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh) {
        if (vh.getItemViewType() == TaskListItem.TYPE_HEADER) return makeMovementFlags(0, 0);
        return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
    }

    @Override public boolean isLongPressDragEnabled() { return true; }   // press and hold to drag
    @Override public boolean isItemViewSwipeEnabled() { return true; }

    /** A row may only be dropped over another task row of the same status group. */
    private boolean sameGroupTask(RecyclerView.ViewHolder a, RecyclerView.ViewHolder b) {
        int pa = a.getAdapterPosition(), pb = b.getAdapterPosition();
        if (pa == RecyclerView.NO_POSITION || pb == RecyclerView.NO_POSITION) return false;
        TaskListItem x = adapter.getItem(pa), y = adapter.getItem(pb);
        return !x.isHeader() && !y.isHeader() && x.groupKey.equals(y.groupKey);
    }

    @Override
    public boolean canDropOver(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder current,
                               @NonNull RecyclerView.ViewHolder target) {
        return sameGroupTask(current, target);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh,
                          @NonNull RecyclerView.ViewHolder target) {
        if (!sameGroupTask(vh, target)) return false;   // never across a header or into another group
        adapter.moveItem(vh.getAdapterPosition(), target.getAdapterPosition());
        moved = true;
        return true;
    }

    @Override
    public void clearView(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh) {
        super.clearView(rv, vh);
        if (moved) {
            moved = false;
            actions.onOrderChanged(adapter.tasksInDisplayOrder());
        }
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direction) {
        int pos = vh.getAdapterPosition();
        if (pos == RecyclerView.NO_POSITION) return;
        TaskListItem item = adapter.getItem(pos);
        if (item.isHeader() || item.task == null) { adapter.notifyItemChanged(pos); return; }
        if (direction == ItemTouchHelper.LEFT) actions.onSwipeComplete(item.task);
        else if (direction == ItemTouchHelper.RIGHT) actions.onSwipeDelete(item.task);
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX != 0) {
            View v = vh.itemView;
            float radius = 14 * density;
            String label;
            float textX;
            if (dX < 0) {   // swiping left = complete
                bgPaint.setColor(0xFF10B981);
                rect.set(v.getRight() + dX, v.getTop(), v.getRight(), v.getBottom());
                label = "Done";
                textX = v.getRight() - 16 * density - textPaint.measureText(label);
            } else {        // swiping right = delete
                bgPaint.setColor(0xFFEF4444);
                rect.set(v.getLeft(), v.getTop(), v.getLeft() + dX, v.getBottom());
                label = "Delete";
                textX = v.getLeft() + 16 * density;
            }
            c.drawRoundRect(rect, radius, radius, bgPaint);
            float textY = v.getTop() + v.getHeight() / 2f - (textPaint.descent() + textPaint.ascent()) / 2f;
            c.save();
            c.clipRect(rect);
            c.drawText(label, textX, textY, textPaint);
            c.restore();
        }
        super.onChildDraw(c, rv, vh, dX, dY, actionState, isCurrentlyActive);
    }
}