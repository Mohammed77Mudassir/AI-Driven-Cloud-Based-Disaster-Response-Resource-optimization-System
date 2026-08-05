package com.disaster.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Daily duty-roster summary for rescue teams.
 *
 * <p>totalMembers is the number of distinct team members referenced by the day's
 * shifts (0 when the roster is empty). onDuty is the number of shifts with an
 * ACTIVE status for the day and available is derived as totalMembers - onDuty.</p>
 */
public class DutyRosterSummaryDTO {
    private LocalDate date;
    private int totalMembers;
    private int onDuty;
    private int available;
    private List<TeamShiftDTO> shifts;

    public DutyRosterSummaryDTO() {}

    public static DutyRosterSummaryDTO of(LocalDate date, int totalMembers, int onDuty, List<TeamShiftDTO> shifts) {
        DutyRosterSummaryDTO dto = new DutyRosterSummaryDTO();
        dto.setDate(date);
        dto.setTotalMembers(totalMembers);
        dto.setOnDuty(onDuty);
        dto.setAvailable(totalMembers - onDuty);
        dto.setShifts(shifts);
        return dto;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public int getTotalMembers() { return totalMembers; }
    public void setTotalMembers(int totalMembers) { this.totalMembers = totalMembers; }
    public int getOnDuty() { return onDuty; }
    public void setOnDuty(int onDuty) { this.onDuty = onDuty; }
    public int getAvailable() { return available; }
    public void setAvailable(int available) { this.available = available; }
    public List<TeamShiftDTO> getShifts() { return shifts; }
    public void setShifts(List<TeamShiftDTO> shifts) { this.shifts = shifts; }
}
