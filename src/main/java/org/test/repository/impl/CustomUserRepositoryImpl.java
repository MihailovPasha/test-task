package org.test.repository.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.test.dto.request.UserSearchRequest;
import org.test.model.EmailData;
import org.test.model.PhoneData;
import org.test.model.User;
import org.test.repository.CustomUserRepository;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CustomUserRepositoryImpl implements CustomUserRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<User> searchUsers(UserSearchRequest request, Pageable pageable) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        CriteriaQuery<User> query = builder.createQuery(User.class);
        Root<User> user = query.from(User.class);
        List<Predicate> predicates = buildPredicates(request, builder, user);

        query.where(predicates.toArray(new Predicate[0]));
        query.distinct(true);

        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            for (Sort.Order order : pageable.getSort()) {
                if (order.isAscending()) {
                    orders.add(builder.asc(user.get(order.getProperty())));
                } else {
                    orders.add(builder.desc(user.get(order.getProperty())));
                }
            }
            query.orderBy(orders);
        }

        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<User> countRoot = countQuery.from(User.class);
        List<Predicate> countPredicates = buildPredicates(request, builder, countRoot);
        countQuery.select(builder.countDistinct(countRoot));
        countQuery.where(countPredicates.toArray(new Predicate[0]));

        Long total = entityManager.createQuery(countQuery).getSingleResult();

        TypedQuery<User> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<User> users = typedQuery.getResultList();

        return new PageImpl<>(users, pageable, total);
    }

    private List<Predicate> buildPredicates(UserSearchRequest request,
                                            CriteriaBuilder cb,
                                            Root<User> user) {
        List<Predicate> predicates = new ArrayList<>();

        if (request.getDateOfBirth() != null) {
            predicates.add(cb.greaterThan(user.get("dateOfBirth"),
                    request.getDateOfBirth()));
        }

        if (request.getName() != null && !request.getName().isEmpty()) {
            predicates.add(cb.like(cb.lower(user.get("name")),
                    request.getName().toLowerCase() + "%"));
        }

        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            Join<User, PhoneData> phones = user.join("phones", JoinType.LEFT);
            predicates.add(cb.equal(phones.get("phone"), request.getPhone()));
        }

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            Join<User, EmailData> emails = user.join("emails", JoinType.LEFT);
            predicates.add(cb.equal(emails.get("email"), request.getEmail()));
        }

        return predicates;
    }
}
