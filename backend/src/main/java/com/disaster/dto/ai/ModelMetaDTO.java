package com.disaster.dto.ai;

/**
 * Metadata describing the prediction model that produced a result. This is what
 * makes the AI layer replaceable: a future ML model reports its own name/version
 * and remains offline-capable.
 */
public class ModelMetaDTO {

    private String name;
    private String version;
    private String family;
    private boolean offline;
    private String description;

    public ModelMetaDTO() {}

    public ModelMetaDTO(String name, String version, String family, boolean offline, String description) {
        this.name = name;
        this.version = version;
        this.family = family;
        this.offline = offline;
        this.description = description;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getFamily() { return family; }
    public void setFamily(String family) { this.family = family; }
    public boolean isOffline() { return offline; }
    public void setOffline(boolean offline) { this.offline = offline; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
