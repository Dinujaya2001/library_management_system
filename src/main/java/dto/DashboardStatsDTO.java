package dto;

public class DashboardStatsDTO {
    private final int totalBooks;
    private final int totalMembers;
    private final int currentlyBorrowed;
    private final int overdueCount;

    public DashboardStatsDTO(int totalBooks, int totalMembers, int currentlyBorrowed, int overdueCount) {
        this.totalBooks = totalBooks;
        this.totalMembers = totalMembers;
        this.currentlyBorrowed = currentlyBorrowed;
        this.overdueCount = overdueCount;
    }

    public int getTotalBooks() { return totalBooks; }
    public int getTotalMembers() { return totalMembers; }
    public int getCurrentlyBorrowed() { return currentlyBorrowed; }
    public int getOverdueCount() { return overdueCount; }
}
