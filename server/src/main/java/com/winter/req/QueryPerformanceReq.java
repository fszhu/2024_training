package com.winter.req;

public class QueryPerformanceReq {
    private String endpoint;
    private String metric;
    private Integer start_ts;
    private Integer end_ts;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public Integer getStart_ts() {
        return start_ts;
    }

    public void setStart_ts(Integer start_ts) {
        this.start_ts = start_ts;
    }

    public Integer getEnd_ts() {
        return end_ts;
    }

    public void setEnd_ts(Integer end_ts) {
        this.end_ts = end_ts;
    }
}
