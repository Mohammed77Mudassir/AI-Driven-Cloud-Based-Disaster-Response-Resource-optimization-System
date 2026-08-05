package com.disaster.dto.shelter;

/**
 * Operational alert raised for a shelter when a metric crosses its safety
 * threshold (near/full occupancy, food or water shortage, low medical stock,
 * power failure or communication loss).
 */
public class ShelterAlertDTO {

    private String severity;
    private String category;
    private String message;
    private Long shelterId;
    private String shelterName;

    public ShelterAlertDTO() {}

    public ShelterAlertDTO(String severity, String category, String message, Long shelterId, String shelterName) {
        this.severity = severity;
        this.category = category;
        this.message = message;
        this.shelterId = shelterId;
        this.shelterName = shelterName;
    }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Long getShelterId() { return shelterId; }
    public void setShelterId(Long shelterId) { this.shelterId = shelterId; }
    public String getShelterName() { return shelterName; }
    public void setShelterName(String shelterName) { this.shelterName = shelterName; }
}
