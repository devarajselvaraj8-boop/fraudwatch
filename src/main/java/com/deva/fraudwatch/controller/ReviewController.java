package com.deva.fraudwatch.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deva.fraudwatch.dto.ReviewRequest;
import com.deva.fraudwatch.dto.ReviewResponse;
import com.deva.fraudwatch.service.ReviewService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public List<ReviewResponse> getAllReviews(@org.springframework.web.bind.annotation.RequestParam(required = false) com.deva.fraudwatch.enums.ReviewStatus status) {
        return reviewService.getAllReviews(status);
    }

    @GetMapping("/pending")
    public List<ReviewResponse> getPendingReviews() {
        return reviewService.getPendingReviews();
    }

    @GetMapping("/{id}")
    public ReviewResponse getReviewById(@PathVariable Long id) {
        return reviewService.getReviewById(id);
    }

    @PostMapping("/{id}/approve")
    public ReviewResponse approveReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request) {
        return reviewService.approveReview(id, request);
    }

    @PostMapping("/{id}/block")
    public ReviewResponse blockReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request) {
        return reviewService.blockReview(id, request);
    }
}
