package com.disaster.dto.shelter;

/**
 * Aggregated operational metrics for a single shelter. All derived values
 * (occupancy, food/water/medical posture, power &amp; communications state,
 * readiness score and capacity forecast) are computed by the service layer
 * from the shelter's stored attributes using documented estimate formulas
 * when live telemetry is unavailable.
 */
public class ShelterOperationsDTO {

    private Long id;
    private String name;
    private String address;
    private String contact;
    private double latitude;
    private double longitude;

    // Occupancy
    private int capacity;
    private int occupancy;
    private int availableSpace;
    private double occupancyPercent;
    private String status;

    // Food
    private int foodPacks;
    private int dailyFoodConsumption;
    private double foodDaysRemaining;
    private String foodStatus;

    // Water
    private int waterLitres;
    private int dailyWaterRequirement;
    private double waterDaysRemaining;
    private String waterStatus;

    // Medical kits
    private int totalMedicalKits;
    private int medicalKitsInUse;
    private int availableMedicalKits;
    private int minRequiredMedicalKits;
    private double medicalReadinessPercent;

    // Electricity
    private boolean gridAvailable;
    private boolean generatorAvailable;
    private int generatorFuelPercent;
    private double backupRuntimeHours;
    private String powerStatus;

    // Internet & communications
    private boolean internetAvailable;
    private String networkType;
    private int signalStrengthPercent;
    private String commStatus;

    // Sanitation
    private double sanitationPercent;
    private boolean sanitationAvailable;

    // Readiness
    private double readinessScore;
    private String readinessLevel;

    // Capacity forecast (estimated)
    private int dailyArrivals;
    private int dailyDepartures;
    private int netDailyChange;
    private String forecastTrend;
    private Integer estimatedDaysToFull;
    private String estimatedFullDate;

