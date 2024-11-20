package fx.facex.yogamanage;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageYogaActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ManageYogaAdapter adapter;
    private List<DataCourse> teacherCourses;
    private DatabaseReference coursesReference;
    private FirebaseAuth auth;
    private AlertDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_yoga);

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Log.e("ManageYogaActivity", "User is not logged in.");
            finish(); // Close the activity if no user is logged in
            return;
        }

        String teacherId = currentUser.getUid();

        // Initialize RecyclerView and Adapter
        recyclerView = findViewById(R.id.recyclerViewManageYoga);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        teacherCourses = new ArrayList<>();
        adapter = new ManageYogaAdapter(this, teacherCourses);
        recyclerView.setAdapter(adapter);

        // Setup Loading Dialog
        setupLoadingDialog();

        // Initialize Firebase Database Reference
        coursesReference = FirebaseDatabase.getInstance().getReference("YogaCourses");

        // Fetch teacher's courses
        fetchTeacherCourses(teacherId);
    }

    private void setupLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.loading_layout);
        builder.setCancelable(false);
        loadingDialog = builder.create();
        loadingDialog.show();
    }

    private void fetchTeacherCourses(String teacherId) {
        coursesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                teacherCourses.clear();
                for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                    DataCourse course = courseSnapshot.getValue(DataCourse.class);
                    if (course != null && teacherId.equals(course.getPostedBy())) {
                        course.setId(courseSnapshot.getKey()); // Set course ID from Firebase
                        teacherCourses.add(course);
                    }
                }
                adapter.notifyDataSetChanged();
                dismissLoadingDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ManageYogaActivity", "Error fetching data: " + error.getMessage());
                dismissLoadingDialog();
                showErrorDialog("Error fetching data", error.getMessage());
            }
        });
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
}
