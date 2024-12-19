package com.aripov.arduinobluetooth_controller

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Controller : AppCompatActivity() {
  private val outputStream = Socket.socket?.outputStream
  private lateinit var editText : EditText
  private lateinit var btnSend : Button

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContentView(R.layout.activity_controller)
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
      val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
      insets
    }

    editText = findViewById(R.id.etOutput)
    btnSend = findViewById(R.id.btnSend)

    btnSend.setOnClickListener {
      if(outputStream == null) {
        Toast.makeText(application, "No socket", Toast.LENGTH_SHORT).show()
      }
      val value = editText.text.toString()
      outputStream?.write(value.toByteArray())
      Toast.makeText(application, "${value}\n${value.toByteArray()}", Toast.LENGTH_SHORT).show()
      editText.text.clear()
    }
  }
}