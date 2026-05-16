package com.botnoitu.keyboard

import android.content.ClipboardManager
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.Normalizer
import kotlin.random.Random

class BotKeyboardService : InputMethodService() {

    private lateinit var tvStatus: TextView
    private lateinit var tvModeBadge: TextView
    private lateinit var viewDot: View
    private lateinit var layoutBotControl: View
    private lateinit var layoutQwerty: View

    private var isPaused = false
    private var isStealthMode = false       // true = QWERTY mode, false = Bot mode
    private var isShiftOn = false
    private var currentMode = Mode.SMART_RANDOM

    // ── Telex engine state ──────────────────────────────────────────
    private var currentWord = StringBuilder()   // buffer chữ đang gõ

    // Bảng tổ hợp âm (diacritics) Telex
    private val toneMap = mapOf("s" to "\u0301", "f" to "\u0300", "r" to "\u0309", "x" to "\u0303", "j" to "\u0323")
    private val modifierMap = mapOf(
        "aa" to "â", "aw" to "ă",
        "ee" to "ê",
        "oo" to "ô", "ow" to "ơ",
        "uw" to "ư", "uw" to "ư",
        "dd" to "đ"
    )

    private val phrases = mutableListOf<Phrase>()
    private val phraseSet = mutableSetOf<String>()
    private val byFirst = mutableMapOf<String, MutableList<Int>>()

    enum class Mode { TOP_TIER, SMART_RANDOM, BOTTOM_TIER }

    data class Phrase(val text: String, val last: String)
    data class RankedPhrase(val phrase: String, val nextCount: Int)

    override fun onCreate() {
        super.onCreate()
        loadDictionary()
    }

    override fun onCreateInputView(): View {
        val view = layoutInflater.inflate(R.layout.keyboard_view, null)

        tvStatus = view.findViewById(R.id.tvStatus)
        tvModeBadge = view.findViewById(R.id.tvModeBadge)
        viewDot = view.findViewById(R.id.viewDot)
        layoutBotControl = view.findViewById(R.id.layoutBotControl)
        layoutQwerty = view.findViewById(R.id.layoutQwerty)

        // Pulsing dot animation
        val animator = android.animation.ObjectAnimator.ofFloat(viewDot, "alpha", 1f, 0.2f, 1f)
        animator.duration = 1500
        animator.repeatCount = android.animation.ObjectAnimator.INFINITE
        animator.start()

        val btnMode = view.findViewById<Button>(R.id.btnMode)
        val btnPause = view.findViewById<Button>(R.id.btnPause)
        val btnSwitch = view.findViewById<Button>(R.id.btnSwitch)
        val btnStealthToggle = view.findViewById<TextView>(R.id.btnStealthToggle)

        updateUI(btnMode, btnPause)

        // ── Stealth Toggle ──────────────────────────────────────────
        btnStealthToggle.setOnClickListener {
            isStealthMode = !isStealthMode
            currentWord.clear()
            if (isStealthMode) {
                layoutBotControl.visibility = View.GONE
                layoutQwerty.visibility = View.VISIBLE
                tvModeBadge.visibility = View.GONE
                btnStealthToggle.text = "🤖"
                tvStatus.text = "Chế độ gõ tay. Nhấn 🤖 để bật Bot."
            } else {
                layoutBotControl.visibility = View.VISIBLE
                layoutQwerty.visibility = View.GONE
                tvModeBadge.visibility = View.VISIBLE
                btnStealthToggle.text = "⌨"
                updateUI(btnMode, btnPause)
            }
        }

        // ── Bot Control Buttons ─────────────────────────────────────
        btnMode.setOnClickListener {
            currentMode = when (currentMode) {
                Mode.TOP_TIER     -> Mode.SMART_RANDOM
                Mode.SMART_RANDOM -> Mode.BOTTOM_TIER
                Mode.BOTTOM_TIER  -> Mode.TOP_TIER
            }
            isPaused = false
            updateUI(btnMode, btnPause)
        }

        btnPause.setOnClickListener {
            isPaused = !isPaused
            updateUI(btnMode, btnPause)
        }

        btnSwitch.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        }

