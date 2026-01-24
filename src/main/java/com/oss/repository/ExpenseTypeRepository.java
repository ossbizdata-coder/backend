package com.oss.repository;
import com.oss.model.ExpenseType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ExpenseTypeRepository extends JpaRepository<ExpenseType, Long> {
    List<ExpenseType> findByShopType(String shopType);
}