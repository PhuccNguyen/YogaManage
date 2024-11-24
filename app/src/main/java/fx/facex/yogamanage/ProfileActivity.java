package fx.facex.yogamanage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView fullName, email, phone, location;
    private Button logoutButton, editProfileButton, manageYogaButton;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference userReference;

    private LinearLayout homeButton, profileButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize UI Components
        profileImage = findViewById(R.id.profileImage);
        fullName = findViewById(R.id.fullName);
        email = findViewById(R.id.email);
        phone = findViewById(R.id.phone);
        location = findViewById(R.id.location);
        logoutButton = findViewById(R.id.logoutButton);
        editProfileButton = findViewById(R.id.editProfileButton);
        manageYogaButton = findViewById(R.id.manageYogaButton);
        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Initialize buttons
        homeButton = findViewById(R.id.homeButton);
        profileButton = findViewById(R.id.profileButton);


        // Logout Button Click Listener
        logoutButton.setOnClickListener(view -> showLogoutConfirmationDialog());

        // Edit Profile Button Logic
        editProfileButton.setOnClickListener(view -> {
            Toast.makeText(this, "Edit Profile clicked.", Toast.LENGTH_SHORT).show();
            // Navigate to Edit Profile Screen (implement if required)
        });

        Button manageYogaButton = findViewById(R.id.manageYogaButton);

        manageYogaButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ManageYogaActivity.class);
            startActivity(intent);
        });


        // Load user details (dummy example; connect with actual data source)
        loadUserDetails();


        if (user != null) {
            String userId = user.getUid();
            userReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

            // Fetch User Details
            fetchUserDetails();
        } else {
            Toast.makeText(this, "User not logged in. Redirecting to Login.", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        }

        // Handle Edit Profile Button click
//        editProfileButton.setOnClickListener(view -> navigateToEditProfile());



        editProfileButton.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });



        // Home Button Logic
        homeButton.setOnClickListener(view -> {
            // Reload the MainActivity
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }


    private void loadUserDetails() {
        // Load user details (for example purposes, replace with real data fetching logic)
        fullName.setText("John Doe");
        email.setText("john.doe@example.com");
        phone.setText("Phone: +123456789");
        location.setText("Location: City, Country");
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    auth.signOut();
                    navigateToLogin();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(ProfileActivity.this, Login.class);
        startActivity(intent);
        finish();
    }

    /**
     * Show a message (for testing purposes)
     */
    private void showMessage(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }
    /**
     * Fetch user details from Firebase and update UI
     */
    private void fetchUserDetails() {
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("fullName").getValue(String.class);
                    String emailAddress = snapshot.child("email").getValue(String.class);
                    String phoneNumber = snapshot.child("phone").getValue(String.class);
                    String userLocation = snapshot.child("location").getValue(String.class);
                    String profileImageBase64 = snapshot.child("profileImage").getValue(String.class);

                    // Update UI with user details
                    fullName.setText(name != null ? name : "N/A");
                    email.setText(emailAddress != null ? emailAddress : "N/A");
                    phone.setText("Phone: " + (phoneNumber != null ? phoneNumber : "N/A"));
                    location.setText("Location: " + (userLocation != null ? userLocation : "N/A"));

                    // Decode and set profile image
                    setProfileImage(profileImageBase64);
                } else {
                    Toast.makeText(ProfileActivity.this, "No user details found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to fetch user details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Set profile image from Base64 string
     */
    private void setProfileImage(String profileImageBase64) {
        if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(profileImageBase64, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                profileImage.setImageBitmap(decodedBitmap);
            } catch (IllegalArgumentException e) {
                profileImage.setImageResource(R.drawable.user);
                Toast.makeText(this, "Invalid profile image format.", Toast.LENGTH_SHORT).show();
            }
        } else {
            profileImage.setImageResource(R.drawable.user);
        }
    }

    /**
     * Navigate to Edit Profile Activity
     */
//    private void navigateToEditProfile() {
//        Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
//        startActivity(intent);
//    }

    /**
     * Redirect to Login Activity if user is not logged in
     */
    private void redirectToLogin() {
        Intent intent = new Intent(ProfileActivity.this, Login.class);
        startActivity(intent);
        finish();
    }
}
