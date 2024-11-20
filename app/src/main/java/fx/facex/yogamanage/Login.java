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

public class Login extends AppCompatActivity {

    private TextInputEditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private TextView textViewRegister;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and navigate to MainActivity if logged in.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToMain();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progressBar);
        textViewRegister = findViewById(R.id.registerNow);

        // Navigate to Register screen
        textViewRegister.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), Register.class);
            startActivity(intent);
            finish();
        });

        // Handle Login button click
        buttonLogin.setOnClickListener(view -> handleLogin());
    }

    /**
     * Handles user login.
     */
    private void handleLogin() {
        progressBar.setVisibility(View.VISIBLE);

        // Get input values
        String email = String.valueOf(editTextEmail.getText()).trim();
        String password = String.valueOf(editTextPassword.getText()).trim();

        // Validate inputs
        if (!validateInputs(email, password)) {
            progressBar.setVisibility(View.GONE);
            return;
        }

        // Attempt to log in the user
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // Login successful
                            Toast.makeText(getApplicationContext(), "Login Successful!", Toast.LENGTH_SHORT).show();
                            navigateToMain();
                        } else {
                            // Login failed
                            handleLoginError(task);
                        }
                    }
                });
    }

    /**
     * Validates user input for email and password.
     *
     * @param email    The email input.
     * @param password The password input.
     * @return True if inputs are valid, false otherwise.
     */
    private boolean validateInputs(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            showToast("Please enter an email address.");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Please enter a valid email address.");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            showToast("Please enter a password.");
            return false;
        }
        if (password.length() < 6) {
            showToast("Password must be at least 6 characters.");
            return false;
        }
        return true;
    }

    /**
     * Handles Firebase login errors.
     *
     * @param task The Task containing the login result.
     */
    private void handleLoginError(@NonNull Task<AuthResult> task) {
        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Login failed.";
        showToast(errorMessage);
    }

    /**
     * Navigates to the MainActivity.
     */
    private void navigateToMain() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Displays a toast message.
     *
     * @param message The message to display.
     */
    private void showToast(String message) {
        Toast.makeText(Login.this, message, Toast.LENGTH_SHORT).show();
    }
}
