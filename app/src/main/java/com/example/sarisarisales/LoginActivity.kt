package com.example.sarisarisales

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sarisarisales.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    // Request code for Google Sign-In
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Configure Google Sign-In options
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // --- 1. Email/Password Login ---
        binding.btnSignIn.setOnClickListener {
            performEmailPasswordLogin()
        }

        // --- 2. Google Sign-In ---
        binding.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        // --- 3. Forgot Password ---
        binding.tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        // --- 4. New User (FIXED: Now navigates to SignupActivity) ---
        binding.tvNewUser.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }

    // ⭐ NEW: Checks if the user is already logged in and skips login screen
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            navigateToDashboard()
        }
    }

    // --- Function to handle standard Email/Password Login ---
    private fun performEmailPasswordLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        // Firebase Sign-In
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    navigateToDashboard() // ⭐ FIXED: Navigates to Dashboard
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    // --- Functions to handle Google Sign-In Flow ---
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: com.google.android.gms.common.api.ApiException) {
                // Google Sign In failed
                Toast.makeText(this, "Google Sign In failed: ${e.statusCode}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    Toast.makeText(this, "Signed in as ${auth.currentUser?.displayName}", Toast.LENGTH_SHORT).show()
                    navigateToDashboard() // ⭐ FIXED: Navigates to Dashboard
                } else {
                    // If sign in fails
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // ⭐ NEW/FIXED: Central function for navigation after successful login/signup
    private fun navigateToDashboard() {
        val intent = Intent(this, MainDashboardActivity::class.java) // Target is MainDashboardActivity
        // Clear activity stack
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}