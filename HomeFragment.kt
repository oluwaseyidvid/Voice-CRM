package com.neuralic.voicecrm.ui

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.neuralic.voicecrm.R
import com.neuralic.voicecrm.data.AppDatabase
import com.neuralic.voicecrm.data.ActionLog
import com.neuralic.voicecrm.network.ApiPlaceholders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private lateinit var micButton: ImageButton
    private lateinit var inputField: EditText
    private lateinit var draftPreview: View
    private lateinit var draftTitle: TextView
    private lateinit var draftBody: TextView
    private lateinit var execBtn: Button
    private lateinit var editBtn: Button
    private lateinit var cancelBtn: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_home, container, false)
        micButton = v.findViewById(R.id.btn_mic)
        inputField = v.findViewById(R.id.input_text)
        draftPreview = v.findViewById(R.id.draft_preview)
        draftTitle = v.findViewById(R.id.draft_title)
        draftBody = v.findViewById(R.id.draft_body)
        execBtn = v.findViewById(R.id.btn_execute)
        editBtn = v.findViewById(R.id.btn_edit)
        cancelBtn = v.findViewById(R.id.btn_cancel)

        // initialize TTS
        tts = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
            }
        }

        micButton.setOnClickListener { startListening() }

        execBtn.setOnClickListener {
            // execute action: for demo, create an ActionLog entry in Room DB and call placeholder API
            val summary = draftBody.text.toString()
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val entry = ActionLog(type = "ManualExecute", summary = summary, timestamp = timestamp)

            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getInstance(requireContext())
                db.actionLogDao().insert(entry)
                // call pipeline: analyze with OpenAI then try create pipedrive activity (placeholders)
                val ai = ApiPlaceholders.analyzeTextWithOpenAI(summary)
                ApiPlaceholders.createPipedriveActivity(ai)
            }
            draftPreview.visibility = View.GONE
        }

        editBtn.setOnClickListener {
            inputField.setText(draftBody.text.toString())
            draftPreview.visibility = View.GONE
        }

        cancelBtn.setOnClickListener { draftPreview.visibility = View.GONE }

        return v
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(requireContext())) return
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {}
                override fun onResults(results: Bundle) {
                    val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val spoken = matches[0]
                        handleSpoken(spoken)
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, requireContext().packageName)
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        speechRecognizer?.startListening(intent)
    }

    private fun handleSpoken(spoken: String) {
        // basic filler filtering
        val filtered = spoken.replace(Regex("\b(um|uh|you know|like)\b", RegexOption.IGNORE_CASE), "").trim()

        // ask AI (placeholder) to interpret intent and produce a draft summary
        CoroutineScope(Dispatchers.IO).launch {
            val draft = ApiPlaceholders.analyzeTextWithOpenAI(filtered)
            CoroutineScope(Dispatchers.Main).launch {
                draftTitle.text = "Draft action"
                draftBody.text = draft
                draftPreview.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tts?.shutdown()
        speechRecognizer?.destroy()
    }
}
