package com.deva.fraudwatch.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.deva.fraudwatch.entity.Rule;

public interface RuleRepository extends JpaRepository<Rule, Long> {

    @Query("SELECT r FROM Rule r WHERE r.deleted = false OR r.deleted IS NULL")
    List<Rule> findByDeletedFalse();

    @Query("SELECT r FROM Rule r WHERE r.active = true AND (r.deleted = false OR r.deleted IS NULL)")
    List<Rule> findByActiveTrueAndDeletedFalse();

    List<Rule> findByActiveTrue();
}
