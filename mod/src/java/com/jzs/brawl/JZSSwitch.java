package com.jzs.brawl;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

/**
 * The menu's toggle.
 *
 * Android's own {@code Switch} is themed from XML, and this mod has no
 * resource table to theme it from, so the control is drawn by hand. The thumb
 * travels on an overshoot curve while the track cross-fades from grey to the
 * current accent - the two run off one 0..1 animator so they can never drift
 * apart mid-animation.
 */
public final class JZSSwitch extends View {

    private static final int TRACK_W_DP = 46;
    private static final int TRACK_H_DP = 26;

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF trackRect = new RectF();

    private int accent = JZSTheme.ACCENTS[0];
    private boolean checked;
    /** 0 = off, 1 = on. Drives the thumb position and the track colour. */
    private float progress;
    private ValueAnimator anim;
    private OnChanged listener;

    public JZSSwitch(Context ctx) {
        super(ctx);
        thumbPaint.setColor(0xFFFFFFFF);
        setClickable(true);
        setFocusable(true);
    }

    public void setAccent(int accent) {
        this.accent = accent;
        invalidate();
    }

    public void setOnChanged(OnChanged listener) {
        this.listener = listener;
    }

    public boolean isChecked() {
        return checked;
    }

    /** Sets the state without animating or notifying - for initial binding. */
    public void setCheckedSilently(boolean value) {
        JZSAnim.cancel(anim);
        checked = value;
        progress = value ? 1f : 0f;
        invalidate();
    }

    /** Flips the state, animates to it and notifies the listener. */
    public void toggle() {
        setChecked(!checked, true);
    }

    public void setChecked(boolean value, boolean notify) {
        if (checked == value) return;
        checked = value;
        animateTo(value ? 1f : 0f);
        if (notify && listener != null) listener.onChanged(this, value);
    }

    private void animateTo(float target) {
        JZSAnim.cancel(anim);
        if (JZSAnim.reduced(getContext())) {
            progress = target;
            invalidate();
            return;
        }
        anim = JZSAnim.progress(progress, target, JZSAnim.D_NORMAL, JZSAnim.overshoot(),
                new JZSAnim.FloatSink() {
                    @Override
                    public void onValue(float value) {
                        progress = value;
                        invalidate();
                    }
                });
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        setMeasuredDimension(
                resolveSize(JZSTheme.dp(getContext(), TRACK_W_DP), widthSpec),
                resolveSize(JZSTheme.dp(getContext(), TRACK_H_DP), heightSpec));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                JZSAnim.press(this, true);
                return true;
            case MotionEvent.ACTION_UP:
                JZSAnim.press(this, false);
                toggle();
                return true;
            case MotionEvent.ACTION_CANCEL:
                JZSAnim.press(this, false);
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        float radius = h / 2f;

        // Track: grey when off, accent when on, blended by the same progress
        // value that moves the thumb.
        trackPaint.setColor(JZSTheme.blend(JZSTheme.TRACK_OFF, accent, progress));
        trackRect.set(0f, 0f, w, h);
        canvas.drawRoundRect(trackRect, radius, radius, trackPaint);

        float inset = h * 0.14f;
        float thumbRadius = radius - inset;
        float travel = w - 2f * radius;
        float cx = radius + travel * progress;
        float cy = h / 2f;

        // Accent halo, fading in with the "on" state so the thumb looks lit
        // rather than merely moved.
        if (progress > 0.01f) {
            glowPaint.setColor(JZSTheme.withAlpha(accent, 0.45f * progress));
            canvas.drawCircle(cx, cy, thumbRadius + inset * 1.1f, glowPaint);
        }

        canvas.drawCircle(cx, cy, thumbRadius, thumbPaint);
    }

    @Override
    protected void onDetachedFromWindow() {
        JZSAnim.cancel(anim);
        anim = null;
        super.onDetachedFromWindow();
    }

    public interface OnChanged {
        void onChanged(JZSSwitch view, boolean checked);
    }
}
