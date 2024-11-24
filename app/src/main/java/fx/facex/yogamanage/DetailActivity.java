package fx.facex.yogamanage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class DetailActivity extends AppCompatActivity {

    private ImageView detailImage;
    private TextView detailType, detailDayAndTime, detailCapacity, detailDuration, detailTeacher, detailPrice, detailDescription;
    private Button detailJoinButton, detailCommentButton;

    private FirebaseAuth auth;
    private DatabaseReference coursesReference;
    private String courseId; // Store the current course ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        coursesReference = FirebaseDatabase.getInstance().getReference("YogaCourses");

        // Initialize views
        initializeViews();

        // Retrieve and display course data
        retrieveCourseDetails();

        // Set button click listeners
        handleJoinButton();
        handleCommentButton();
    }

    private void initializeViews() {
        detailImage = findViewById(R.id.detailImage);
        detailType = findViewById(R.id.detailType);
        detailDayAndTime = findViewById(R.id.detailDayAndTime);
        detailCapacity = findViewById(R.id.detailCapacity);
        detailDuration = findViewById(R.id.detailDuration);
        detailTeacher = findViewById(R.id.detailTeacher);
        detailPrice = findViewById(R.id.detailPrice);
        detailDescription = findViewById(R.id.detailDescription);
        detailJoinButton = findViewById(R.id.detailJoinButton);
        detailCommentButton = findViewById(R.id.detailCommentButton);
    }

    private void retrieveCourseDetails() {
        Intent intent = getIntent();
        if (intent != null) {
            // Retrieve course ID and other details
            courseId = intent.getStringExtra("courseId");
            if (courseId == null || courseId.isEmpty()) {
                showToast("Error: Course ID is missing.");
                finish(); // Exit the activity if course ID is missing
                return;
            }

            String dataImage = intent.getStringExtra("dataImage");
            if (dataImage != null && !dataImage.isEmpty()) {
                byte[] decodedString = Base64.decode(dataImage, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                detailImage.setImageBitmap(decodedBitmap);
            } else {
                detailImage.setImageResource(R.drawable.yogaimages); // Placeholder image
            }

            // Set course details in the UI
            detailType.setText(intent.getStringExtra("type"));
            detailDayAndTime.setText("Day: " + intent.getStringExtra("dayOfWeek") + ", Time: " + intent.getStringExtra("timeOfCourse"));
            detailCapacity.setText("Capacity: " + intent.getIntExtra("capacity", 0));
            detailDuration.setText("Duration: " + intent.getIntExtra("duration", 0) + " mins");
            detailTeacher.setText("Teacher: " + intent.getStringExtra("teacher"));
            detailPrice.setText("Price: $" + intent.getDoubleExtra("price", 0.0));
            detailDescription.setText(intent.getStringExtra("description"));
        } else {
            showToast("Error: No data found for this course.");
            finish(); // Exit the activity if intent is null
        }
    }

    private void handleJoinButton() {
        detailJoinButton.setOnClickListener(view -> {
            if (courseId == null || courseId.isEmpty()) {
                showToast("Error: Cannot join the class without a valid course ID.");
                return;
            }
            showPaymentDialog();
        });
    }

    private void handleCommentButton() {
        detailCommentButton.setOnClickListener(view -> showToast("View Comments clicked."));
    }

    private void showPaymentDialog() {
        // Inflate the dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_payment, null);

        // Initialize dialog views
        TextView userName = dialogView.findViewById(R.id.userName);
        TextView userPhone = dialogView.findViewById(R.id.userPhone);
        TextView userAddress = dialogView.findViewById(R.id.userAddress);
        TextView coursePrice = dialogView.findViewById(R.id.coursePrice);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button confirmButton = dialogView.findViewById(R.id.confirmButton);

        // Populate user info from Firebase
        populateUserInfo(userName, userPhone, userAddress);

        // Populate course price
        double price = getIntent().getDoubleExtra("price", 0.0);
        coursePrice.setText("Price: $" + price);

        // Build and show the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Handle cancel button click
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // Handle confirm button click
        confirmButton.setOnClickListener(v -> {
            dialog.dismiss();
            confirmJoinClass();
        });
    }

    private void populateUserInfo(TextView userName, TextView userPhone, TextView userAddress) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        userName.setText("Name: " + snapshot.child("fullName").getValue(String.class));
                        userPhone.setText("Phone: " + snapshot.child("phone").getValue(String.class));
                        userAddress.setText("Address: " + snapshot.child("location").getValue(String.class));
                    } else {
                        showToast("Error: User information not found.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    showToast("Error: " + error.getMessage());
                }
            });
        }
    }
    private void confirmJoinClass() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            showToast("You need to log in to join the class.");
            return;
        }

        if (courseId == null || courseId.isEmpty()) {
            showToast("Error: Course ID is missing.");
            return;
        }

        DatabaseReference courseRef = coursesReference.child(courseId);

        courseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DataCourse course = snapshot.getValue(DataCourse.class);
                    if (course != null) {
                        // Initialize participants if null
                        HashMap<String, Boolean> participants = course.getParticipants();
                        if (participants == null) {
                            participants = new HashMap<>();
                        }

                        // Check if the class is full
                        if (participants.size() >= course.getCapacity()) {
                            showToast("Class is full!");
                            return;
                        }

                        // Add current user to participants
                        participants.put(currentUser.getUid(), true);

                        // Update Firebase with new participants list
                        courseRef.child("participants").setValue(participants).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                showToast("You have joined the class!");

                            } else {
                                showToast("Failed to join class.");
                            }
                        });
                        // Update UI with new capacity
                        detailCapacity.setText("Capacity: " + participants.size() + "/" + course.getCapacity());
                    } else {
                        showToast("Error: Course data is missing.");
                    }
                } else {
                    showToast("Class not found.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Error: " + error.getMessage());
            }
        });
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
