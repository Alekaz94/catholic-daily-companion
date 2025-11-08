package com.alexandros.dailycompanion.controller;

import com.alexandros.dailycompanion.dto.JournalEntryDto;
import com.alexandros.dailycompanion.dto.RosaryLogDto;
import com.alexandros.dailycompanion.dto.UserDto;
import com.alexandros.dailycompanion.service.JournalEntryService;
import com.alexandros.dailycompanion.service.RosaryLogService;
import com.alexandros.dailycompanion.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/v1/user")
public class UserExportController {

    private final UserService userService;
    private final RosaryLogService rosaryLogService;
    private final JournalEntryService journalEntryService;
    private final ObjectMapper objectMapper;

    @Autowired
    public UserExportController(UserService userService, RosaryLogService rosaryLogService, JournalEntryService journalEntryService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.rosaryLogService = rosaryLogService;
        this.journalEntryService = journalEntryService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/export-data/{userId}")
    public ResponseEntity<byte[]> exportUserData(@PathVariable UUID userId) throws AccessDeniedException, JsonProcessingException {
        // Authorization handled by userService.getUser()
        UserDto currentUser = userService.getUser(userId);

        Map<String, Object> exportData = new HashMap<>();

        exportData.put("user", userService.getUser(currentUser.id()));
        exportData.put("rosaryPrayed", rosaryLogService.getHistory(currentUser.id()));
        exportData.put("journalEntries", journalEntryService.getAllJournalEntriesForUserNotPaged(currentUser.id()));
        exportData.put("metadata", Map.of(
                "exportedAt", Instant.now(),
                "formatVersion", 1
        ));

        String json = objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(exportData);

        byte[] data = json.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"user-data-export.json\"")
                .contentType(MediaType.APPLICATION_JSON)
                .contentLength(data.length)
                .body(data);
    }

    @GetMapping("/export-data-zip/{userId}")
    public ResponseEntity<byte[]> exportUserDataAsZip(@PathVariable UUID userId) throws IOException {
        // Authorization handled by userService.getUser()
        UserDto user = userService.getUser(userId);
        List<RosaryLogDto> rosaryLogs = rosaryLogService.getHistory(userId);
        List<JournalEntryDto> journalEntries = journalEntryService.getAllJournalEntriesForUserNotPaged(userId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try(ZipOutputStream zipOutputStream = new ZipOutputStream(baos)) {
            zipOutputStream.putNextEntry(new ZipEntry("user.json"));
            String userJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(user);
            zipOutputStream.write(userJson.getBytes(StandardCharsets.UTF_8));
            zipOutputStream.closeEntry();

            zipOutputStream.putNextEntry(new ZipEntry("rosary.csv"));
            String rosaryCsv = convertRosaryToCsv(rosaryLogs);
            zipOutputStream.write(rosaryCsv.getBytes(StandardCharsets.UTF_8));
            zipOutputStream.closeEntry();

            zipOutputStream.putNextEntry(new ZipEntry("journal.csv"));
            String journalCsv = convertJournalToCsv(journalEntries);
            zipOutputStream.write(journalCsv.getBytes(StandardCharsets.UTF_8));
            zipOutputStream.closeEntry();

            zipOutputStream.putNextEntry(new ZipEntry("metadata.json"));
            String metaJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(java.util.Map.of(
                    "exportedAt", Instant.now(),
                    "formatVersion", 2)
            );
            zipOutputStream.write(metaJson.getBytes(StandardCharsets.UTF_8));
            zipOutputStream.closeEntry();
        }

        byte[] zipData = baos.toByteArray();
        System.out.println("Export size: " + zipData.length);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"user-data-export.zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(zipData.length)
                .body(zipData);
    }

    private String convertRosaryToCsv(List<RosaryLogDto> logs) {
        StringBuilder sb = new StringBuilder("date,completed\n");
        for(RosaryLogDto log : logs) {
            sb.append(log.date()).append(",").append(log.completed()).append("\n");
        }
        return sb.toString();
    }

    private String convertJournalToCsv(List<JournalEntryDto> entries) {
        StringBuilder sb = new StringBuilder("createdAt,updatedAt,title,content\n");
        for(JournalEntryDto entry : entries) {
            sb.append(entry.date()).append(",")
                    .append(entry.updatedAt()).append(",")
                    .append(escapeCsv(entry.title())).append(",")
                    .append(escapeCsv(entry.content())).append("\n");
        }
        return sb.toString();
    }

    private String escapeCsv(String text) {
        if(text == null) {
            return "";
        }

        String escaped = text.replace("\"", "\"\"");
        if(escaped.contains(",") || escaped.contains("\n")) {
            escaped = "\"" + escaped + "\"";
        }

        return escaped;
    }
}
