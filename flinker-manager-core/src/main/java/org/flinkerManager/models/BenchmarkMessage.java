package org.flinkerManager.models;

import com.fasterxml.jackson.annotation.JsonProperty;


public class BenchmarkMessage {
    @JsonProperty("id")
    private long id;

    @JsonProperty("type")
    private String type;

    @JsonProperty("message")
    private String message;

    @JsonProperty("datetime")
    private String datetime;

    @JsonProperty("process_id")
    private long processId;

    @JsonProperty("client_id")
    private long clientId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDatetime() {
        return datetime;
    }

    public void String(String datetime) {
        this.datetime = datetime;
    }

    public long getProcessId() {
        return processId;
    }

    public void setProcessId(long processId) {
        this.processId = processId;
    }

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    @Override
    public String toString() {
        return "BenchmarkMessage{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", message='" + message + '\'' +
                ", datetime=" + datetime +
                ", processId=" + processId +
                ", clientId=" + clientId +
                '}';
    }
}
