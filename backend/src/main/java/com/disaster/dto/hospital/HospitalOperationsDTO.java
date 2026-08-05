package com.disaster.dto.hospital;

import com.disaster.entity.Hospital;

import java.util.List;

/**
 * Aggregated operational metrics for a single hospital. All derived values
 * (occupancy, ICU pressure, emergency capacity, waiting time, status) are
 * computed by the service layer from the hospital's current resource levels
 * using documented estimate formulas when live telemetry is unavailable.
 */
public class HospitalOperationsDTO {

    private Long id;
    private String name;
    private String address;
    private String emergencyContact;
    private double latitude;
    private double longitude;
    private boolean bloodBankPresent;

    private int availableBeds;
    private int totalBeds;
    private int occupiedBeds;
    private double bedUtilizationPercent;

    private int availableIcuBeds;
    private int totalIcuBeds;
    private int occupiedIcuBeds;
    private double icuOccupancyPercent;

    private int totalDoctors;
    private int doctorsAvailable;
    private int doctorsOnDuty;
    private int doctorsOffDuty;
    private double doctorAvailabilityPercent;

    private double emergencyCapacityScore;
    private String emergencyCapacityLevel;

    private int patientLoad;
    private int estimatedWaitingTimeMinutes;

    private String status;

    private int bloodUnits;
    private String bloodStockStatus;
    private List<BloodGroupDTO> bloodGroups;

    public HospitalOperationsDTO() {}

    public static HospitalOperationsDTO fromEntity(Hospital h) {
        HospitalOperationsDTO dto = new HospitalOperationsDTO();
        dto.setId(h.getId());
        dto.setName(h.getName());
        dto.setAddress(h.getAddress());
        dto.setEmergencyContact(h.getEmergencyContact());
        dto.setLatitude(h.getLatitude());
        dto.setLongitude(h.getLongitude());
        dto.setBloodBankPresent(h.isBloodBank());
        dto.setAvailableBeds(h.getAvailableBeds());
        dto.setAvailableIcuBeds(h.getIcuBeds());
        dto.setDoctorsAvailable(h.getDoctorsAvailable());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public boolean isBloodBankPresent() { return bloodBankPresent; }
    public void setBloodBankPresent(boolean bloodBankPresent) { this.bloodBankPresent = bloodBankPresent; }
    public int getAvailableBeds() { return availableBeds; }
    public void setAvailableBeds(int availableBeds) { this.availableBeds = availableBeds; }
    public int getTotalBeds() { return totalBeds; }
    public void setTotalBeds(int totalBeds) { this.totalBeds = totalBeds; }
    public int getOccupiedBeds() { return occupiedBeds; }
    public void setOccupiedBeds(int occupiedBeds) { this.occupiedBeds = occupiedBeds; }
    public double getBedUtilizationPercent() { return bedUtilizationPercent; }
    public void setBedUtilizationPercent(double bedUtilizationPercent) { this.bedUtilizationPercent = bedUtilizationPercent; }
    public int getAvailableIcuBeds() { return availableIcuBeds; }
    public void setAvailableIcuBeds(int availableIcuBeds) { this.availableIcuBeds = availableIcuBeds; }
    public int getTotalIcuBeds() { return totalIcuBeds; }
    public void setTotalIcuBeds(int totalIcuBeds) { this.totalIcuBeds = totalIcuBeds; }
    public int getOccupiedIcuBeds() { return occupiedIcuBeds; }
    public void setOccupiedIcuBeds(int occupiedIcuBeds) { this.occupiedIcuBeds = occupiedIcuBeds; }
    public double getIcuOccupancyPercent() { return icuOccupancyPercent; }
    public void setIcuOccupancyPercent(double icuOccupancyPercent) { this.icuOccupancyPercent = icuOccupancyPercent; }
    public int getTotalDoctors() { return totalDoctors; }
    public void setTotalDoctors(int totalDoctors) { this.totalDoctors = totalDoctors; }
    public int getDoctorsAvailable() { return doctorsAvailable; }
    public void setDoctorsAvailable(int doctorsAvailable) { this.doctorsAvailable = doctorsAvailable; }
    public int getDoctorsOnDuty() { return doctorsOnDuty; }
    public void setDoctorsOnDuty(int doctorsOnDuty) { this.doctorsOnDuty = doctorsOnDuty; }
    public int getDoctorsOffDuty() { return doctorsOffDuty; }
    public void setDoctorsOffDuty(int doctorsOffDuty) { this.doctorsOffDuty = doctorsOffDuty; }
    public double getDoctorAvailabilityPercent() { return doctorAvailabilityPercent; }
    public void setDoctorAvailabilityPercent(double doctorAvailabilityPercent) { this.doctorAvailabilityPercent = doctorAvailabilityPercent; }
    public double getEmergencyCapacityScore() { return emergencyCapacityScore; }
    public void setEmergencyCapacityScore(double emergencyCapacityScore) { this.emergencyCapacityScore = emergencyCapacityScore; }
    public String getEmergencyCapacityLevel() { return emergencyCapacityLevel; }
    public void setEmergencyCapacityLevel(String emergencyCapacityLevel) { this.emergencyCapacityLevel = emergencyCapacityLevel; }
    public int getPatientLoad() { return patientLoad; }
    public void setPatientLoad(int patientLoad) { this.patientLoad = patientLoad; }
    public int getEstimatedWaitingTimeMinutes() { return estimatedWaitingTimeMinutes; }
    public void setEstimatedWaitingTimeMinutes(int estimatedWaitingTimeMinutes) { this.estimatedWaitingTimeMinutes = estimatedWaitingTimeMinutes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getBloodUnits() { return bloodUnits; }
    public void setBloodUnits(int bloodUnits) { this.bloodUnits = bloodUnits; }
    public String getBloodStockStatus() { return bloodStockStatus; }
    public void setBloodStockStatus(String bloodStockStatus) { this.bloodStockStatus = bloodStockStatus; }
    public List<BloodGroupDTO> getBloodGroups() { return bloodGroups; }
    public void setBloodGroups(List<BloodGroupDTO> bloodGroups) { this.bloodGroups = bloodGroups; }
}
