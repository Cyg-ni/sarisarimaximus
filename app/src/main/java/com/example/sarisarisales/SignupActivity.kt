package com.example.sarisarisales

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sarisarisales.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize View Binding
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // 3. Set up Click Listeners

        // Sign Up Button Listener: Trigger the registration process
        binding.btnSignUp.setOnClickListener {
            registerNewUser()
        }

        // Already Have Account Link: Navigate back to LoginActivity
        binding.tvHaveAccount.setOnClickListener {
            // Simply closing this activity returns to the LoginActivity below it
            finish()
        }
    }

    /**
     * Handles user input validation and Firebase email/password registration.
     */
    private fun registerNewUser() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        // --- Basic Validation ---
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show()
            return
        }

        // --- Firebase Registration ---
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign up success, now update user profile (display name)
                    val user = auth.currentUser
                    if (user != null) {
                        // Request to set the display name
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()

                        user.updateProfile(profileUpdates)
                            .addOnCompleteListener { profileTask ->
                                if (profileTask.isSuccessful) {
                                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_LONG).show()
                                    // Navigate to the Dashboard after successful registration
                                    navigateToDashboard()
                                } else {
                                    // Registration was successful, but name update failed
                                    Toast.makeText(this, "Registration successful, but failed to set display name.", Toast.LENGTH_SHORT).show()
                                    navigateToDashboard()
                                }
                            }
                    }
                } else {
                    // If sign up fails (e.g., email already in use, invalid email format)
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    /**
     * Navigates to the main dashboard activity and clears the back stack.
     */
    private fun navigateToDashboard() {
        val intent = Intent(this, MainDashboardActivity::class.java)
        // FLAG_ACTIVITY_CLEAR_TASK and FLAG_ACTIVITY_NEW_TASK ensure the user can't press back
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}