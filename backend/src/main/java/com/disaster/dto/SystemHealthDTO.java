package com.disaster.dto;

/**
 * @deprecated The System Health / Runtime Health monitoring feature has been
 * disabled from the application. This DTO is no longer produced by any
 * controller endpoint and is retained only for reference / backwards
 * compatibility. Scheduled for removal in a future release.
 */
@Deprecated
public class SystemHealthDTO {
    private String overallStatus;
    private String uptime;
    private String startedAt;
    private String timestamp;

    private String databaseStatus;
    private String databaseDetail;

    private String serverHost;
    private int serverPort;
    private String serverStatus;

    private long jvmUsedMemoryMb;
    private long jvmMaxMemoryMb;
    private long jvmFreeMemoryMb;
    private int cpuCores;
    private double cpuLoad;
    private String javaVersion;
    private String jvmStatus;

    private String apiStatus;
    private int apiEndpointCount;

    private int wsActiveConnections;
    private int wsHeartbeatIntervalSeconds;
    private String wsStatus;

    private long diskTotalMb;
    private long diskFreeMb;
    private long diskUsableMb;

    public SystemHealthDTO() {}

    public String getOverallStatus() { return overallStatus; }
    public void setOverallStatus(String overallStatus) { this.overallStatus = overallStatus; }
    public String getUptime() { return uptime; }
    public void setUptime(String uptime) { this.uptime = uptime; }
    public String getStartedAt() { return startedAt; }
    public void setStartedAt(String startedAt) { this.startedAt = startedAt; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getDatabaseStatus() { return databaseStatus; }
    public void setDatabaseStatus(String databaseStatus) { this.databaseStatus = databaseStatus; }
    public String getDatabaseDetail() { return databaseDetail; }
    public void setDatabaseDetail(String databaseDetail) { this.databaseDetail = databaseDetail; }

    public String getServerHost() { return serverHost; }
    public void setServerHost(String serverHost) { this.serverHost = serverHost; }
    public int getServerPort() { return serverPort; }
    public void setServerPort(int serverPort) { this.serverPort = serverPort; }
    public String getServerStatus() { return serverStatus; }
    public void setServerStatus(String serverStatus) { this.serverStatus = serverStatus; }

    public long getJvmUsedMemoryMb() { return jvmUsedMemoryMb; }
    public void setJvmUsedMemoryMb(long jvmUsedMemoryMb) { this.jvmUsedMemoryMb = jvmUsedMemoryMb; }
    public long getJvmMaxMemoryMb() { return jvmMaxMemoryMb; }
    public void setJvmMaxMemoryMb(long jvmMaxMemoryMb) { this.jvmMaxMemoryMb = jvmMaxMemoryMb; }
    public long getJvmFreeMemoryMb() { return jvmFreeMemoryMb; }
    public void setJvmFreeMemoryMb(long jvmFreeMemoryMb) { this.jvmFreeMemoryMb = jvmFreeMemoryMb; }
    public int getCpuCores() { return cpuCores; }
    public void setCpuCores(int cpuCores) { this.cpuCores = cpuCores; }
    public double getCpuLoad() { return cpuLoad; }
    public void setCpuLoad(double cpuLoad) { this.cpuLoad = cpuLoad; }
    public String getJavaVersion() { return javaVersion; }
    public void setJavaVersion(String javaVersion) { this.javaVersion = javaVersion; }
    public String getJvmStatus() { return jvmStatus; }
    public void setJvmStatus(String jvmStatus) { this.jvmStatus = jvmStatus; }

    public String getApiStatus() { return apiStatus; }
    public void setApiStatus(String apiStatus) { this.apiStatus = apiStatus; }
    public int getApiEndpointCount() { return apiEndpointCount; }
    public void setApiEndpointCount(int apiEndpointCount) { this.apiEndpointCount = apiEndpointCount; }

    public int getWsActiveConnections() { return wsActiveConnections; }
    public void setWsActiveConnections(int wsActiveConnections) { this.wsActiveConnections = wsActiveConnections; }
    public int getWsHeartbeatIntervalSeconds() { return wsHeartbeatIntervalSeconds; }
    public void setWsHeartbeatIntervalSeconds(int wsHeartbeatIntervalSeconds) { this.wsHeartbeatIntervalSeconds = wsHeartbeatIntervalSeconds; }
    public String getWsStatus() { return wsStatus; }
    public void setWsStatus(String wsStatus) { this.wsStatus = wsStatus; }

    public long getDiskTotalMb() { return diskTotalMb; }
    public void setDiskTotalMb(long diskTotalMb) { this.diskTotalMb = diskTotalMb; }
    public long getDiskFreeMb() { return diskFreeMb; }
    public void setDiskFreeMb(long diskFreeMb) { this.diskFreeMb = diskFreeMb; }
    public long getDiskUsableMb() { return diskUsableMb; }
    public void setDiskUsableMb(long diskUsableMb) { this.diskUsableMb = diskUsableMb; }
}
