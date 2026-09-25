package com.example.studybuddy.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studybuddy.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();
        binding.loginButton.setOnClickListener(v -> loginUser());

        binding.registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = binding.emailEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        String userId = firebaseAuth.getCurrentUser().getUid();

                        db.collection("users")
                                .document(userId)
                                .get()
                                .addOnSuccessListener(document -> {
                                    if (document.exists()) {

                                        String firestoreEmail =
                                                document.getString("email");

                                        Toast.makeText(this,
                                                "Firestore read successful: "
                                                        + firestoreEmail,
                                                Toast.LENGTH_LONG).show();

                                    } else {
                                        Toast.makeText(this,
                                                "User document not found",
                                                Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this,
                                            "Firestore read failed: "
                                                    + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                });

                    } else {
                        Toast.makeText(this,
                                "Login failed: "
                                        + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}