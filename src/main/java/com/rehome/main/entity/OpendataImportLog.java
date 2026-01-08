package com.rehome.main.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "opendata_import_log")
@Data
public class OpendataImportLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate importDate;
    private LocalDateTime executionTime;
    private String status;
    private Integer totalCount;
    private Integer successCount;
    private String backupFilePath;
    private String errorMessage;
}
