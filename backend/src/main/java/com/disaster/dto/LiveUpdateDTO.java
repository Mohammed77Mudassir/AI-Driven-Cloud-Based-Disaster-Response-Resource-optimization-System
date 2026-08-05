package com.disaster.dto;

public class LiveUpdateDTO {
    private String type;
    private Object data;
    private String timestamp;

    public LiveUpdateDTO() {}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
