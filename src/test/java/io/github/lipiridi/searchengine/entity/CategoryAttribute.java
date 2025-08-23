package io.github.lipiridi.searchengine.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.hibernate.proxy.HibernateProxy;

@Getter
@Setter
@ToString
@Entity
@IdClass(CategoryAttribute.EntityId.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryAttribute {

    @Id
    @ManyToOne
    Attribute attribute;

    @Id
    @ManyToOne
    Category category;

    @Column(nullable = false)
    Boolean mandatory = false;

    @Column(nullable = false)
    Boolean useInFilters = false;

    @Column(nullable = false)
    Integer sortOrder = 0;

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class EntityId implements Serializable {
        Attribute attribute;
        Category category;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy
                ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        CategoryAttribute that = (CategoryAttribute) o;
        return getCategory() != null
                && Objects.equals(getCategory(), that.getCategory())
                && getAttribute() != null
                && Objects.equals(getAttribute(), that.getAttribute());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getCategory(), getAttribute());
    }
}
