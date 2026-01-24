package com.oss.repository;
import com.oss.model.FoodhutSale;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface FoodhutSaleRepository extends JpaRepository<FoodhutSale, Long> {
    @Query("""
        SELECT s FROM FoodhutSale s
        WHERE s.transactionTime BETWEEN :start AND :end
    """)
    List<FoodhutSale> findAllForDay(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
                                   );
}