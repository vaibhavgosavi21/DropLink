package com.example.fileshare.dto;

import com.example.fileshare.entity.FileRecord;
import org.springframework.core.io.Resource;

public record DownloadResult(FileRecord record, Resource resource) {}
