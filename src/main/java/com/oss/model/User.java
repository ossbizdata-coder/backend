package com.oss.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;   // THIS is now the login identifier

    private String password; // hashed
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private Double hourlyRate = 125.0;

    @Column(name = "daily_salary")
    @Builder.Default
    private Double dailySalary = 0.0;

    @Column(name = "deduction_rate_per_hour")
    @Builder.Default
    private Double deductionRatePerHour = 0.0;
}