        // ── QWERTY Keys ─────────────────────────────────────────────
        val keyShift = view.findViewById<Button>(R.id.keyShift)
        val keyDelete = view.findViewById<Button>(R.id.keyDelete)
        val keySpace = view.findViewById<Button>(R.id.keySpace)
        val keyEnter = view.findViewById<Button>(R.id.keyEnter)
        val keyComma = view.findViewById<Button>(R.id.keyComma)
        val keyDot = view.findViewById<Button>(R.id.keyDot)

        keyShift.setOnClickListener {
            isShiftOn = !isShiftOn
            keyShift.text = if (isShiftOn) "⇪" else "⇧"
        }

        keyDelete.setOnClickListener { handleDelete() }
        keySpace.setOnClickListener { handleCharInput(" ") }
        keyEnter.setOnClickListener {
            currentInputConnection?.commitText("\n", 1)
            currentWord.clear()
        }
        keyComma.setOnClickListener { handleCharInput(",") }
        keyDot.setOnClickListener { handleCharInput(".") }

        // Gán listener cho tất cả phím chữ
        val letterKeys = mapOf(
            R.id.keyQ to "q", R.id.keyW to "w", R.id.keyE to "e",
            R.id.keyR to "r", R.id.keyT to "t", R.id.keyY to "y",
            R.id.keyU to "u", R.id.keyI to "i", R.id.keyO to "o",
            R.id.keyP to "p", R.id.keyA to "a", R.id.keyS to "s",
            R.id.keyD to "d", R.id.keyF to "f", R.id.keyG to "g",
            R.id.keyH to "h", R.id.keyJ to "j", R.id.keyK to "k",
            R.id.keyL to "l", R.id.keyZ to "z", R.id.keyX to "x",
            R.id.keyC to "c", R.id.keyV to "v", R.id.keyB to "b",
            R.id.keyN to "n", R.id.keyM to "m"
        )

        for ((id, char) in letterKeys) {
            view.findViewById<Button>(id).setOnClickListener {
                val c = if (isShiftOn) char.uppercase() else char
                handleCharInput(c)
                if (isShiftOn) {
                    isShiftOn = false
                    keyShift.text = "⇧"
                }
            }
        }

