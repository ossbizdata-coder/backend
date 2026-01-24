package com.oss.repository;
import com.oss.model.FoodhutItem;
import org.springframework.data.jpa.repository.JpaRepository;
public interface FoodhutItemRepository extends JpaRepository<FoodhutItem, Long> {}