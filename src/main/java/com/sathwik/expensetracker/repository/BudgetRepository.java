package com.sathwik.expensetracker.repository;

import com.sathwik.expensetracker.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserId(Long userId);

    List<Budget> findByUserIdAndPeriod(Long userId, String period);

    Optional<Budget> findByUserIdAndCategoryIgnoreCaseAndPeriod(Long userId, String category, String period);

    boolean existsByUserIdAndCategoryIgnoreCaseAndPeriod(Long userId, String category, String period);

    boolean existsByUserIdAndCategoryIgnoreCaseAndPeriodAndIdNot(Long userId, String category, String period, Long id);

}
