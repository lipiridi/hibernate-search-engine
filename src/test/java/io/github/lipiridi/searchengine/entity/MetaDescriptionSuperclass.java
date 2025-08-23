package io.github.lipiridi.searchengine.entity;

import io.github.lipiridi.searchengine.Searchable;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@ToString
@MappedSuperclass
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class MetaDescriptionSuperclass implements HasLocale {

    @Searchable
    @Id
    Integer languageId;

    @Searchable
    String title;

    @Lob
    String description;

    String metaTitle;
    String metaDescription;

    @Override
    public String getName() {
        return title;
    }
}
