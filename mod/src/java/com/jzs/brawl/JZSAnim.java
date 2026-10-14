package com.jzs.brawl;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.PathInterpolator;
import android.os.Build;

/**
 * The mod's motion vocabulary.
 *
 * Two rules hold everywhere in here:
 *
 *  - Nothing animates if the device asks it not to. When the user turns
 *    animations off in developer options (or turns on the mod's own "reduce
 *    motion"), every helper jumps straight to the end state instead of
 *    running a timeline. A menu that silently never finishes animating is
 *    worse than one that never animates.
 *  - Views get a hardware layer for the duration of a transform animation and
 *    lose it on end *and* on cancel, so an interrupted open never leaks a
 *    layer onto the game's own draw pass.
 */
public final class JZSAnim {

    /** Scrim fade, tab cross-fades. */
    public static final int D_FAST = 140;
    /** Row stagger, switch thumb, press feedback. */
    public static final int D_NORMAL = 220;
    /** Panel open. */
    public static final int D_SLOW = 340;

    /** Delay between two consecutive rows entering. */
    public static final int STAGGER = 38;

    private static final ArgbEvaluator ARGB = new ArgbEvaluator();

    private JZSAnim() {}

    /* ------------------------------------------------------- interpolators */

    /** Settles past the target then eases back - the panel's "snap open". */
    public static Interpolator overshoot() {
        return new OvershootInterpolator(1.5f);
    }

