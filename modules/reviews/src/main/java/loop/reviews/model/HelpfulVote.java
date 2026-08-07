package loop.reviews.model;

/**
 * Records a single customer vote on a review (maps to "helpful_votes").
 * A UNIQUE(review_id, customer_id) constraint stores one current choice per
 * customer and review. The choice can be removed or switched (FR7).
 */
public class HelpfulVote {
    public static final String HELPFUL = "helpful";
    public static final String UNHELPFUL = "unhelpful";

    private int id;
    private int reviewId;
    private int customerId;
    private String voteType; // helpful / unhelpful
    private long createdAt;

    public HelpfulVote() { }

    public HelpfulVote(int reviewId, int customerId, String voteType) {
        this.reviewId = reviewId;
        this.customerId = customerId;
        this.voteType = voteType;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getReviewId() { return reviewId; }
    public void setReviewId(int reviewId) { this.reviewId = reviewId; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public String getVoteType() { return voteType; }
    public void setVoteType(String voteType) { this.voteType = voteType; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
