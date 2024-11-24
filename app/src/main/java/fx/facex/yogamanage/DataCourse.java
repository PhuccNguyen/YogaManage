package fx.facex.yogamanage;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

import java.util.HashMap;

public class DataCourse implements Parcelable {

    private String id; // Unique identifier for the course
    private String dayOfWeek;
    private String timeOfCourse;
    private int capacity;
    private int duration; // in minutes or hours
    private double price;
    private String type;
    private String description;
    private String dataImage; // Base64 encoded image
    private HashMap<String, Boolean> participants; // Store user IDs as keys and true as value
    private String postedBy; // User ID of the poster
    private String postedByName; // Name of the poster

    // Default constructor (required for Firebase or similar frameworks)
    public DataCourse() {
        this.dayOfWeek = "Unknown";
        this.timeOfCourse = "00:00";
        this.capacity = 0;
        this.duration = 0;
        this.price = 0.0;
        this.type = "Not specified";
        this.description = "No description available";
        this.dataImage = "";
        this.participants = new HashMap<>(); // Initialize the participants map
        this.postedBy = "Unknown";
        this.postedByName = "Unknown";
    }

    // Parameterized constructor
    public DataCourse(String dayOfWeek, String timeOfCourse, int capacity, int duration,
                      double price, String type, String description, String dataImage,
                      HashMap<String, Boolean> participants, String postedBy, String postedByName) {
        this.dayOfWeek = dayOfWeek != null ? dayOfWeek : "Unknown";
        this.timeOfCourse = timeOfCourse != null ? timeOfCourse : "00:00";
        this.capacity = Math.max(0, capacity); // Ensure capacity is not negative
        this.duration = Math.max(0, duration); // Ensure duration is not negative
        this.price = Math.max(0.0, price); // Ensure price is not negative
        this.type = type != null ? type : "Not specified";
        this.description = description != null ? description : "No description available";
        this.dataImage = dataImage != null ? dataImage : ""; // Ensure non-null value
        this.participants = participants != null ? participants : new HashMap<>();
        this.postedBy = postedBy != null ? postedBy : "Unknown";
        this.postedByName = postedByName != null ? postedByName : "Unknown";
    }

    // Parcelable implementation
    protected DataCourse(Parcel in) {
        id = in.readString();
        dayOfWeek = in.readString();
        timeOfCourse = in.readString();
        capacity = in.readInt();
        duration = in.readInt();
        price = in.readDouble();
        type = in.readString();
        description = in.readString();
        dataImage = in.readString();
        postedBy = in.readString();
        postedByName = in.readString();
        participants = (HashMap<String, Boolean>) in.readSerializable();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(dayOfWeek);
        dest.writeString(timeOfCourse);
        dest.writeInt(capacity);
        dest.writeInt(duration);
        dest.writeDouble(price);
        dest.writeString(type);
        dest.writeString(description);
        dest.writeString(dataImage);
        dest.writeString(postedBy);
        dest.writeString(postedByName);
        dest.writeSerializable(participants);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<DataCourse> CREATOR = new Parcelable.Creator<DataCourse>() {
        @Override
        public DataCourse createFromParcel(Parcel in) {
            return new DataCourse(in);
        }

        @Override
        public DataCourse[] newArray(int size) {
            return new DataCourse[size];
        }
    };

    // Getters
    public String getId() {
        return id;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public String getTimeOfCourse() {
        return timeOfCourse;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getDuration() {
        return duration;
    }

    public double getPrice() {
        return price;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getDataImage() {
        return dataImage;
    }

    public String CourseId() {
        return id;
    }

    public HashMap<String, Boolean> getParticipants() {
        return participants;
    }

    public String getPostedBy() {
        return postedBy;
    }

    public String getPostedByName() {
        return postedByName;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek != null ? dayOfWeek : "Unknown";
    }

    public void setTimeOfCourse(String timeOfCourse) {
        this.timeOfCourse = timeOfCourse != null ? timeOfCourse : "00:00";
    }

    public void setCapacity(int capacity) {
        this.capacity = Math.max(0, capacity);
    }

    public void setDuration(int duration) {
        this.duration = Math.max(0, duration);
    }

    public void setPrice(double price) {
        this.price = Math.max(0.0, price);
    }

    public void setType(String type) {
        this.type = type != null ? type : "Not specified";
    }

    public void setDescription(String description) {
        this.description = description != null ? description : "No description available";
    }

    public void setDataImage(String dataImage) {
        this.dataImage = dataImage != null ? dataImage : "";
    }

    public void setParticipants(HashMap<String, Boolean> participants) {
        this.participants = participants != null ? participants : new HashMap<>();
    }

    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy != null ? postedBy : "Unknown";
    }

    public void setPostedByName(String postedByName) {
        this.postedByName = postedByName != null ? postedByName : "Unknown";
    }

    @Override
    public String toString() {
        return "DataCourse{" +
                "id='" + id + '\'' +
                ", dayOfWeek='" + dayOfWeek + '\'' +
                ", timeOfCourse='" + timeOfCourse + '\'' +
                ", capacity=" + capacity +
                ", duration=" + duration +
                ", price=" + price +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", dataImage='" + (dataImage != null ? dataImage : "No Image") + '\'' +
                ", participants=" + participants +
                ", postedBy='" + postedBy + '\'' +
                ", postedByName='" + postedByName + '\'' +
                '}';
    }
}
