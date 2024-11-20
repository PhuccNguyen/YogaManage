package fx.facex.yogamanage;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UpdateCourseActivity extends AppCompatActivity {

    private EditText edtType, edtDay, edtCapacity, edtDuration, edtPrice, edtDescription;
    private Button btnUpdate;

    private DatabaseReference coursesReference;
    private String courseId, userId, teacherFullName, existingImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_course);

        // Initialize views
        edtType = findViewById(R.id.edtType);
        edtDay = findViewById(R.id.edtDay);
        edtCapacity = findViewById(R.id.edtCapacity);
        edtDuration = findViewById(R.id.edtDuration);
        edtPrice = findViewById(R.id.edtPrice);
        edtDescription = findViewById(R.id.edtDescription);
        btnUpdate = findViewById(R.id.btnUpdate);

        // Initialize Firebase reference
        coursesReference = FirebaseDatabase.getInstance().getReference("YogaCourses");

        // Retrieve course data from the intent
        DataCourse course = getIntent().getParcelableExtra("courseData");
        if (course != null) {
            populateFields(course); // Populate fields with course data
            courseId = course.getId(); // Store course ID for updates
            userId = course.getPostedBy(); // Fetch user ID of the course creator
            teacherFullName = course.getPostedByName(); // Fetch teacher's full name
            existingImage = course.getDataImage(); // Preserve the current image
        } else {
            Toast.makeText(this, "Error: No course data provided.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Handle Update button click
        btnUpdate.setOnClickListener(view -> {
            if (TextUtils.isEmpty(courseId)) {
                Toast.makeText(this, "Error: Invalid course ID.", Toast.LENGTH_SHORT).show();
                return;
            }
            updateCourseDetails();
        });
    }

    /**
     * Populate fields with the existing course data
     */
    private void populateFields(@NonNull DataCourse course) {
        edtType.setText(course.getType());
        edtDay.setText(course.getDayOfWeek());
        edtCapacity.setText(String.valueOf(course.getCapacity()));
        edtDuration.setText(String.valueOf(course.getDuration()));
        edtPrice.setText(String.valueOf(course.getPrice()));
        edtDescription.setText(course.getDescription());
    }

    /**
     * Validate and update course details in Firebase
     */
    private void updateCourseDetails() {
        String updatedType = edtType.getText().toString().trim();
        String updatedDay = edtDay.getText().toString().trim();
        String updatedCapacityStr = edtCapacity.getText().toString().trim();
        String updatedDurationStr = edtDuration.getText().toString().trim();
        String updatedPriceStr = edtPrice.getText().toString().trim();
        String updatedDescription = edtDescription.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(updatedType) || TextUtils.isEmpty(updatedDay) ||
                TextUtils.isEmpty(updatedCapacityStr) || TextUtils.isEmpty(updatedDurationStr) ||
                TextUtils.isEmpty(updatedPriceStr) || TextUtils.isEmpty(updatedDescription)) {
            Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        int updatedCapacity = Integer.parseInt(updatedCapacityStr);
        int updatedDuration = Integer.parseInt(updatedDurationStr);
        double updatedPrice = Double.parseDouble(updatedPriceStr);

        // Ensure `userId`, `teacherFullName`, and `existingImage` are preserved
        if (userId == null || teacherFullName == null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            teacherFullName = "Unknown Teacher"; // Default value if teacher's name is unavailable
        }
        if (existingImage == null) {
            existingImage = ""; // Fallback to empty if no image is provided
        }

        // Update course data
        DataCourse updatedCourse = new DataCourse(
                updatedDay,
                getIntent().getStringExtra("timeOfCourse"), // Keep time unchanged
                updatedCapacity,
                updatedDuration,
                updatedPrice,
                updatedType,
                updatedDescription,
                existingImage, // Keep the current image unchanged
                null, // No changes to participants
                userId,
                teacherFullName
        );
        updatedCourse.setId(courseId); // Ensure course ID is set

        // Save updated course to Firebase
        coursesReference.child(courseId).setValue(updatedCourse)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Course updated successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Close activity after success
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update course: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
