package fx.facex.yogamanage;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ManageYogaAdapter extends RecyclerView.Adapter<ManageYogaAdapter.ViewHolder> {

    private final Context context;
    private final List<DataCourse> courses;
    private final DatabaseReference coursesReference;

    public ManageYogaAdapter(Context context, List<DataCourse> courses) {
        this.context = context;
        this.courses = courses;
        this.coursesReference = FirebaseDatabase.getInstance().getReference("YogaCourses");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item_manage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DataCourse course = courses.get(position);

        // Display course details
        holder.recType.setText(course.getType() != null ? course.getType() : "No Type");
        holder.recDay.setText("Day: " + (course.getDayOfWeek() != null ? course.getDayOfWeek() : "Unknown"));
        holder.recCapacity.setText("Capacity: " + course.getCapacity());
        holder.recPrice.setText("Price: $" + course.getPrice());

        // Handle image from Base64
        setCourseImage(holder.recImage, course.getDataImage());

        // Handle menu actions
        holder.recMenu.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.recMenu);
            popupMenu.inflate(R.menu.course_menu);
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_delete) {
                    deleteCourse(course, position);
                    return true;
                } else if (itemId == R.id.menu_update) {
                    updateCourse(course);
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    /**
     * Delete a course with confirmation
     */
    private void deleteCourse(DataCourse course, int position) {
        if (course.getId() == null || course.getId().isEmpty()) {
            Toast.makeText(context, "Invalid course ID: Unable to delete.", Toast.LENGTH_SHORT).show();
            Log.e("ManageYogaAdapter", "Course ID is null or empty for course: " + course.toString());
            return;
        }

        new AlertDialog.Builder(context)
                .setTitle("Delete Course")
                .setMessage("Are you sure you want to delete this course?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Proceed with deletion
                    coursesReference.child(course.getId()).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Course deleted successfully.", Toast.LENGTH_SHORT).show();
                                courses.remove(position);
                                notifyItemRemoved(position);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to delete course: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e("ManageYogaAdapter", "Failed to delete course: " + e.getMessage());
                            });
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Update course: Navigate to UpdateCourseActivity with course details
     */
    private void updateCourse(DataCourse course) {
        Intent intent = new Intent(context, UpdateCourseActivity.class);
        intent.putExtra("courseData", course); // Pass the Parcelable course object
        context.startActivity(intent);
    }

    /**
     * Set image for a course
     */
    private void setCourseImage(ImageView imageView, String dataImage) {
        if (dataImage != null && !dataImage.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(dataImage, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                Log.e("ManageYogaAdapter", "Error decoding Base64 image: " + e.getMessage());
                imageView.setImageResource(R.drawable.yogaimages);
            }
        } else {
            imageView.setImageResource(R.drawable.yogaimages);
        }
    }

    /**
     * ViewHolder class for RecyclerView
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView recType, recDay, recCapacity, recPrice;
        ImageView recImage, recMenu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recImage = itemView.findViewById(R.id.recImage);
            recType = itemView.findViewById(R.id.recType);
            recDay = itemView.findViewById(R.id.recDay);
            recCapacity = itemView.findViewById(R.id.recCapacity);
            recPrice = itemView.findViewById(R.id.recPrice);
            recMenu = itemView.findViewById(R.id.recMenu);
        }
    }
}
