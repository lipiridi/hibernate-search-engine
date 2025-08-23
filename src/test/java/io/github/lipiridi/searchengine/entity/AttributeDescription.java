package io.github.lipiridi.searchengine.entity;

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
@Table(name = AttributeDescription.TABLE_NAME)
@IdClass(AttributeDescription.EntityId.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttributeDescription extends NameDescriptionSuperclass {

    public static final String TABLE_NAME = "attribute_description";

    @Id
    @ManyToOne
    Attribute attribute;

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class EntityId implements Serializable {
        Integer languageId;
        Attribute attribute;
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
        AttributeDescription that = (AttributeDescription) o;
        return getLanguageId() != null
                && Objects.equals(getLanguageId(), that.getLanguageId())
                && getAttribute() != null
                && Objects.equals(getAttribute(), that.getAttribute());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getLanguageId(), getAttribute());
    }
}
