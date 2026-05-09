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
    private var isPaused = false
    private var currentMode = Mode.SMART_RANDOM

    private val phrases = mutableListOf<Phrase>()
    private val phraseSet = mutableSetOf<String>()
    private val byFirst = mutableMapOf<String, MutableList<Int>>()

    enum class Mode { TOP_TIER, SMART_RANDOM }

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
        val btnMode = view.findViewById<Button>(R.id.btnMode)
        val btnPause = view.findViewById<Button>(R.id.btnPause)
        val btnSwitch = view.findViewById<Button>(R.id.btnSwitch)

        updateUI(btnMode, btnPause)

        btnMode.setOnClickListener {
            currentMode = if (currentMode == Mode.TOP_TIER) Mode.SMART_RANDOM else Mode.TOP_TIER
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

        setupClipboardListener()

        return view
    }

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
            if (currentMode == Mode.TOP_TIER) {
                btnMode.text = "🎯  CỰC PHẨM"
                tvStatus.text = "Chế độ cực phẩm. Chờ copy..."
                tvModeBadge.text = "🎯 CỰC PHẨM"
                tvModeBadge.setTextColor(android.graphics.Color.parseColor("#00FF88"))
                tvModeBadge.setBackgroundResource(R.drawable.badge_top)
                viewDot.setBackgroundColor(android.graphics.Color.parseColor("#00FF88"))
            } else {
                btnMode.text = "🎲  THÔNG MINH"
                tvStatus.text = "Chế độ thông minh. Chờ copy..."
                tvModeBadge.text = "🎲 THÔNG MINH"
                tvModeBadge.setTextColor(android.graphics.Color.parseColor("#00D4FF"))
                tvModeBadge.setBackgroundResource(R.drawable.badge_smart)
                viewDot.setBackgroundColor(android.graphics.Color.parseColor("#00D4FF"))
            }
        }
    }

    private fun setupClipboardListener() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.addPrimaryClipChangedListener {
            if (isPaused) return@addPrimaryClipChangedListener

            val clip = clipboard.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).text?.toString() ?: return@addPrimaryClipChangedListener
                processCopiedText(text)
            }
        }
    }

    private fun processCopiedText(rawText: String) {
        val normText = normalize(rawText)
        if (normText.isEmpty()) return

        val parts = normText.split("\\s+".toRegex())
        if (parts.size > 2) {
            tvStatus.text = "Văn bản dài quá, bỏ qua."
            return
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
                pickTwoWord(candidates)?.text
            }
        }

        if (suggestion != null && suggestion != normText) {
            tvStatus.text = "Đã dán: $suggestion"
            currentInputConnection?.commitText(suggestion, 1)
        } else if (suggestion == null) {
            tvStatus.text = "Không có gợi ý."
        }
    }

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

                val first = parts[0]
                val last = parts[1]

                phraseSet.add(text)
                if (!byFirst.containsKey(first)) {
                    byFirst[first] = mutableListOf()
                }
                byFirst[first]?.add(index)
                phrases.add(Phrase(text, last))
                index++
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun suggestNext(phrase: String, topN: Int): List<Phrase> {
        val parts = phrase.split(" ")
        if (parts.size != 2) return emptyList()
        val lastWord = parts[1]
        val indices = byFirst[lastWord] ?: return emptyList()
        return indices.take(topN).map { phrases[it] }
    }

    private fun suggestFromFirst(firstWord: String, topN: Int): List<RankedPhrase> {
        val indices = byFirst[firstWord] ?: return emptyList()
        val ranked = indices.take(200).map { i ->
            val p = phrases[i]
            val nextCount = byFirst[p.last]?.size ?: 0
            RankedPhrase(p.text, nextCount)
        }.sortedBy { it.nextCount }.take(topN)
        return ranked
    }

    private fun pickTwoWord(candidates: List<Phrase>): Phrase? {
        if (candidates.isEmpty()) return null
        return when (currentMode) {
            Mode.TOP_TIER -> candidates.random()
            Mode.SMART_RANDOM -> if (Random.nextDouble() < 0.25) candidates.random() else candidates.random() // Same as Rust logic for randomizing top pool
        }
    }

    private fun pickOneWord(ranked: List<RankedPhrase>): String? {
        if (ranked.isEmpty()) return null
        return when (currentMode) {
            Mode.TOP_TIER -> {
                val bestCount = ranked[0].nextCount
                val top = ranked.filter { it.nextCount == bestCount }
                top.random().phrase
            }
            Mode.SMART_RANDOM -> {
                if (Random.nextDouble() < 0.25) {
                    ranked.random().phrase
                } else {
                    val bestCount = ranked[0].nextCount
                    val top = ranked.filter { it.nextCount == bestCount }
                    top.random().phrase
                }
            }
        }
    }
}
