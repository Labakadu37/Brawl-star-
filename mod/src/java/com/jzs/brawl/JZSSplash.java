package com.jzs.brawl;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import java.util.Random;

/**
 * Animated intro shown once per app launch, on top of the game's loading screen.
 *
 * Timeline (ms):
 *   0-700     "JZS" pops in with an overshoot, rainbow gradient scrolling through it
 *   350-1250  "BRAWL" drops in letter by letter
 *   1250-1700 light shine sweeps across the logo
 *   0-2400    sparks burst out, loading bar fills
 *   2400-2900 everything fades out, then the view removes itself
 * A tap anywhere skips straight to the fade.
 */
public final class JZSSplash extends View {

    private static final long TOTAL = 2900L;
    private static final long FADE_START = 2400L;
    private static final long SKIP_FADE = 300L;
    private static final int SPARKS = 46;

    private static boolean sShown;

    private final Runnable mOnDone;
    private final long mStart = SystemClock.uptimeMillis();
    private long mFadeFrom = FADE_START;
    private long mEnd = TOTAL;
    private boolean mFinished;

    private final Paint mBg = new Paint();
    private final Paint mText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mGlow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mSpark = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mBar = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mSmall = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Matrix mShaderMatrix = new Matrix();
    private final RectF mRect = new RectF();

    private final float[] mSparkAngle = new float[SPARKS];
    private final float[] mSparkSpeed = new float[SPARKS];
    private final float[] mSparkSize = new float[SPARKS];
    private final int[] mSparkColor = new int[SPARKS];
    private final long[] mSparkDelay = new long[SPARKS];

    private LinearGradient mRainbow;
    private int mRainbowWidth;

