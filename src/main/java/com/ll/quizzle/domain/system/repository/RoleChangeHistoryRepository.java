package com.ll.quizzle.domain.system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ll.quizzle.domain.system.entity.RoleChangeHistory;

public interface RoleChangeHistoryRepository extends JpaRepository<RoleChangeHistory, Long> {
}
