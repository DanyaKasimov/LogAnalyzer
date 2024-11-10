package backend.academy.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogStatistics {
    private int totalRequests;

    private Map<String, Long> resources;

    private Map<Integer, Long> statuses;

    private Map<String, Long> ipAddresses;

    private Map<String, Long> countRequestsPerDay;

    private double avgResponseSize;

    private double responseSizePercentile95;

    private List<String> filesNames;

    public LogStatistics() {
        this.resources = new HashMap<>();
        this.statuses = new HashMap<>();
        this.ipAddresses = new HashMap<>();
        this.countRequestsPerDay = new HashMap<>();
        this.filesNames = new ArrayList<>();
    }
}
