package fx.facex.yogamanage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private Button buttonLogout;
    private TextView textViewUserDetails;

    private FloatingActionButton fab;

    private List<DataCourse> dataCourseList;
    private RecyclerView recyclerView;
    private DatabaseReference coursesReference, usersReference;
    private MyAdapter adapter;
    private AlertDialog dialog;

    private LinearLayout homeButton, profileButton;

    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase and UI components
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        textViewUserDetails = findViewById(R.id.user_details);
        fab = findViewById(R.id.fad);
        recyclerView = findViewById(R.id.recyclerviews);

        homeButton = findViewById(R.id.homeButton);
        profileButton = findViewById(R.id.profileButton);

        searchView = findViewById(R.id.search);
        searchView.clearFocus();



        // Set up RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 1));
        dataCourseList = new ArrayList<>();
        adapter = new MyAdapter(MainActivity.this, dataCourseList);
        recyclerView.setAdapter(adapter);

        // Loading dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(false);
        builder.setView(R.layout.loading_layout);
        dialog = builder.create();
        dialog.show();

        // Firebase Database References
        coursesReference = FirebaseDatabase.getInstance().getReference("YogaCourses");
        usersReference = FirebaseDatabase.getInstance().getReference("Users");

        // Fetch and display user details
        if (firebaseUser != null) {
            fetchUserDetails(firebaseUser.getUid());
        }

        // Fetch data from Firebase
        fetchCoursesFromFirebase();

        // Floating Action Button
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, UploadActivity.class);
            startActivity(intent);
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform search when the user submits the query
                filterList(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Perform search dynamically as the user types
                filterList(newText);
                return true;
            }
        });





        // Profile Button Logic
        profileButton.setOnClickListener(view -> {
            // Navigate to ProfileActivity
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

    }

    private void fetchUserDetails(String userId) {
        usersReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fullName = snapshot.child("fullName").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    textViewUserDetails.setText("Welcome, " + fullName);
                } else {
                    textViewUserDetails.setText("Error fetching user details.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MainActivity", "Error fetching user details: " + error.getMessage());
            }
        });
    }

    private void fetchCoursesFromFirebase() {
        coursesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataCourseList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        DataCourse course = dataSnapshot.getValue(DataCourse.class);
                        if (course != null) {
                            dataCourseList.add(course);
                            Log.d("MainActivity", "Course Loaded: " + course.toString());
                        } else {
                            Log.e("MainActivity", "Invalid course data at snapshot: " + dataSnapshot.getKey());
                        }
                    }
                    adapter.notifyDataSetChanged();
                    Log.d("MainActivity", "DataCourse List Size: " + dataCourseList.size());
                } else {
                    Log.e("MainActivity", "No data found in Firebase.");
                }
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss();
                Log.e("MainActivity", "Database error: " + error.getMessage());
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Error")
                        .setMessage("Failed to load data: " + error.getMessage())
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    private void filterList(String text) {
        ArrayList<DataCourse> filteredList = new ArrayList<>();
        for (DataCourse dataCourse : dataCourseList) {
            if (dataCourse.getPostedByName() != null && dataCourse.getType() != null) {
                // Check if the teacher's name or type matches the search text
                if (dataCourse.getPostedByName().toLowerCase().contains(text.toLowerCase()) ||
                        dataCourse.getType().toLowerCase().contains(text.toLowerCase())) {
                    filteredList.add(dataCourse);
                }
            }
        }

        if (filteredList.isEmpty()) {
            Log.d("MainActivity", "No matching courses found for: " + text);
        } else {
            Log.d("MainActivity", "Filtered List Size: " + filteredList.size());
        }

        // Update the adapter with the filtered list
        adapter.searchDataCourseList(filteredList);
    }


}
