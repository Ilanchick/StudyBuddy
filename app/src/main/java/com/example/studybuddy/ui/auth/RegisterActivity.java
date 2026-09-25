package com.example.studybuddy.ui.auth;


import java.util.HashMap;
import java.util.Map;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studybuddy.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        binding.registerButton.setOnClickListener(v -> registerUser());

        binding.backToLoginButton.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String email = binding.emailEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString();
        String confirmPassword =
                binding.confirmPasswordEditText.getText().toString();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = firebaseAuth.getCurrentUser().getUid();

                        Map<String, Object> user = new HashMap<>();
                        user.put("email", email);

                        db.collection("users")
                                .document(userId)
                                .set(user)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Account created!",
                                            Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this,
                                            "Account created, but Firestore failed: "
                                                    + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                });
                    } else {
                        Toast.makeText(this,
                                "Registration failed: "
                                        + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}