        setupClipboardListener()
        return view
    }

    // ── Telex Input Handling ────────────────────────────────────────

    private fun handleCharInput(char: String) {
        val ic = currentInputConnection ?: return

        // Space hoặc dấu câu → commit word + char
        if (char == " " || char == "," || char == "." || char == "\n") {
            currentWord.clear()
            ic.commitText(char, 1)
            return
        }

        currentWord.append(char)
        val buf = currentWord.toString()

        // Kiểm tra tổ hợp nguyên âm (modifier)
        for ((combo, replacement) in modifierMap) {
            if (buf.endsWith(combo)) {
                // Xóa số ký tự bằng với độ dài combo, rồi ghi replacement
                repeat(combo.length) { ic.deleteSurroundingText(1, 0) }
                ic.commitText(replacement, 1)
                currentWord.replace(currentWord.length - combo.length, currentWord.length, replacement)
                return
            }
        }

        // Kiểm tra dấu thanh (tone)
        for ((key, tone) in toneMap) {
            if (buf.endsWith(key) && buf.length > 1) {
                // Thử thêm dấu vào nguyên âm cuối trong từ
                val baseText = buf.dropLast(key.length)
                val toned = applyTone(baseText, tone)
                if (toned != null) {
                    // Xóa toàn bộ từ rồi commit lại với dấu
                    repeat(baseText.length + key.length - toned.length + toned.length) {
                        ic.deleteSurroundingText(1, 0)
                    }
                    // Xóa sạch và commit lại từ mới
                    repeat(buf.length) { ic.deleteSurroundingText(1, 0) }
                    ic.commitText(toned, 1)
                    currentWord.clear()
                    currentWord.append(toned)
                    return
                }
            }
        }

        // Gõ bình thường
        ic.commitText(char, 1)
    }

    private fun handleDelete() {
        val ic = currentInputConnection ?: return
        ic.deleteSurroundingText(1, 0)
        if (currentWord.isNotEmpty()) currentWord.deleteCharAt(currentWord.length - 1)
    }

    /**
     * Tìm nguyên âm cuối trong chuỗi và thêm dấu thanh (NFC).
     * Trả về null nếu không tìm được nguyên âm.
     */
    private fun applyTone(base: String, tone: String): String? {
        val vowels = "aăâeêioôơuưy"
        // Tìm nguyên âm từ cuối ngược lên
        for (i in base.indices.reversed()) {
            if (base[i].lowercaseChar() in vowels) {
                val withTone = Normalizer.normalize(base[i].toString() + tone, Normalizer.Form.NFC)
                return base.substring(0, i) + withTone + base.substring(i + 1)
            }
        }
        return null
    }

    // ── Bot UI ──────────────────────────────────────────────────────

    private fun updateUI(btnMode: Button, btnPause: Button) {
        if (isPaused) {
            tvStatus.text = "⏸️ Đang tạm dừng. Copy bất kỳ từ nào để tiếp tục."
            btnPause.text = "▶  Tiếp tục"
            tvModeBadge.text = "⏸ TẠM DỪNG"
            tvModeBadge.setTextColor(android.graphics.Color.parseColor("#FFB800"))
            tvModeBadge.setBackgroundResource(R.drawable.btn_muted)
            viewDot.setBackgroundColor(android.graphics.Color.parseColor("#FFB800"))
        } else {
            btnPause.text = "⏸  Dừng"
            when (currentMode) {
                Mode.TOP_TIER -> {
                    btnMode.text = "🎯  CỰC PHẨM"
                    tvStatus.text = "Chế độ cực phẩm. Chờ copy..."
                    tvModeBadge.text = "🎯 CỰC PHẨM"
                    tvModeBadge.setTextColor(android.graphics.Color.parseColor("#00FF88"))
                    tvModeBadge.setBackgroundResource(R.drawable.badge_top)
                    viewDot.setBackgroundColor(android.graphics.Color.parseColor("#00FF88"))
                }
                Mode.SMART_RANDOM -> {
                    btnMode.text = "🎲  THÔNG MINH"
                    tvStatus.text = "Chế độ thông minh. Chờ copy..."
                    tvModeBadge.text = "🎲 THÔNG MINH"
                    tvModeBadge.setTextColor(android.graphics.Color.parseColor("#00D4FF"))
                    tvModeBadge.setBackgroundResource(R.drawable.badge_smart)
                    viewDot.setBackgroundColor(android.graphics.Color.parseColor("#00D4FF"))
                }
                Mode.BOTTOM_TIER -> {
                    btnMode.text = "🔥  TOP CUỐI"
                    tvStatus.text = "Chế độ top cuối. Chờ copy..."
                    tvModeBadge.text = "🔥 TOP CUỐI"
                    tvModeBadge.setTextColor(android.graphics.Color.parseColor("#FF7A00"))
                    tvModeBadge.setBackgroundResource(R.drawable.badge_bottom)
                    viewDot.setBackgroundColor(android.graphics.Color.parseColor("#FF7A00"))
                }
            }
        }
    }

    // ── Clipboard Listener ──────────────────────────────────────────

    private fun setupClipboardListener() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.addPrimaryClipChangedListener {
            if (isPaused) return@addPrimaryClipChangedListener

            val clip = clipboard.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).text?.toString() ?: return@addPrimaryClipChangedListener
                val normText = normalize(text)
                if (normText.isEmpty()) return@addPrimaryClipChangedListener

                val parts = normText.split("\\s+".toRegex())
                if (parts.size > 2) {
                    tvStatus.text = "Văn bản dài quá, bỏ qua."
                    return@addPrimaryClipChangedListener
                }

                val suggestion = if (parts.size == 1) {
                    val ranked = suggestFromFirst(parts[0], 15)
                    pickOneWord(ranked)
                } else {
                    val candidates = suggestNext(normText, 15)
                    if (candidates.isEmpty()) {
                        tvStatus.text = "Không tìm thấy từ tiếp theo!"
                        null
                    } else {
                        pickTwoWord(candidates)?.phrase
                    }
                }

                if (suggestion != null && suggestion != normText) {
                    // Ở chế độ QWERTY (stealth): chỉ hiển thị gợi ý, không tự gõ
                    if (isStealthMode) {
                        tvStatus.text = "Gợi ý: $suggestion  (nhấn 🤖 để dùng Bot)"
                    } else {
                        tvStatus.text = "Đã dán: $suggestion"
                        currentInputConnection?.commitText(suggestion, 1)
                    }
                } else if (suggestion == null) {
                    tvStatus.text = "Không có gợi ý."
                }
            }
        }
    }

    // ── Dictionary & Logic ──────────────────────────────────────────

    private fun normalize(s: String): String {
        val nfc = Normalizer.normalize(s.trim(), Normalizer.Form.NFC)
        return nfc.lowercase().replace("\\s+".toRegex(), " ")
    }

    private fun loadDictionary() {
        try {
            val inputStream = assets.open("TuVung.txt")
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            var index = 0
            while (reader.readLine().also { line = it } != null) {
                val text = normalize(line!!)
                if (text.isEmpty()) continue
                val parts = text.split(" ")
                if (parts.size != 2) continue
                val first = parts[0]; val last = parts[1]
                phraseSet.add(text)
                byFirst.getOrPut(first) { mutableListOf() }.add(index)
                phrases.add(Phrase(text, last))
                index++
            }
            reader.close()
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun suggestNext(phrase: String, topN: Int): List<RankedPhrase> {
        val parts = phrase.split(" ")
        if (parts.size != 2) return emptyList()
        val indices = byFirst[parts[1]] ?: return emptyList()
        val ranked = indices.take(200).map { i ->
            val p = phrases[i]
            RankedPhrase(p.text, byFirst[p.last]?.size ?: 0)
        }
        return when (currentMode) {
            Mode.BOTTOM_TIER -> ranked.sortedByDescending { it.nextCount }.take(topN)
            else             -> ranked.sortedBy { it.nextCount }.take(topN)
        }
    }

    private fun suggestFromFirst(firstWord: String, topN: Int): List<RankedPhrase> {
        val indices = byFirst[firstWord] ?: return emptyList()
        val ranked = indices.take(200).map { i ->
            val p = phrases[i]
            RankedPhrase(p.text, byFirst[p.last]?.size ?: 0)
        }
        return when (currentMode) {
            Mode.BOTTOM_TIER -> ranked.sortedByDescending { it.nextCount }.take(topN)
            else             -> ranked.sortedBy { it.nextCount }.take(topN)
        }
    }

    private fun pickTwoWord(candidates: List<RankedPhrase>): RankedPhrase? {
        if (candidates.isEmpty()) return null
        return when (currentMode) {
            Mode.TOP_TIER -> {
                val best = candidates[0].nextCount
                candidates.filter { it.nextCount == best }.random()
            }
            Mode.SMART_RANDOM -> {
                if (Random.nextDouble() < 0.25) candidates.random()
                else {
                    val best = candidates[0].nextCount
                    candidates.filter { it.nextCount == best }.random()
                }
            }
            Mode.BOTTOM_TIER -> {
                val rich = candidates.filter { it.nextCount > 4 }
                if (rich.isNotEmpty()) rich.random() else candidates.random()
            }
        }
    }

    private fun pickOneWord(ranked: List<RankedPhrase>): String? {
        if (ranked.isEmpty()) return null
        return when (currentMode) {
            Mode.TOP_TIER -> {
                val best = ranked[0].nextCount
                ranked.filter { it.nextCount == best }.random().phrase
            }
            Mode.SMART_RANDOM -> {
                if (Random.nextDouble() < 0.25) ranked.random().phrase
                else {
                    val best = ranked[0].nextCount
                    ranked.filter { it.nextCount == best }.random().phrase
                }
            }
            Mode.BOTTOM_TIER -> {
                val rich = ranked.filter { it.nextCount > 4 }
                if (rich.isNotEmpty()) rich.random().phrase else ranked.random().phrase
            }
        }
    }
}