    /** Shows the intro once per process; runs {@code onDone} on the UI thread when it is gone. */
    public static void show(final Activity activity, final Runnable onDone) {
        if (activity == null) return;
        if (sShown) {
            if (onDone != null) activity.runOnUiThread(onDone);
            return;
        }
        sShown = true;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JZSSplash splash = new JZSSplash(activity, onDone);
                    activity.getWindow().addContentView(splash, new WindowManager.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
                    splash.bringToFront();
                } catch (Throwable t) {
                    android.util.Log.e("JZS", "splash failed", t);
                    if (onDone != null) onDone.run();
                }
            }
        });
    }

    private JZSSplash(Context ctx, Runnable onDone) {
        super(ctx);
        mOnDone = onDone;
        setClickable(true);

        mText.setTypeface(JZSUi.titleFont(ctx));
        mText.setTextAlign(Paint.Align.CENTER);
        mStroke.setTypeface(JZSUi.titleFont(ctx));
        mStroke.setTextAlign(Paint.Align.CENTER);
        mStroke.setStyle(Paint.Style.STROKE);
        mStroke.setStrokeJoin(Paint.Join.ROUND);
        mStroke.setColor(Color.BLACK);
        mSmall.setTypeface(JZSUi.titleFont(ctx));
        mSmall.setTextAlign(Paint.Align.CENTER);

        Random rnd = new Random();
        for (int i = 0; i < SPARKS; i++) {
            mSparkAngle[i] = (float) (rnd.nextFloat() * Math.PI * 2);
            mSparkSpeed[i] = 0.35f + rnd.nextFloat() * 0.65f;
            mSparkSize[i] = 1.5f + rnd.nextFloat() * 3f;
            mSparkColor[i] = JZSUi.RAINBOW[rnd.nextInt(JZSUi.RAINBOW.length - 1)];
            mSparkDelay[i] = (long) (rnd.nextFloat() * 500);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            long t = elapsed();
            if (t < mFadeFrom) {
                mFadeFrom = t;
                mEnd = t + SKIP_FADE;
            }
        }
        return true;
    }

    private long elapsed() {
        return SystemClock.uptimeMillis() - mStart;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mBg.setShader(new RadialGradient(w / 2f, h / 2f, Math.max(w, h) * 0.75f,
                JZSUi.BG_DARK, JZSUi.BG_DEEP, Shader.TileMode.CLAMP));
        mRainbowWidth = Math.max(1, w / 2);
        mRainbow = new LinearGradient(0, 0, mRainbowWidth, 0,
                JZSUi.RAINBOW, null, Shader.TileMode.MIRROR);
    }

    @Override
    protected void onDraw(Canvas c) {
        long t = elapsed();
        if (t >= mEnd) {
            finish();
            return;
        }

        float w = getWidth();
        float h = getHeight();
        float cx = w / 2f;
        float cy = h * 0.44f;
        float unit = Math.min(w, h);

        float fade = t < mFadeFrom ? 1f : 1f - clamp((t - mFadeFrom) / (float) (mEnd - mFadeFrom));
        int alpha = (int) (255 * fade);

        // Background
        mBg.setAlpha(alpha);
        c.drawRect(0, 0, w, h, mBg);

        // Pulsing glow ring behind the logo
        float pulse = 0.5f + 0.5f * (float) Math.sin(t / 260.0);
        float ringR = unit * (0.26f + 0.02f * pulse) * easeOutBack(clamp(t / 700f));
        mGlow.setStyle(Paint.Style.STROKE);
        mGlow.setStrokeWidth(JZSUi.dp(getContext(), 3));
        mGlow.setColor(JZSUi.BLUE);
        mGlow.setAlpha((int) (alpha * (0.25f + 0.25f * pulse)));
        c.drawCircle(cx, cy, ringR, mGlow);
        mGlow.setStrokeWidth(JZSUi.dp(getContext(), 14));
        mGlow.setAlpha((int) (alpha * 0.06f));
        c.drawCircle(cx, cy, ringR, mGlow);

        // Sparks
        for (int i = 0; i < SPARKS; i++) {
            float st = clamp((t - mSparkDelay[i]) / 1600f);
            if (st <= 0f || st >= 1f) continue;
            float dist = unit * 0.12f + unit * 0.5f * mSparkSpeed[i] * easeOutCubic(st);
            float sx = cx + (float) Math.cos(mSparkAngle[i]) * dist;
            float sy = cy + (float) Math.sin(mSparkAngle[i]) * dist;
            mSpark.setColor(mSparkColor[i]);
            mSpark.setAlpha((int) (alpha * (1f - st)));
            c.drawCircle(sx, sy, JZSUi.dp(getContext(), mSparkSize[i]), mSpark);
        }

        // "JZS" — pops in, rainbow scrolls through it
        float logoSize = unit * 0.26f;
        float pop = easeOutBack(clamp(t / 700f));
        if (pop > 0f) {
            c.save();
            c.scale(pop, pop, cx, cy);
            mStroke.setTextSize(logoSize);
            mStroke.setStrokeWidth(logoSize * 0.09f);
            mStroke.setAlpha(alpha);
            c.drawText("JZS", cx, cy + logoSize * 0.35f, mStroke);

            mShaderMatrix.setTranslate((t * 0.35f) % (mRainbowWidth * 2f), 0);
            mRainbow.setLocalMatrix(mShaderMatrix);
            mText.setShader(mRainbow);
            mText.setTextSize(logoSize);
            mText.setAlpha(alpha);
            c.drawText("JZS", cx, cy + logoSize * 0.35f, mText);
            mText.setShader(null);

            // Shine sweep
            float shine = clamp((t - 1250f) / 450f);
            if (shine > 0f && shine < 1f) {
                float half = mText.measureText("JZS") / 2f;
                float sx = cx - half - logoSize + (half * 2f + logoSize * 2f) * shine;
                LinearGradient lg = new LinearGradient(sx - logoSize * 0.3f, 0, sx + logoSize * 0.3f, 0,
                        new int[]{0x00FFFFFF, 0xCCFFFFFF, 0x00FFFFFF}, null, Shader.TileMode.CLAMP);
                mText.setShader(lg);
                mText.setAlpha(alpha);
                c.drawText("JZS", cx, cy + logoSize * 0.35f, mText);
                mText.setShader(null);
            }
            c.restore();
        }

        // "BRAWL" — letters drop in one after the other
        String brawl = "BRAWL";
        float subSize = unit * 0.1f;
        mText.setTextSize(subSize);
        mStroke.setTextSize(subSize);
        mStroke.setStrokeWidth(subSize * 0.14f);
        float totalW = mText.measureText(brawl) + subSize * 0.12f * (brawl.length() - 1);
        float x = cx - totalW / 2f;
        float baseY = cy + logoSize * 0.35f + subSize * 1.25f;
        for (int i = 0; i < brawl.length(); i++) {
            String ch = String.valueOf(brawl.charAt(i));
            float cw = mText.measureText(ch);
            float lt = clamp((t - 350f - i * 110f) / 380f);
            if (lt > 0f) {
                float dy = (1f - easeOutBack(lt)) * -subSize * 1.4f;
                int la = (int) (alpha * clamp(lt * 2f));
                mStroke.setAlpha(la);
                c.drawText(ch, x + cw / 2f, baseY + dy, mStroke);
                mText.setColor(JZSUi.YELLOW);
                mText.setAlpha(la);
                c.drawText(ch, x + cw / 2f, baseY + dy, mText);
            }
            x += cw + subSize * 0.12f;
        }

        // Loading bar
        float barW = unit * 0.55f;
        float barH = JZSUi.dp(getContext(), 8);
        float barY = h * 0.84f;
        float progress = easeInOut(clamp(t / (float) FADE_START));
        mRect.set(cx - barW / 2f, barY, cx + barW / 2f, barY + barH);
        mBar.setShader(null);
        mBar.setColor(JZSUi.CARD_BORDER);
        mBar.setAlpha(alpha);
        c.drawRoundRect(mRect, barH / 2f, barH / 2f, mBar);
        mRect.right = mRect.left + Math.max(barH, barW * progress);
        mBar.setShader(mRainbow);
        mBar.setAlpha(alpha);
        c.drawRoundRect(mRect, barH / 2f, barH / 2f, mBar);
        mBar.setShader(null);

        mSmall.setTextSize(JZSUi.dp(getContext(), 14));
        mSmall.setColor(Color.WHITE);
        mSmall.setAlpha(alpha);
        String status = progress < 1f
                ? JZSUi.tr("Loading", "Chargement") + dots(t)
                : JZSUi.tr("Ready!", "Prêt !");
        c.drawText(status, cx, barY - JZSUi.dp(getContext(), 10), mSmall);

        mSmall.setTextSize(JZSUi.dp(getContext(), 12));
        mSmall.setColor(JZSUi.TEXT_DIM);
        mSmall.setAlpha(alpha);
        c.drawText(JZSOverlay.MOD_VERSION + "  •  "
                        + JZSUi.tr("Free, never paid", "Gratuit, jamais payant"),
                cx, barY + barH + JZSUi.dp(getContext(), 22), mSmall);

        postInvalidateOnAnimation();
    }

    private void finish() {
        if (mFinished) return;
        mFinished = true;
        setVisibility(GONE);
        post(new Runnable() {
            @Override
            public void run() {
                if (getParent() instanceof ViewGroup) {
                    ((ViewGroup) getParent()).removeView(JZSSplash.this);
                }
                if (mOnDone != null) {
                    try {
                        mOnDone.run();
                    } catch (Throwable t) {
                        android.util.Log.e("JZS", "after-splash callback failed", t);
                    }
                }
            }
        });
    }

    private static String dots(long t) {
        int n = (int) ((t / 300) % 4);
        return n == 0 ? "" : n == 1 ? "." : n == 2 ? ".." : "...";
    }

    private static float clamp(float v) {
        return v < 0f ? 0f : v > 1f ? 1f : v;
    }

    private static float easeOutCubic(float x) {
        float a = 1f - x;
        return 1f - a * a * a;
    }

    private static float easeInOut(float x) {
        return x < 0.5f ? 2f * x * x : 1f - (float) Math.pow(-2f * x + 2f, 2) / 2f;
    }

    private static float easeOutBack(float x) {
        if (x <= 0f) return 0f;
        float c1 = 1.70158f;
        float c3 = c1 + 1f;
        return 1f + c3 * (float) Math.pow(x - 1f, 3) + c1 * (float) Math.pow(x - 1f, 2);
    }
}