    public ShelterOperationsDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public int getOccupancy() { return occupancy; }
    public void setOccupancy(int occupancy) { this.occupancy = occupancy; }
    public int getAvailableSpace() { return availableSpace; }
    public void setAvailableSpace(int availableSpace) { this.availableSpace = availableSpace; }
    public double getOccupancyPercent() { return occupancyPercent; }
    public void setOccupancyPercent(double occupancyPercent) { this.occupancyPercent = occupancyPercent; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getFoodPacks() { return foodPacks; }
    public void setFoodPacks(int foodPacks) { this.foodPacks = foodPacks; }
    public int getDailyFoodConsumption() { return dailyFoodConsumption; }
    public void setDailyFoodConsumption(int dailyFoodConsumption) { this.dailyFoodConsumption = dailyFoodConsumption; }
    public double getFoodDaysRemaining() { return foodDaysRemaining; }
    public void setFoodDaysRemaining(double foodDaysRemaining) { this.foodDaysRemaining = foodDaysRemaining; }
    public String getFoodStatus() { return foodStatus; }
    public void setFoodStatus(String foodStatus) { this.foodStatus = foodStatus; }
    public int getWaterLitres() { return waterLitres; }
    public void setWaterLitres(int waterLitres) { this.waterLitres = waterLitres; }
    public int getDailyWaterRequirement() { return dailyWaterRequirement; }
    public void setDailyWaterRequirement(int dailyWaterRequirement) { this.dailyWaterRequirement = dailyWaterRequirement; }
    public double getWaterDaysRemaining() { return waterDaysRemaining; }
    public void setWaterDaysRemaining(double waterDaysRemaining) { this.waterDaysRemaining = waterDaysRemaining; }
    public String getWaterStatus() { return waterStatus; }
    public void setWaterStatus(String waterStatus) { this.waterStatus = waterStatus; }
    public int getTotalMedicalKits() { return totalMedicalKits; }
    public void setTotalMedicalKits(int totalMedicalKits) { this.totalMedicalKits = totalMedicalKits; }
    public int getMedicalKitsInUse() { return medicalKitsInUse; }
    public void setMedicalKitsInUse(int medicalKitsInUse) { this.medicalKitsInUse = medicalKitsInUse; }
    public int getAvailableMedicalKits() { return availableMedicalKits; }
    public void setAvailableMedicalKits(int availableMedicalKits) { this.availableMedicalKits = availableMedicalKits; }
    public int getMinRequiredMedicalKits() { return minRequiredMedicalKits; }
    public void setMinRequiredMedicalKits(int minRequiredMedicalKits) { this.minRequiredMedicalKits = minRequiredMedicalKits; }
    public double getMedicalReadinessPercent() { return medicalReadinessPercent; }
    public void setMedicalReadinessPercent(double medicalReadinessPercent) { this.medicalReadinessPercent = medicalReadinessPercent; }
    public boolean isGridAvailable() { return gridAvailable; }
    public void setGridAvailable(boolean gridAvailable) { this.gridAvailable = gridAvailable; }
    public boolean isGeneratorAvailable() { return generatorAvailable; }
    public void setGeneratorAvailable(boolean generatorAvailable) { this.generatorAvailable = generatorAvailable; }
    public int getGeneratorFuelPercent() { return generatorFuelPercent; }
    public void setGeneratorFuelPercent(int generatorFuelPercent) { this.generatorFuelPercent = generatorFuelPercent; }
    public double getBackupRuntimeHours() { return backupRuntimeHours; }
    public void setBackupRuntimeHours(double backupRuntimeHours) { this.backupRuntimeHours = backupRuntimeHours; }
    public String getPowerStatus() { return powerStatus; }
    public void setPowerStatus(String powerStatus) { this.powerStatus = powerStatus; }
    public boolean isInternetAvailable() { return internetAvailable; }
    public void setInternetAvailable(boolean internetAvailable) { this.internetAvailable = internetAvailable; }
    public String getNetworkType() { return networkType; }
    public void setNetworkType(String networkType) { this.networkType = networkType; }
    public int getSignalStrengthPercent() { return signalStrengthPercent; }
    public void setSignalStrengthPercent(int signalStrengthPercent) { this.signalStrengthPercent = signalStrengthPercent; }
    public String getCommStatus() { return commStatus; }
    public void setCommStatus(String commStatus) { this.commStatus = commStatus; }
    public double getSanitationPercent() { return sanitationPercent; }
    public void setSanitationPercent(double sanitationPercent) { this.sanitationPercent = sanitationPercent; }
    public boolean isSanitationAvailable() { return sanitationAvailable; }
    public void setSanitationAvailable(boolean sanitationAvailable) { this.sanitationAvailable = sanitationAvailable; }
    public double getReadinessScore() { return readinessScore; }
    public void setReadinessScore(double readinessScore) { this.readinessScore = readinessScore; }
    public String getReadinessLevel() { return readinessLevel; }
    public void setReadinessLevel(String readinessLevel) { this.readinessLevel = readinessLevel; }
    public int getDailyArrivals() { return dailyArrivals; }
    public void setDailyArrivals(int dailyArrivals) { this.dailyArrivals = dailyArrivals; }
    public int getDailyDepartures() { return dailyDepartures; }
    public void setDailyDepartures(int dailyDepartures) { this.dailyDepartures = dailyDepartures; }
    public int getNetDailyChange() { return netDailyChange; }
    public void setNetDailyChange(int netDailyChange) { this.netDailyChange = netDailyChange; }
    public String getForecastTrend() { return forecastTrend; }
    public void setForecastTrend(String forecastTrend) { this.forecastTrend = forecastTrend; }
    public Integer getEstimatedDaysToFull() { return estimatedDaysToFull; }
    public void setEstimatedDaysToFull(Integer estimatedDaysToFull) { this.estimatedDaysToFull = estimatedDaysToFull; }
    public String getEstimatedFullDate() { return estimatedFullDate; }
    public void setEstimatedFullDate(String estimatedFullDate) { this.estimatedFullDate = estimatedFullDate; }
}
