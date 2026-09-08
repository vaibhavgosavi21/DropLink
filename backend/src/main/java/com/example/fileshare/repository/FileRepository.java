package com.example.fileshare.repository;

import com.example.fileshare.entity.FileRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileRepository extends JpaRepository<FileRecord, UUID> {
    Optional<FileRecord> findByShareId(String shareId);
    List<FileRecord> findAllByIsActiveAndExpiresAtBefore(boolean isActive, Instant now);
}
