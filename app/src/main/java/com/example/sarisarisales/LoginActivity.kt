package com.example.sarisarisales

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val usernameInput = findViewById<EditText>(R.id.et_email)
        val passwordInput = findViewById<EditText>(R.id.et_password)
        val loginButton = findViewById<Button>(R.id.btn_sign_in)
        val forgotPassword = findViewById<TextView>(R.id.tv_forgot_password)

        // Hardcoded credentials (offline)
        val validUsername = "admin"
        val validPassword = "1234"

        loginButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            } else if (username == validUsername && password == validPassword) {
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("username", username)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Invalid username or password.", Toast.LENGTH_SHORT).show()
            }
        }

        forgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }
}