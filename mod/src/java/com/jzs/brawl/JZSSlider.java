package com.jzs.brawl;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

/**
 * The menu's slider, for badge opacity and badge scale.
 *
 * Drawn by hand for the same reason as {@link JZSSwitch}: no resource table to
 * theme a {@code SeekBar} from. Touch moves the value immediately (a slider
 * that lags behind the finger feels broken), while the thumb's *size* is
 * animated - it swells on touch down and settles back on release, which is
 * the only part of a slider that should ease.
 */
public final class JZSSlider extends View {

    private static final int HEIGHT_DP = 34;
    private static final int TRACK_DP = 5;
    private static final float THUMB_IDLE_DP = 8.5f;
    private static final float THUMB_HELD_DP = 11.5f;

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private int accent = JZSTheme.ACCENTS[0];
    private float min = 0f;
    private float max = 1f;
    /** Snap step in value units; 0 disables snapping. */
    private float step = 0f;
    private float value = 0.5f;

    private float thumbRadiusDp = THUMB_IDLE_DP;
    private boolean dragging;
    private OnValue listener;

    public JZSSlider(Context ctx) {
        super(ctx);
        thumbPaint.setColor(0xFFFFFFFF);
        trackPaint.setColor(JZSTheme.TRACK_OFF);
        setClickable(true);
    }

    public void setAccent(int accent) {
        this.accent = accent;
        invalidate();
    }

    public void setRange(float min, float max, float step) {
        this.min = min;
        this.max = max;
        this.step = step;
        invalidate();
    }

    public void setOnValue(OnValue listener) {
        this.listener = listener;
    }

    public float getValue() {
        return value;
    }

    /** Sets the value without notifying - for initial binding. */
    public void setValueSilently(float v) {
        value = clamp(v);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        setMeasuredDimension(
                resolveSize(JZSTheme.dp(getContext(), 200), widthSpec),
                resolveSize(JZSTheme.dp(getContext(), HEIGHT_DP), heightSpec));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dragging = true;
                // The slider lives inside a scrolling tab body; claim the
                // gesture or the first drag turns into a scroll.
                getParent().requestDisallowInterceptTouchEvent(true);
                animateThumb(THUMB_HELD_DP);
                updateFromX(event.getX(), true);
                return true;
            case MotionEvent.ACTION_MOVE:
                if (dragging) updateFromX(event.getX(), true);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                dragging = false;
                getParent().requestDisallowInterceptTouchEvent(false);
                animateThumb(THUMB_IDLE_DP);
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    private void animateThumb(final float targetDp) {
        if (JZSAnim.reduced(getContext())) {
            thumbRadiusDp = targetDp;
            invalidate();
            return;
        }
        JZSAnim.progress(thumbRadiusDp, targetDp, JZSAnim.D_FAST, JZSAnim.overshoot(),
                new JZSAnim.FloatSink() {
                    @Override
                    public void onValue(float v) {
                        thumbRadiusDp = v;
                        invalidate();
                    }
                });
    }

    private void updateFromX(float x, boolean notify) {
        float pad = JZSTheme.dpf(getContext(), THUMB_HELD_DP);
        float usable = getWidth() - 2f * pad;
        if (usable <= 0f) return;
        float t = (x - pad) / usable;
        if (t < 0f) t = 0f;
        if (t > 1f) t = 1f;

        float raw = min + t * (max - min);
        if (step > 0f) raw = min + Math.round((raw - min) / step) * step;
        float next = clamp(raw);

        if (next != value) {
            value = next;
            invalidate();
            if (notify && listener != null) listener.onValue(this, value);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        float pad = JZSTheme.dpf(getContext(), THUMB_HELD_DP);
        float usable = w - 2f * pad;
        float cy = h / 2f;
        float trackH = JZSTheme.dpf(getContext(), TRACK_DP);
        float trackR = trackH / 2f;
        float t = (max > min) ? (value - min) / (max - min) : 0f;
        float cx = pad + usable * t;

        rect.set(pad, cy - trackR, pad + usable, cy + trackR);
        canvas.drawRoundRect(rect, trackR, trackR, trackPaint);

        fillPaint.setColor(accent);
        rect.set(pad, cy - trackR, cx, cy + trackR);
        canvas.drawRoundRect(rect, trackR, trackR, fillPaint);

        float thumbR = JZSTheme.dpf(getContext(), thumbRadiusDp);
        glowPaint.setColor(JZSTheme.withAlpha(accent, dragging ? 0.5f : 0.3f));
        canvas.drawCircle(cx, cy, thumbR * 1.55f, glowPaint);
        canvas.drawCircle(cx, cy, thumbR, thumbPaint);
    }

    private float clamp(float v) {
        return v < min ? min : (v > max ? max : v);
    }

    public interface OnValue {
        void onValue(JZSSlider view, float value);
    }
}
