package com.example.flowerstoreproject.model;

public class Profile {
    private String _id;
    private String fullName;
    private String email;
    private String phone;
    private String avatar;
    private String avvatar; // Thêm để xử lý lỗi đánh máy từ backend
    private String role;
    private boolean isActive;
    private String createdAt;
    private String updatedAt;
    private int __v;

    // Default constructor
    public Profile() {}

    // Parameterized constructor
    public Profile(String fullName, String email, String phone, String avatar, String role) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.avatar = avatar;
        this.role = role;
    }

    // Getters and Setters
    public String getId() { return _id; }
    public void setId(String _id) { this._id = _id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAvatar() {
        // Xử lý trường hợp backend trả về avvatar thay vì avatar
        return avatar != null ? avatar : avvatar;
    }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getAvvatar() { return avvatar; }
    public void setAvvatar(String avvatar) { this.avvatar = avvatar; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean isActive) { this.isActive = isActive; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public int get__v() { return __v; }
    public void set__v(int __v) { this.__v = __v; }

    @Override
    public String toString() {
        return "Profile{" +
                "id='" + _id + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", avatar='" + avatar + '\'' +
                ", avvatar='" + avvatar + '\'' +
                ", role='" + role + '\'' +
                ", isActive=" + isActive +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", __v=" + __v +
                '}';
    }
}