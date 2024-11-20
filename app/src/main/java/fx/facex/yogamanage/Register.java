package fx.facex.yogamanage;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    private TextInputEditText editTextEmail, editTextPassword, editTextFullName, editTextPhone, editTextLocation;
    private Button buttonReg;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private TextView textViewLogin;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToMain();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextFullName = findViewById(R.id.full_name);
        editTextPhone = findViewById(R.id.phone);
        editTextLocation = findViewById(R.id.location);
        buttonReg = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);
        textViewLogin = findViewById(R.id.loginNow);

        textViewLogin.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        buttonReg.setOnClickListener(view -> handleRegistration());
    }

    private void handleRegistration() {
        progressBar.setVisibility(View.VISIBLE);

        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String fullName = editTextFullName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();

        if (!validateInputs(email, password, fullName, phone, location)) {
            progressBar.setVisibility(View.GONE);
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserDetails(user.getUid(), fullName, phone, location, email);
                        }
                    } else {
                        handleRegistrationError(task);
                    }
                });
    }

    private boolean validateInputs(String email, String password, String fullName, String phone, String location) {
        if (TextUtils.isEmpty(fullName)) {
            showToast("Please enter your full name.");
            return false;
        }
        if (TextUtils.isEmpty(phone)) {
            showToast("Please enter your phone number.");
            return false;
        }
        if (TextUtils.isEmpty(location)) {
            showToast("Please enter your location.");
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            showToast("Please enter an email.");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Enter a valid email.");
            return false;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            showToast("Password must be at least 6 characters.");
            return false;
        }
        return true;
    }
    private void saveUserDetails(String userId, String fullName, String phone, String location, String email) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Create a User object
        User user = new User(fullName, email, phone, location, userId);

        // Save the user object to Firebase
        databaseReference.child(userId).setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Register.this, "User details saved successfully.", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        // Handle the failure case
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error occurred.";
                        Toast.makeText(Register.this, "Failed to save user details: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }




    private void handleRegistrationError(Task<AuthResult> task) {
        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Registration failed.";
        showToast(errorMsg);
    }

    private void navigateToMain() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
