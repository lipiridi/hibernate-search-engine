package io.github.lipiridi.searchengine.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = ProductAttribute.TABLE_NAME)
@IdClass(ProductAttribute.EntityId.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductAttribute {

    public static final String TABLE_NAME = "product_attribute";

    @Id
    @ManyToOne
    Attribute attribute;

    @Id
    @ManyToOne
    Product product;

    @Column(nullable = false)
    String attributeValue;

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class EntityId implements Serializable {
        Attribute attribute;
        Product product;
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
        ProductAttribute that = (ProductAttribute) o;
        return getProduct() != null
                && Objects.equals(getProduct(), that.getProduct())
                && getAttribute() != null
                && Objects.equals(getAttribute(), that.getAttribute());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getProduct(), getAttribute());
    }
}
