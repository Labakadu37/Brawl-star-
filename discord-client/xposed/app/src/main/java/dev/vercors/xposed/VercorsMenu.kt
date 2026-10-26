package dev.vercors.xposed

import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.TextView
import kotlin.math.abs

/**
 * Native Android UI drawn over Discord: a draggable "V" button opening the
 * plugin list and a log viewer. It doesn't depend on Discord's JS, so it works
 * even when the mod fails to load, which is when the logs matter most.
 */
object VercorsMenu {
    private const val BUTTON_TAG = "vercors-menu-button"
    private const val DIALOG_THEME = android.R.style.Theme_DeviceDefault_Dialog_Alert

    fun attach(activity: Activity, settings: Settings) {
        val root = activity.window?.decorView as? ViewGroup ?: return
        if (root.findViewWithTag<View>(BUTTON_TAG) != null) return

        val size = activity.dp(40)
        val button = TextView(activity).apply {
            tag = BUTTON_TAG
            text = "V"
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.argb(200, 88, 101, 242))
            }
            elevation = activity.dp(6).toFloat()
            contentDescription = "Vercors"
        }
        val params = FrameLayout.LayoutParams(size, size, Gravity.TOP or Gravity.END).apply {
            topMargin = activity.resources.displayMetrics.heightPixels / 3
            marginEnd = activity.dp(4)
        }
        button.setOnTouchListener(DragToMove { showMainDialog(activity, settings) })
        root.addView(button, params)
    }

    private fun showMainDialog(activity: Activity, settings: Settings) {
        val plugins = settings.plugins
        val checked = BooleanArray(plugins.size) { settings.isEnabled(plugins[it]) }
        val labels = plugins.map { plugin ->
            SpannableStringBuilder().apply {
                append(plugin.name, StyleSpan(Typeface.BOLD), 0)
                append("\n")
                append(plugin.description, RelativeSizeSpan(0.8f), 0)
            }
        }.toTypedArray<CharSequence>()

        val builder = AlertDialog.Builder(activity, DIALOG_THEME)
            .setTitle("Vercors v$LOADER_VERSION")
            .setNeutralButton("Journal") { _, _ -> showLogs(activity) }
            .setNegativeButton("Fermer", null)

        if (plugins.isEmpty()) {
            builder.setMessage("Liste des plugins introuvable dans le module.")
        } else {
            builder.setMultiChoiceItems(labels, checked) { _, which, isChecked -> checked[which] = isChecked }
                .setPositiveButton("Appliquer") { _, _ ->
                    settings.setEnabled(plugins.indices.associate { plugins[it].name to checked[it] })
                    restart(activity)
                }
        }
        builder.show()
    }

    private fun showLogs(activity: Activity) {
        val text = TextView(activity).apply {
            typeface = Typeface.MONOSPACE
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            setTextIsSelectable(true)
            setPadding(activity.dp(12), activity.dp(8), activity.dp(12), activity.dp(8))
            text = "Lecture des journaux…"
        }
        val scroll = ScrollView(activity).apply { addView(text) }
        val dialog = AlertDialog.Builder(activity, DIALOG_THEME)
            .setTitle("Journal Vercors")
            .setView(scroll)
            .setPositiveButton("Copier") { _, _ ->
                val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Vercors", text.text))
            }
            .setNegativeButton("Fermer", null)
            .show()

        Thread {
            val logs = readLogs()
            Handler(Looper.getMainLooper()).post {
                if (!dialog.isShowing) return@post
                text.text = logs.ifBlank { "Aucun journal Vercors trouvé." }
                scroll.post { scroll.fullScroll(View.FOCUS_DOWN) }
            }
        }.start()
    }

    /** An app may read its own process' logcat without any permission. */
    private fun readLogs(): String = runCatching {
        val process = Runtime.getRuntime().exec(arrayOf("logcat", "-d", "-v", "time", "-t", "3000", "--pid=${Process.myPid()}"))
        val interesting = Regex("Vercors|ReactNative|AndroidRuntime|LSPatch|LSPosed|Hermes", RegexOption.IGNORE_CASE)
        process.inputStream.bufferedReader().useLines { lines ->
            lines.filter { interesting.containsMatchIn(it) }.toList().takeLast(300).joinToString("\n")
        }
    }.getOrElse { "Impossible de lire les journaux : $it" }

    private fun restart(activity: Activity) {
        val launch = activity.packageManager.getLaunchIntentForPackage(activity.packageName) ?: return
        val intent = Intent.makeRestartActivityTask(launch.component)
        val pending = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT)
        val alarms = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarms.set(AlarmManager.RTC, System.currentTimeMillis() + 300, pending)
        activity.finishAffinity()
        Process.killProcess(Process.myPid())
    }

    private fun Context.dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    /** Moves the view vertically while dragging; a short touch counts as a click. */
    private class DragToMove(private val onClick: () -> Unit) : View.OnTouchListener {
        private var startY = 0f
        private var startTranslation = 0f
        private var dragging = false

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    startY = event.rawY
                    startTranslation = view.translationY
                    dragging = false
                }
                MotionEvent.ACTION_MOVE -> {
                    val dy = event.rawY - startY
                    if (abs(dy) > ViewConfiguration.get(view.context).scaledTouchSlop) dragging = true
                    if (dragging) view.translationY = startTranslation + dy
                }
                MotionEvent.ACTION_UP -> if (!dragging) onClick()
            }
            return true
        }
    }
}
