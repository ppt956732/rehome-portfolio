package com.rehome.main.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rehome.main.entity.OpendataImportLog;

public interface OpendataImportLogRepository extends JpaRepository<OpendataImportLog, Long> {
    boolean existsByImportDate(LocalDate imoporDate);
}
