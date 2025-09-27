package io.github.lipiridi.searchengine.entity;

import io.github.lipiridi.searchengine.Searchable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;
import org.hibernate.proxy.HibernateProxy;

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
@FieldNameConstants
@Entity
public class SimpleEntity {

    @Searchable
    @Id
    UUID id;

    @Searchable
    String stringValue;

    @Searchable
    Boolean booleanValue;

    @Searchable
    Byte byteValue;

    @Searchable
    Short shortValue;

    @Searchable
    Integer intValue;

    @Searchable
    Long longValue;

    @Searchable
    Double doubleValue;

    @Searchable
    Float floatValue;

    @Searchable
    BigDecimal bigDecimalValue;

    @Searchable
    Instant instantValue;

    @Searchable
    LocalDate localDateValue;

    @Searchable
    LocalDateTime localDateTimeValue;

    @Searchable
    ZonedDateTime zonedDateTimeValue;

    @Searchable
    OffsetDateTime offsetDateTimeValue;

    @Searchable
    Currency currency;

    @Searchable
    @Enumerated
    Operation operation;

    @Searchable
    @Enumerated(EnumType.STRING)
    Operation operationAsString;

    @Searchable
    String nullStringValue;

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
        SimpleEntity that = (SimpleEntity) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
                ? ((HibernateProxy) this)
                        .getHibernateLazyInitializer()
                        .getPersistentClass()
                        .hashCode()
                : getClass().hashCode();
    }
}
