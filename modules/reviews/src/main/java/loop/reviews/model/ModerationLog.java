package loop.reviews.model;

/**
 * Audit trail for admin moderation actions (maps to "admin_moderation_log").
 * Every flag/edit/delete/restore is logged with adminID, reviewID, action type
 * and timestamp (FR8).
 */
public class ModerationLog {
    private int id;
    private int adminId;
    private int reviewId;
    private String action;   // FLAG / EDIT / DELETE / RESTORE
    private long createdAt;
    private String notes;

    // Convenience display fields (from joins).
    private String adminName;

    public ModerationLog() { }

    public ModerationLog(int adminId, int reviewId, String action, String notes) {
        this.adminId = adminId;
        this.reviewId = reviewId;
        this.action = action;
        this.notes = notes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }
    public int getReviewId() { return reviewId; }
    public void setReviewId(int reviewId) { this.reviewId = reviewId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getAdminName() { return adminName; }
    public void setAdminName(String adminName) { this.adminName = adminName; }
}
