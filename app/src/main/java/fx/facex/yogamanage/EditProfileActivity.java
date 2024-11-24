package fx.facex.yogamanage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editFullName, editEmail, editPhone, editLocation;
    private ImageView editProfileImage;
    private Button saveProfileButton, changeImageButton;
    private String imageBase64;
    private FirebaseAuth auth;
    private DatabaseReference userReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize UI components
        editFullName = findViewById(R.id.editFullName);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        editLocation = findViewById(R.id.editLocation);
        editProfileImage = findViewById(R.id.editProfileImage);
        saveProfileButton = findViewById(R.id.saveProfileButton);
        changeImageButton = findViewById(R.id.changeImageButton);

        auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        userReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // Fetch current user details
        fetchUserDetails();

        // Handle changing the profile image
        changeImageButton.setOnClickListener(view -> {
            Intent photoPicker = new Intent(Intent.ACTION_PICK);
            photoPicker.setType("image/*");
            startActivityForResult(photoPicker, 1);
        });

        // Save updated details
        saveProfileButton.setOnClickListener(view -> saveUpdatedDetails());
    }

    private void fetchUserDetails() {
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    editFullName.setText(snapshot.child("fullName").getValue(String.class));
                    editEmail.setText(snapshot.child("email").getValue(String.class));
                    editPhone.setText(snapshot.child("phone").getValue(String.class));
                    editLocation.setText(snapshot.child("location").getValue(String.class));
                    String profileImageBase64 = snapshot.child("profileImage").getValue(String.class);
                    setProfileImage(profileImageBase64);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(EditProfileActivity.this, "Failed to load user details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUpdatedDetails() {
        String fullName = editFullName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String location = editLocation.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update user details in Firebase
        userReference.child("fullName").setValue(fullName);
        userReference.child("email").setValue(email);
        userReference.child("phone").setValue(phone);
        userReference.child("location").setValue(location);
        if (imageBase64 != null) {
            userReference.child("profileImage").setValue(imageBase64);
        }

        Toast.makeText(this, "Profile updated successfully.", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void setProfileImage(String profileImageBase64) {
        if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
            byte[] decodedBytes = Base64.decode(profileImageBase64, Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            editProfileImage.setImageBitmap(decodedBitmap);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            editProfileImage.setImageURI(imageUri);
            encodeImageToBase64(imageUri);
        }
    }

    private void encodeImageToBase64(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (IOException e) {
            Toast.makeText(this, "Failed to encode image.", Toast.LENGTH_SHORT).show();
        }
    }
}
