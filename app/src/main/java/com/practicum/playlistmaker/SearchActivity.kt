package com.practicum.playlistmaker

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class SearchActivity : AppCompatActivity() {

    private lateinit var inputEditText: EditText
    private var clearDrawable: Drawable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val buttonBack = findViewById<TextView>(R.id.searchHeader)
        inputEditText = findViewById<EditText>(R.id.inputEditText)
        clearDrawable =
            ContextCompat.getDrawable(this, android.R.drawable.ic_menu_close_clear_cancel)
        clearDrawable?.setBounds(
            0,
            0,
            clearDrawable!!.intrinsicWidth,
            clearDrawable!!.intrinsicHeight
        )

        buttonBack.setOnClickListener {
            finish()
        }

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // empty
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateClearIconVisibility(s)
            }

            override fun afterTextChanged(s: Editable?) {
                // empty
            }
        }
        inputEditText.addTextChangedListener(simpleTextWatcher)

        inputEditText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = inputEditText.compoundDrawables[2]
                if (drawableEnd != null) {
                    val drawableStartX =
                        inputEditText.width - inputEditText.paddingEnd - drawableEnd.bounds.width()
                    if (event.x >= drawableStartX) {
                        inputEditText.text?.clear()
                        hideKeyboard()
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }

    private fun updateClearIconVisibility(s: CharSequence?) {
        val drawables = if (!s.isNullOrEmpty()) {
            arrayOf(
                inputEditText.compoundDrawables[0],
                null,
                clearDrawable,
                null
            )
        } else {
            arrayOf(
                inputEditText.compoundDrawables[0],
                null,
                null,
                null
            )
        }
        inputEditText.setCompoundDrawables(
            drawables[0],
            drawables[1],
            drawables[2],
            drawables[3]
        )
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }
}
