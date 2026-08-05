package com.disaster.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateUserRequest {
    @Email(message = "A valid email address is required")
    @Size(max = 100, message = "Email must be at most 100 characters")
    private String email;
    @Pattern(regexp = "^[+0-9\\-() ]{8,15}$", message = "Phone number must be 8-15 digits")
    private String phone;
    @Size(max = 500, message = "Address must be at most 500 characters")
    private String address;
    private String profilePhoto;
    @Size(max = 100, message = "Emergency contact must be at most 100 characters")
    private String emergencyContact;

    public UpdateUserRequest() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }
    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }
}
