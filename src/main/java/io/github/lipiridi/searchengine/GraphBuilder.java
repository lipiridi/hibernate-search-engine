package io.github.lipiridi.searchengine;

import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;
import jakarta.persistence.criteria.FetchParent;
import jakarta.persistence.criteria.JoinType;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.annotations.FetchMode;
import org.hibernate.query.sqm.tree.domain.AbstractSqmAttributeJoin;

public class GraphBuilder {

    private final Map<String, AbstractSqmAttributeJoin<?, ?>> fetchedAttributes = new HashMap<>();

    public Map<String, AbstractSqmAttributeJoin<?, ?>> getFetchedAttributes() {
        return Collections.unmodifiableMap(fetchedAttributes);
    }

    public void addEagerJoins(FetchParent<?, ?> root, Class<?> entityClass, @Nullable String parentField) {
        for (Field field : entityClass.getDeclaredFields()) {
            if (shouldSkipJoin(field)) {
                continue;
            }

            AbstractSqmAttributeJoin<?, ?> fetch =
                    (AbstractSqmAttributeJoin<?, ?>) root.fetch(field.getName(), JoinType.LEFT);
            String newParentField = parentField == null ? field.getName() : parentField + "." + field.getName();
            fetchedAttributes.put(newParentField, fetch);

            Class<?> fieldType = field.getType();
            if (fieldType.isAnnotationPresent(Entity.class)) {
                addEagerJoins(fetch, fieldType, newParentField);
            }
        }

        // Handle inheritance by looking at superclasses
        Class<?> superclass = entityClass.getSuperclass();
        if (superclass != null && superclass.isAnnotationPresent(MappedSuperclass.class)) {
            addEagerJoins(root, superclass, parentField);
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
