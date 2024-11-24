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
import android.widget.Button;
import android.widget.ImageView;
import fx.facex.yogamanage.R;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private Context context;

    public MyAdapter(Context context, List<DataCourse> dataCourseList) {
        this.context = context;
        this.dataCourseList = dataCourseList;
    }

    private List<DataCourse> dataCourseList;

    @NonNull

    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        DataCourse currentCourse = dataCourseList.get(position);

        if (currentCourse == null) {
            Log.e("MyAdapter", "Null course at position: " + position);
            return; // Exit early if the currentCourse is null
        }

        Log.d("MyAdapter", "Binding Course: " + currentCourse.toString());

        // Decode Base64 image or use fallback
        if (currentCourse.getDataImage() != null && !currentCourse.getDataImage().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(currentCourse.getDataImage(), Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.recImage.setImageBitmap(decodedBitmap);
            } catch (Exception e) {
                holder.recImage.setImageResource(R.drawable.yogaimages);
                Log.e("MyAdapter", "Image decoding error: " + e.getMessage());
            }
        } else {
            holder.recImage.setImageResource(R.drawable.yogaimages);
        }



        // Set data into views
        holder.recType.setText(currentCourse.getType());
        holder.recDay.setText("Day: " + currentCourse.getDayOfWeek());
        holder.recCapacity.setText("Capacity: " + currentCourse.getCapacity());
        holder.recPrice.setText("Price: $" + currentCourse.getPrice());

        // Set click listener for the "View Details" button
        holder.recViewDetailButton.setOnClickListener(v -> {
            Intent detailIntent = new Intent(context, DetailActivity.class);

            // Pass data to DetailActivity
            detailIntent.putExtra("courseId", currentCourse.getId()); // Add courseId
            detailIntent.putExtra("dataImage", currentCourse.getDataImage());
            detailIntent.putExtra("type", currentCourse.getType());
            detailIntent.putExtra("dayOfWeek", currentCourse.getDayOfWeek());
            detailIntent.putExtra("timeOfCourse", currentCourse.getTimeOfCourse());
            detailIntent.putExtra("capacity", currentCourse.getCapacity());
            detailIntent.putExtra("teacher", currentCourse.getPostedByName());
            detailIntent.putExtra("duration", currentCourse.getDuration());
            detailIntent.putExtra("price", currentCourse.getPrice());
            detailIntent.putExtra("description", currentCourse.getDescription());

            context.startActivity(detailIntent);
        });
    }



    @Override
    public int getItemCount() {
        return dataCourseList.size();
    }

    public void searchDataCourseList(ArrayList<DataCourse> searchList) {
        dataCourseList = searchList;
        notifyDataSetChanged();
    }
}

class MyViewHolder extends RecyclerView.ViewHolder {

    ImageView recImage;
    TextView recType, recDay, recCapacity, recPrice;
    Button recViewDetailButton;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);

        // Initialize the views
        recImage = itemView.findViewById(R.id.recImage);
        recType = itemView.findViewById(R.id.recType);
        recDay = itemView.findViewById(R.id.recDay);
        recCapacity = itemView.findViewById(R.id.recCapacity);
        recPrice = itemView.findViewById(R.id.recPrice);
        recViewDetailButton = itemView.findViewById(R.id.recViewDetailButton);



    }
}