    /** Standard "emphasized decelerate"; the workhorse for entrances. */
    public static Interpolator enter() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new PathInterpolator(0.05f, 0.7f, 0.1f, 1f);
        }
        return new DecelerateInterpolator(2.2f);
    }

    /** Quick, linear-ish out: exits should not linger. */
    public static Interpolator exit() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new PathInterpolator(0.3f, 0f, 0.8f, 0.15f);
        }
        return new AccelerateInterpolator(1.6f);
    }

    /* --------------------------------------------------------- motion gate */

    private static boolean sForceReduced;

    /** Mirrors the mod's own "reduce motion" switch. */
    public static void setReducedMotion(boolean reduced) {
        sForceReduced = reduced;
    }

    /**
     * True when the system animator duration scale is 0 (animations off in
     * developer options) or the user asked the mod to calm down.
     */
    public static boolean reduced(Context ctx) {
        if (sForceReduced) return true;
        try {
            float scale = Settings.Global.getFloat(
                    ctx.getContentResolver(),
                    Settings.Global.ANIMATOR_DURATION_SCALE, 1f);
            return scale == 0f;
        } catch (Throwable ignored) {
            return false;
        }
    }

    /* ----------------------------------------------------------- entrances */

    /** Fade + scale + lift: how the whole panel arrives. */
    public static void popIn(final View v, int durationMs, int startDelayMs, float fromScale,
                             float fromTranslationY) {
        if (v == null) return;
        if (reduced(v.getContext())) {
            v.setAlpha(1f);
            v.setScaleX(1f);
            v.setScaleY(1f);
            v.setTranslationY(0f);
            v.setVisibility(View.VISIBLE);
            return;
        }
        v.setVisibility(View.VISIBLE);
        v.setAlpha(0f);
        v.setScaleX(fromScale);
        v.setScaleY(fromScale);
        v.setTranslationY(fromTranslationY);
        layerOn(v);
        v.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationY(0f)
                .setStartDelay(startDelayMs)
                .setDuration(durationMs)
                .setInterpolator(overshoot())
                .setListener(new AnimatorListenerAdapter() {
                    @Override public void onAnimationEnd(Animator a) { layerOff(v); }
                    @Override public void onAnimationCancel(Animator a) { layerOff(v); }
                })
                .start();
    }

    /** Slide in from the left while fading up: how a single row arrives. */
    public static void slideInFromLeft(final View v, int durationMs, int startDelayMs,
                                       float fromTranslationX) {
        if (v == null) return;
        if (reduced(v.getContext())) {
            v.setAlpha(1f);
            v.setTranslationX(0f);
            return;
        }
        v.setAlpha(0f);
        v.setTranslationX(fromTranslationX);
        layerOn(v);
        v.animate()
                .alpha(1f)
                .translationX(0f)
                .setStartDelay(startDelayMs)
                .setDuration(durationMs)
                .setInterpolator(enter())
                .setListener(new AnimatorListenerAdapter() {
                    @Override public void onAnimationEnd(Animator a) { layerOff(v); }
                    @Override public void onAnimationCancel(Animator a) { layerOff(v); }
                })
                .start();
    }

    /**
     * Runs {@link #slideInFromLeft} over every child of {@code group}, each one
     * {@link #STAGGER} ms behind the last. This is the cascade the menu shows
     * when a tab opens.
     */
    public static void staggerChildren(ViewGroup group, int startDelayMs) {
        if (group == null) return;
        Context ctx = group.getContext();
        int offset = JZSTheme.dp(ctx, 22);
        for (int i = 0; i < group.getChildCount(); i++) {
            slideInFromLeft(group.getChildAt(i), D_NORMAL, startDelayMs + i * STAGGER, offset);
        }
    }

    /** Fade to {@code alpha}; no transform, so no layer needed. */
    public static void fadeTo(View v, float alpha, int durationMs) {
        if (v == null) return;
        if (reduced(v.getContext())) {
            v.setAlpha(alpha);
            return;
        }
        v.animate().alpha(alpha)
                .setDuration(durationMs)
                .setInterpolator(enter())
                .setListener(null)
                .start();
    }

    /** Fade + shrink out, then hand control back via {@code onEnd}. */
    public static void popOut(final View v, int durationMs, final float toScale,
                              final float toTranslationY, final Runnable onEnd) {
        if (v == null) {
            if (onEnd != null) onEnd.run();
            return;
        }
        if (reduced(v.getContext())) {
            v.setVisibility(View.GONE);
            if (onEnd != null) onEnd.run();
            return;
        }
        layerOn(v);
        v.animate()
                .alpha(0f)
                .scaleX(toScale)
                .scaleY(toScale)
                .translationY(toTranslationY)
                .setStartDelay(0)
                .setDuration(durationMs)
                .setInterpolator(exit())
                .setListener(new AnimatorListenerAdapter() {
                    private boolean done;
                    @Override public void onAnimationEnd(Animator a) { finish(); }
                    @Override public void onAnimationCancel(Animator a) { finish(); }
                    private void finish() {
                        if (done) return;
                        done = true;
                        layerOff(v);
                        v.setVisibility(View.GONE);
                        if (onEnd != null) onEnd.run();
                    }
                })
                .start();
    }

    /* -------------------------------------------------------------- repeats */

    /**
     * Slow breathing scale, used by the launcher bubble so it reads as "tap
     * me" without ever being loud. Returns the animator so the caller can
     * cancel it when the bubble goes away; null when motion is reduced.
     */
    public static ValueAnimator breathe(final View v, final float from, final float to,
                                        int periodMs) {
        if (v == null || reduced(v.getContext())) return null;
        ValueAnimator anim = ValueAnimator.ofFloat(from, to);
        anim.setDuration(periodMs);
        anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.setRepeatMode(ValueAnimator.REVERSE);
        anim.setInterpolator(new DecelerateInterpolator(1.4f));
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator a) {
                float s = (Float) a.getAnimatedValue();
                v.setScaleX(s);
                v.setScaleY(s);
            }
        });
        anim.start();
        return anim;
    }

    /* ------------------------------------------------------------- tweening */

    /** ARGB tween, for switch tracks and accent swaps. */
    public static ValueAnimator colors(int from, int to, int durationMs,
                                       final ColorSink sink) {
        ValueAnimator anim = ValueAnimator.ofObject(ARGB, from, to);
        anim.setDuration(durationMs);
        anim.setInterpolator(enter());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator a) {
                sink.onColor((Integer) a.getAnimatedValue());
            }
        });
        anim.start();
        return anim;
    }

    /** Plain 0..1 tween, for anything a view has to repaint by hand. */
    public static ValueAnimator progress(float from, float to, int durationMs,
                                         Interpolator interpolator, final FloatSink sink) {
        ValueAnimator anim = ValueAnimator.ofFloat(from, to);
        anim.setDuration(durationMs);
        anim.setInterpolator(interpolator);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator a) {
                sink.onValue((Float) a.getAnimatedValue());
            }
        });
        anim.start();
        return anim;
    }

    /* ------------------------------------------------------ touch feedback */

    /** Press/release scale dip. Short enough to feel like a button, not a toy. */
    public static void press(View v, boolean pressed) {
        if (v == null) return;
        float target = pressed ? 0.965f : 1f;
        if (reduced(v.getContext())) {
            v.setScaleX(target);
            v.setScaleY(target);
            return;
        }
        v.animate().scaleX(target).scaleY(target)
                .setDuration(pressed ? 90 : 160)
                .setInterpolator(pressed ? exit() : overshoot())
                .setListener(null)
                .start();
    }

    /** Cancels an animator without caring whether it exists or already ended. */
    public static void cancel(Animator anim) {
        if (anim != null) {
            try { anim.cancel(); } catch (Throwable ignored) { }
        }
    }

    /* --------------------------------------------------------------- layers */

    private static void layerOn(View v) {
        try { v.setLayerType(View.LAYER_TYPE_HARDWARE, null); } catch (Throwable ignored) { }
    }

    private static void layerOff(View v) {
        try { v.setLayerType(View.LAYER_TYPE_NONE, null); } catch (Throwable ignored) { }
    }

    /* ---------------------------------------------------------- callbacks */

    public interface ColorSink { void onColor(int color); }

    public interface FloatSink { void onValue(float value); }
}
