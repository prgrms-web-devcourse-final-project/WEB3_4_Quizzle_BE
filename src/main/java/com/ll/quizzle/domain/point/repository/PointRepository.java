package com.ll.quizzle.domain.point.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.point.entity.Point;
import com.ll.quizzle.domain.point.type.PointType;

public interface PointRepository extends JpaRepository<Point, Long> {

	Page<Point> findPageByMemberOrderByCreateDateDesc(Member member, Pageable pageable);

	Page<Point> findPageByMemberAndTypeOrderByCreateDateDesc(Member member, PointType type, Pageable pageable);
}
