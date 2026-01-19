package com.revoktek.services.specification;

import com.revoktek.services.model.User;
import com.revoktek.services.model.enums.Authority;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> hasAuthority(Authority authority) {
        return (root, query, cb) -> {
            if (authority == null) {
                return null; // filtro opcional
            }

            // Join directo al ElementCollection
            Join<User, Authority> join = root.join("authorities");

            // IMPORTANTE: el join ES el enum
            return cb.equal(join, authority);
        };
    }


    public static Specification<User> usernameLike(String username) {
        return (root, query, criteriaBuilder) -> {
            if (username == null || username.isEmpty()) return null;
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("username")),
                    "%" + username.toLowerCase() + "%");
        };
    }

    public static Specification<User> namesLike(String names) {
        return (root, query, criteriaBuilder) -> {
            if (names == null || names.isEmpty()) return null;
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("names")),
                    "%" + names.toLowerCase() + "%");
        };
    }

    public static Specification<User> paternalSurnameLike(String paternalSurname) {
        return (root, query, criteriaBuilder) -> {
            if (paternalSurname == null || paternalSurname.isEmpty()) return null;
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("paternalSurname")),
                    "%" + paternalSurname.toLowerCase() + "%");
        };
    }

    public static Specification<User> maternalSurnameLike(String maternalSurname) {
        return (root, query, criteriaBuilder) -> {
            if (maternalSurname == null || maternalSurname.isEmpty()) return null;
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("maternalSurname")),
                    "%" + maternalSurname.toLowerCase() + "%");
        };
    }

    public static Specification<User> residencyCityLike(String residencyCity) {
        return (root, query, criteriaBuilder) -> {
            if (residencyCity == null || residencyCity.isEmpty()) return null;
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("residencyCity")),
                    "%" + residencyCity.toLowerCase() + "%");
        };
    }

    public static Specification<User> hasAge(Integer age) {
        return (root, query, criteriaBuilder) -> {
            if (age == null) return null;
            return criteriaBuilder.equal(root.get("age"), age);
        };
    }

    public static Specification<User> isEnabled(Boolean enabled) {
        return (root, query, criteriaBuilder) -> {
            if (enabled == null) return null;
            return criteriaBuilder.equal(root.get("enabled"), enabled);
        };
    }
}

