package com.oss.repository;

import com.oss.model.IdeaOfTheWeek;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdeaOfTheWeekRepository extends JpaRepository<IdeaOfTheWeek, Long> {}

