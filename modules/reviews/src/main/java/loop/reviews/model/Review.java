package loop.reviews.model;

import java.time.Duration;
import java.time.Instant;

/**
 * Central entity of the component (maps to "reviews"). The configurable
 * edit/delete window (Assessment A "EditTimeWindow") is folded in here as
 * createdAt + editDurationSeconds, exposing isEditWindowOpen()/remainingSeconds().
 */
public class Review {
    public static final String ACTIVE = "Active";
    public static final String FLAGGED = "Flagged";
    public static final String REMOVED = "Removed";

    private int id;
    private int productId;
    private int customerId;
    private int rating;              // 1..5
    private String commentText;
    private String imageUrl;         // optional
    private long createdAt;          // epoch millis
    private String status;           // Active / Flagged / Removed
    private int helpfulCount;
    private int unhelpfulCount;
    private int editDurationSeconds; // default 300 (admin-adjustable)
    private int flagCount;

    // Convenience field populated by joins for display (not a DB column).
    private String customerName;

    public Review() { }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getHelpfulCount() { return helpfulCount; }
    public void setHelpfulCount(int helpfulCount) { this.helpfulCount = helpfulCount; }
    public int getUnhelpfulCount() { return unhelpfulCount; }
    public void setUnhelpfulCount(int unhelpfulCount) { this.unhelpfulCount = unhelpfulCount; }
    public int getEditDurationSeconds() { return editDurationSeconds; }
    public void setEditDurationSeconds(int s) { this.editDurationSeconds = s; }
    public int getFlagCount() { return flagCount; }
    public void setFlagCount(int flagCount) { this.flagCount = flagCount; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    /** Helpfulness score used for default ordering (FR6/FR7). */
    public int getHelpfulnessScore() { return helpfulCount - unhelpfulCount; }

    /** True while the edit/delete time window is still open (FR4/FR5). */
    public boolean isEditWindowOpen() {
        return remainingSeconds() > 0;
    }

    /** Whole seconds left in the edit/delete window; 0 once elapsed. */
    public long remainingSeconds() {
        long elapsed = Duration.between(Instant.ofEpochMilli(createdAt), Instant.now()).getSeconds();
        long remaining = editDurationSeconds - elapsed;
        return Math.max(0, remaining);
    }
}
