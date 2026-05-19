package com.deltarfd.deltagamesandroid.presentation.profile

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import com.deltarfd.deltagamesandroid.R
import com.deltarfd.deltagamesandroid.databinding.FragmentProfileBinding
import androidx.core.content.edit

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val PREF_NAME   = "delta_games_profile"
        private const val KEY_CAPTION = "caption"
        private const val DEFAULT_CAPTION = "Hello World"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedCaption = prefs.getString(KEY_CAPTION, DEFAULT_CAPTION) ?: DEFAULT_CAPTION

        // Read current locale from AppCompatDelegate — source of truth after recreation
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        val currentLang = when {
            currentLocales.isEmpty                               -> "system"
            currentLocales[0]?.language?.startsWith("id") == true -> "indonesian"
            currentLocales[0]?.language?.startsWith("en") == true -> "english"
            else                                                 -> "system"
        }

        with(binding) {
            tvName.text           = "Delta R F D"
            tvEmail.text          = getString(R.string.profile_joined)
            tvBio.text            = "\"$savedCaption\""
            tvVersion.text        = getString(R.string.version_format)
            tvApiAttribution.text = getString(R.string.api_attribution)

            applyChipVisuals(currentLang)

            chipSystem.setOnClickListener    { switchLocale("system") }
            chipEnglish.setOnClickListener   { switchLocale("english") }
            chipIndonesian.setOnClickListener { switchLocale("indonesian") }

            btnEdit.setOnClickListener {
                val caption = prefs.getString(KEY_CAPTION, DEFAULT_CAPTION) ?: DEFAULT_CAPTION
                showEditCaptionDialog(caption) { newCaption ->
                    tvBio.text = "\"$newCaption\""
                    prefs.edit { putString(KEY_CAPTION, newCaption) }
                }
            }
        }
    }

    private fun switchLocale(lang: String) {
        val localeList = when (lang) {
            "english"    -> LocaleListCompat.forLanguageTags("en")
            "indonesian" -> LocaleListCompat.forLanguageTags("id")
            else         -> LocaleListCompat.getEmptyLocaleList()
        }
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    private fun showEditCaptionDialog(current: String, onSave: (String) -> Unit) {
        val editText = EditText(requireContext()).apply {
            id = R.id.edit_caption_edittext
            setText(current)
            setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.white))
            setHintTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.colorTextHint))
            hint = getString(R.string.edit_caption_hint)
            setSelection(text.length)
        }
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (20 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad / 2, pad, 0)
            addView(editText)
        }
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.edit_caption_title))
            .setView(container)
            .setPositiveButton(getString(R.string.action_save)) { dialog, _ ->
                val dialogEditText = (dialog as? AlertDialog)?.findViewById(R.id.edit_caption_edittext) ?: editText
                val newCaption = dialogEditText.text.trim().toString()
                if (newCaption.isNotEmpty()) onSave(newCaption)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.action_cancel)) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun applyChipVisuals(lang: String) {
        with(binding) {
            fun chip(view: android.widget.TextView, selected: Boolean) {
                view.setBackgroundResource(if (selected) R.drawable.bg_chip_selected else R.drawable.bg_chip_default)
                view.setTextColor(
                    androidx.core.content.ContextCompat.getColor(
                        requireContext(),
                        if (selected) R.color.black else R.color.colorTextSecondary
                    )
                )
            }
            chip(chipSystem,      lang == "system")
            chip(chipEnglish,     lang == "english")
            chip(chipIndonesian,  lang == "indonesian")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
