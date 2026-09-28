package com.deva.fraudwatch.dto;

public class DashboardResponse {

    private long totalTransactions;
    private long completedTransactions;
    private long flaggedTransactions;
    private long blockedTransactions;
    private long pendingReviews;
    private long approvedReviews;
    private long blockedReviews;

    public DashboardResponse() {
    }

    public DashboardResponse(
            long totalTransactions,
            long completedTransactions,
            long flaggedTransactions,
            long blockedTransactions,
            long pendingReviews,
            long approvedReviews,
            long blockedReviews) {
        this.totalTransactions = totalTransactions;
        this.completedTransactions = completedTransactions;
        this.flaggedTransactions = flaggedTransactions;
        this.blockedTransactions = blockedTransactions;
        this.pendingReviews = pendingReviews;
        this.approvedReviews = approvedReviews;
        this.blockedReviews = blockedReviews;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public long getCompletedTransactions() {
        return completedTransactions;
    }

    public void setCompletedTransactions(long completedTransactions) {
        this.completedTransactions = completedTransactions;
    }

    public long getFlaggedTransactions() {
        return flaggedTransactions;
    }

    public void setFlaggedTransactions(long flaggedTransactions) {
        this.flaggedTransactions = flaggedTransactions;
    }

    public long getBlockedTransactions() {
        return blockedTransactions;
    }

    public void setBlockedTransactions(long blockedTransactions) {
        this.blockedTransactions = blockedTransactions;
    }

    public long getPendingReviews() {
        return pendingReviews;
    }

    public void setPendingReviews(long pendingReviews) {
        this.pendingReviews = pendingReviews;
    }

    public long getApprovedReviews() {
        return approvedReviews;
    }

    public void setApprovedReviews(long approvedReviews) {
        this.approvedReviews = approvedReviews;
    }

    public long getBlockedReviews() {
        return blockedReviews;
    }

    public void setBlockedReviews(long blockedReviews) {
        this.blockedReviews = blockedReviews;
    }
}
