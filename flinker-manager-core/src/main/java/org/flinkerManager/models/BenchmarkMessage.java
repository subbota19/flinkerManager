package org.flinkerManager.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.flink.table.data.TimestampData;


public class BenchmarkMessage {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("type")
    private String type;

    @JsonProperty("message")
    private String message;

    @JsonProperty("rawDatetime")
    private String rawDatetime;

    @JsonProperty("process_id")
    private Long processId;

    @JsonProperty("client_id")
    private Long clientId;

    private TimestampData processedDatetime;

    public Long getId() {
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

    public String getRawDatetime() {
        return rawDatetime;
    }

    public void setRawDatetime(String rawDatetime) {
        this.rawDatetime = rawDatetime;
    }

    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(long processId) {
        this.processId = processId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public TimestampData getProcessedDatetime() {
        return processedDatetime;
    }

    public void setProcessedDatetime(TimestampData processedDatetime) {
        this.processedDatetime = processedDatetime;
    }

    @Override
    public String toString() {
        return "BenchmarkMessage{" + "id=" + id + ", type='" + type + '\'' + ", message='" + message + '\'' + ", rawDatetime=" + rawDatetime + ", processId=" + processId + ", clientId=" + clientId + ", processedDatetime=" + processedDatetime + '}';
    }

    public Boolean isEmpty() {
        return (id == null && type == null && message == null && rawDatetime == null && processId == null && clientId == null && processedDatetime == null);
    }
}
