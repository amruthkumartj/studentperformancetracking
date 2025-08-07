package com.portal.datatransfer_access;

/**
 * A simple DTO to hold the aggregated attendance data for calculating
 * the overall percentage across all courses.
 */
public class OverallAttendanceDTO {
    private long totalPresent;
    private long totalConducted;

    // Constructor, Getters, and a calculation method
    public OverallAttendanceDTO(long totalPresent, long totalConducted) {
        this.totalPresent = totalPresent;
        this.totalConducted = totalConducted;
    }

    public long getTotalPresent() { return totalPresent; }
    public long getTotalConducted() { return totalConducted; }

    public double getOverallPercentage() {
        if (totalConducted == 0) {
            return 0.0;
        }
        return ((double) totalPresent / totalConducted) * 100.0;
    }
}