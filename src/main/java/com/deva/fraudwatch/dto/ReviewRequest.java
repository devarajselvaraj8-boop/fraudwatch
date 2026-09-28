package com.deva.fraudwatch.dto;

import jakarta.validation.constraints.NotBlank;

public class ReviewRequest {

    @NotBlank(message = "Reviewer cannot be blank")
    private String reviewer;

    private String comment;

    public ReviewRequest() {
    }

    public ReviewRequest(String reviewer, String comment) {
        this.reviewer = reviewer;
        this.comment = comment;
    }

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
