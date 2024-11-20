package fx.facex.yogamanage;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class UploadActivity extends AppCompatActivity {

    ImageView uploadImage;
    Button saveYogaCourseButton;
    EditText capacity, duration, price, type, description;
    TextView timeOfCourse, dayOfWeek;
    String imageBase64; // Base64 string for the image
    Uri uri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // Initialize UI components
        uploadImage = findViewById(R.id.uploadImage);
        dayOfWeek = findViewById(R.id.dayOfWeek);
        saveYogaCourseButton = findViewById(R.id.saveYogaCourseButton);
        timeOfCourse = findViewById(R.id.timeOfCourse);
        capacity = findViewById(R.id.capacity);
        duration = findViewById(R.id.duration);
        price = findViewById(R.id.price);
        type = findViewById(R.id.type);
        description = findViewById(R.id.description);

        // Day of week selection
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        boolean[] selectedDays = new boolean[days.length];
        ArrayList<String> selectedDayList = new ArrayList<>();

        dayOfWeek.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(UploadActivity.this);
            builder.setTitle("Select Day(s) of the Week");
            builder.setMultiChoiceItems(days, selectedDays, (dialog, which, isChecked) -> {
                if (isChecked) {
                    selectedDayList.add(days[which]);
                } else {
                    selectedDayList.remove(days[which]);
                }
            });
            builder.setPositiveButton("OK", (dialog, which) -> dayOfWeek.setText(TextUtils.join(", ", selectedDayList)));
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.show();
        });

        // Time Picker for course time
        timeOfCourse.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    UploadActivity.this,
                    (view1, hourOfDay, minute1) -> {
                        String time = String.format("%02d:%02d", hourOfDay, minute1);
                        timeOfCourse.setText(time);
                    },
                    hour, minute, true
            );
            timePickerDialog.show();
        });

        // Image selection
        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            uri = data.getData();
                            uploadImage.setImageURI(uri); // Display selected image

                            // Encode image to Base64
                            encodeImageToBase64(uri);
                        } else {
                            Toast.makeText(UploadActivity.this, "No Image Selected!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // On image click
        uploadImage.setOnClickListener(view -> {
            Intent photoPicker = new Intent(Intent.ACTION_PICK);
            photoPicker.setType("image/*");
            activityResultLauncher.launch(photoPicker);
        });

        // Save data on button click
        saveYogaCourseButton.setOnClickListener(view -> saveData());
    }



    // Generate a random 6-digit prefix and append .png
    private String generateRandomFileName() {
        Random random = new Random();
        int number = random.nextInt(999999);
        return String.format("%06d", number);
    }

    // Extract file name from URI
    private String getFileNameFromUri(Uri uri) {
        String[] pathSegments = uri.getPath().split("/");
        return pathSegments[pathSegments.length - 1];
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxWidth;
            height = (int) (maxWidth / bitmapRatio);
        } else {
            height = maxHeight;
            width = (int) (maxHeight * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }


    private void encodeImageToBase64(Uri imageUri) {
        try {
            // Mở InputStream từ URI
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // Resize ảnh trước khi mã hóa
            Bitmap resizedBitmap = resizeBitmap(bitmap, 800, 800);

            // Chuyển Bitmap sang Base64
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);

            Toast.makeText(this, "Image encoded to Base64", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error encoding image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveData() {
        if (dayOfWeek.getText().toString().isEmpty() ||
                timeOfCourse.getText().toString().isEmpty() ||
                capacity.getText().toString().isEmpty() ||
                duration.getText().toString().isEmpty() ||
                price.getText().toString().isEmpty() ||
                type.getText().toString().isEmpty() ||
                description.getText().toString().isEmpty() ||
                imageBase64 == null) { // Check if image is encoded
            Toast.makeText(this, "Please fill out all fields and select an image.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Fetch current user's full name and ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();


        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String teacherFullName = task.getResult().child("fullName").getValue(String.class);

                String day = dayOfWeek.getText().toString();
                String time = timeOfCourse.getText().toString();
                int cap = Integer.parseInt(capacity.getText().toString());
                int dur = Integer.parseInt(duration.getText().toString());
                double pr = Double.parseDouble(price.getText().toString());
                String typ = type.getText().toString();
                String desc = description.getText().toString();

                DataCourse dataCourse = new DataCourse(day, time, cap, dur, pr, typ, desc, imageBase64, null, userId, teacherFullName);

                String currentDate = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());



                DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference("YogaCourses").push();
                courseRef.setValue(dataCourse).addOnCompleteListener(courseTask -> {
                    dialog.dismiss();
                    if (courseTask.isSuccessful()) {
                        Toast.makeText(this, "Yoga Course Saved Successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to Save Yoga Course: " + courseTask.getException(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } else {
                dialog.dismiss();
                Toast.makeText(this, "Failed to fetch user details!", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
