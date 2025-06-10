package com.generador.Generador.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "project_name", nullable = false)
    private String projectName;

    @Column(name = "description")
    private String description;

    @Column(name = "json_schema", nullable = false)
    private String jsonSchema;

    @Column(name = "created_at", nullable = false)
    private String createdAt;

    @Column(name = "created_by", nullable = true) // de momento nulleable == true por que no tenemos como saber quien lo ha creado
    private String createdBy;
}
