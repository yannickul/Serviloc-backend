// infrastructure/persistence/LitigeMotifJpaEntity.java
package com.serviloc.litiges.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "litige_motifs")
public class LitigeMotifJpaEntity {

    @Id
    private String id;
    private String title;
    private String description;

    public LitigeMotifJpaEntity() {}

    public String getId()          { return id; }
    public String getTitle()       { return title; }
    public String getDescription() { return description; }

    public void setId(String id)                   { this.id = id; }
    public void setTitle(String title)             { this.title = title; }
    public void setDescription(String description) { this.description = description; }
}