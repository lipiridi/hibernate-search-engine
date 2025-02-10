package io.github.lipiridi.searchengine;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.FetchParent;
import jakarta.persistence.criteria.JoinType;
import java.lang.reflect.Field;
import org.hibernate.annotations.FetchMode;

public class GraphBuilder {

    public void addEagerJoins(FetchParent<?, ?> root, Class<?> entityClass) {
        for (Field field : entityClass.getDeclaredFields()) {
            if (shouldSkipJoin(field)) {
                continue;
            }

            Fetch<?, ?> fetch = root.fetch(field.getName(), JoinType.LEFT);

            Class<?> fieldType = field.getType();
            if (fieldType.isAnnotationPresent(Entity.class)) {
                addEagerJoins(fetch, fieldType);
            }
        }

        // Handle inheritance by looking at superclasses
        Class<?> superclass = entityClass.getSuperclass();
        if (superclass != null && superclass.isAnnotationPresent(MappedSuperclass.class)) {
            addEagerJoins(root, superclass);
        }
    }

    private boolean shouldSkipJoin(Field field) {
        if (FetchType.LAZY == resolveFetchType(field)) {
            return true;
        }

        org.hibernate.annotations.Fetch fetchAnnotation = field.getAnnotation(org.hibernate.annotations.Fetch.class);
        return fetchAnnotation != null && fetchAnnotation.value() != FetchMode.JOIN;
    }

    private FetchType resolveFetchType(Field field) {
        if (field.isAnnotationPresent(ManyToOne.class)) {
            return field.getAnnotation(ManyToOne.class).fetch();
        } else if (field.isAnnotationPresent(OneToOne.class)) {
            return field.getAnnotation(OneToOne.class).fetch();
        } else {
            return FetchType.LAZY;
        }
    }
}
