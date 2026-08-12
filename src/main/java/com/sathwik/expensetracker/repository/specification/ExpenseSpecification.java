package com.sathwik.expensetracker.repository.specification;

import com.sathwik.expensetracker.dto.ExpenseSearchRequest;
import com.sathwik.expensetracker.entity.Expense;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ExpenseSpecification {

    public static Specification<Expense> buildSpecification(Long userId, ExpenseSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always scope to the specified userId
            predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));

            if (request != null) {
                if (request.getStartDate() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), request.getStartDate()));
                }

                if (request.getEndDate() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("date"), request.getEndDate()));
                }

                if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
                    predicates.add(criteriaBuilder.equal(
                            criteriaBuilder.lower(root.get("category")),
                            request.getCategory().trim().toLowerCase()
                    ));
                }

                if (request.getPaymentMethod() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("paymentMethod"), request.getPaymentMethod()));
                }

                if (request.getType() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("type"), request.getType()));
                }

                if (request.getMinAmount() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), request.getMinAmount()));
                }

                if (request.getMaxAmount() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("amount"), request.getMaxAmount()));
                }

                if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
                    String searchTerm = "%" + request.getKeyword().trim().toLowerCase() + "%";
                    Predicate descriptionMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchTerm);
                    Predicate categoryMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("category")), searchTerm);
                    predicates.add(criteriaBuilder.or(descriptionMatch, categoryMatch));
                }
            }

            // Sort by date descending
            query.orderBy(criteriaBuilder.desc(root.get("date")), criteriaBuilder.desc(root.get("id")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
