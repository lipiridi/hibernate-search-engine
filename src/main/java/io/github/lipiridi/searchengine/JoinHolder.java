package io.github.lipiridi.searchengine;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.query.sqm.tree.domain.AbstractSqmAttributeJoin;

public class JoinHolder {

    private final Map<String, AbstractSqmAttributeJoin<?, ?>> builtJoins = new HashMap<>();

    public JoinHolder() {}

    public JoinHolder(Map<String, AbstractSqmAttributeJoin<?, ?>> joinedPaths) {
        builtJoins.putAll(joinedPaths);
    }

    @SuppressWarnings("unchecked")
    public <Y> Path<Y> getPath(Root<?> root, SearchField searchField) {
        String[] fields = searchField.path().split("\\.");
        String firstField = fields[0];
        int length = fields.length;

        if (length == 1 && !searchField.elementCollection()) {
            return root.get(firstField);
        }

        AbstractSqmAttributeJoin<?, ?> rootJoin = builtJoins.get(firstField);
        if (rootJoin == null) {
            rootJoin = (AbstractSqmAttributeJoin<?, ?>) root.join(firstField, JoinType.LEFT);
            builtJoins.put(firstField, rootJoin);
        }

        String currentPath = firstField;
        for (int i = 1; i < length; i++) {
            currentPath = currentPath + "." + fields[i];
            AbstractSqmAttributeJoin<?, ?> cachedJoin = builtJoins.get(currentPath);
            if (cachedJoin == null) {
                cachedJoin = (AbstractSqmAttributeJoin<?, ?>) rootJoin.join(fields[i], JoinType.LEFT);
                builtJoins.put(currentPath, cachedJoin);
            }
            rootJoin = cachedJoin;
        }

        return searchField.elementCollection() ? (Path<Y>) rootJoin : (Path<Y>) rootJoin.get(fields[length - 1]);
    }
}
