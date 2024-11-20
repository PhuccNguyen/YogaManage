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

import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    private ImageView detailImage;
    private TextView detailType, detailDayAndTime, detailCapacity, detailDuration, detailTeacher, detailPrice, detailDescription;
    private Button detailJoinButton, detailCommentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Initialize views
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

        // Retrieve data from the Intent
        Intent intent = getIntent();
        if (intent != null) {
            // Set the image
            String dataImage = intent.getStringExtra("dataImage");
            if (dataImage != null && !dataImage.isEmpty()) {
                byte[] decodedString = Base64.decode(dataImage, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                detailImage.setImageBitmap(decodedBitmap);
            } else {
                detailImage.setImageResource(R.drawable.yogaimages); // Placeholder image
            }

            // Set other details
            detailType.setText(intent.getStringExtra("type"));
            detailDayAndTime.setText("Day: " + intent.getStringExtra("dayOfWeek") + ", Time: " + intent.getStringExtra("timeOfCourse"));
            detailCapacity.setText("Capacity: " + intent.getIntExtra("capacity", 0));
            detailDuration.setText("Duration: " + intent.getIntExtra("duration", 0) + " mins");
            detailTeacher.setText("Teacher: " + intent.getStringExtra("teacher"));
            detailPrice.setText("Price: $" + intent.getDoubleExtra("price", 0.0));
            detailDescription.setText(intent.getStringExtra("description"));
        }

        // Handle Join Button click
        detailJoinButton.setOnClickListener(view -> {
            // Logic for joining the class
            // You can add Firebase or any other implementation here
            // Example: Toast message for now
            showMessage("Join Class clicked");
        });

        // Handle Comment Button click
        detailCommentButton.setOnClickListener(view -> {
            // Logic for viewing comments
            // Example: Navigate to a CommentsActivity or show a dialog
            showMessage("View Comments clicked");
        });
    }

    /**
     * Show a message (for testing purposes)
     */
    private void showMessage(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }

}
