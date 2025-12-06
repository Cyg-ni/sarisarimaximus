package com.example.sarisarisales

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sarisarisales.databinding.ActivityResetPasswordBinding // IMPORTANT: Matches activity_reset_password.xml
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var auth: FirebaseAuth
    // This will hold the unique code from the Firebase email link
    private var actionCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // 1. Get the action code (oobCode) from the Intent that launched this activity
        // Firebase passes this code in the URL parameters when the user clicks the reset link.
        actionCode = intent.getStringExtra("oobCode")

        if (actionCode.isNullOrEmpty()) {
            Toast.makeText(this, "Error: Invalid or missing password reset link.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // 2. Set up Click Listener for the Update Password Button
        // This corresponds to btn_update_password in your activity_reset_password.xml
        binding.btnUpdatePassword.setOnClickListener {
            resetPassword()
        }
    }

    /**
     * Validates input and executes the Firebase password reset using the action code.
     */
    private fun resetPassword() {
        val newPassword = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        // --- Validation ---
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in both password fields.", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmPassword) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. Firebase Logic: Use the action code to update the password
        if (actionCode != null) {
            auth.confirmPasswordReset(actionCode!!, newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Password reset successful! You can now log in with your new password.", Toast.LENGTH_LONG).show()

                        // Navigate back to LoginActivity and clear the activity stack
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        finish()
                    } else {
                        // Error may be due to expired code, incorrect code, or network issues
                        Toast.makeText(this, "Password reset failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}