package fx.facex.yogamanage;

public class User {
    private String fullName;
    private String email;
    private String phone;
    private String location;
    private String userId;

    // Default constructor required for Firebase
    public User() {}

    public User(String fullName, String email, String phone, String location, String userId) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.location = location;
        this.userId = userId;
    }

    // Getters
    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getLocation() {
        return location;
    }

    public String getUserId() {
        return userId;
    }
}
