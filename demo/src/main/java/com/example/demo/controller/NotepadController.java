package com.example.demo.controller;

import com.example.demo.dto.NotepadDto;
import com.example.demo.service.NotepadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notepads")
@CrossOrigin(origins = "*")
public class NotepadController {
    
    @Autowired
    private NotepadService notepadService;
    
    // Get all notepads
    @GetMapping
    public ResponseEntity<List<NotepadDto>> getAllNotepads() {
        List<NotepadDto> notepads = notepadService.getAllNotepads();
        return ResponseEntity.ok(notepads);
    }
    
    // Get all active notepads
    @GetMapping("/active")
    public ResponseEntity<List<NotepadDto>> getActiveNotepads() {
        List<NotepadDto> notepads = notepadService.getActiveNotepads();
        return ResponseEntity.ok(notepads);
    }
    
    // Get all archived notepads
    @GetMapping("/archived")
    public ResponseEntity<List<NotepadDto>> getArchivedNotepads() {
        List<NotepadDto> notepads = notepadService.getArchivedNotepads();
        return ResponseEntity.ok(notepads);
    }
    
    // Get notepad by ID
    @GetMapping("/{id}")
    public ResponseEntity<NotepadDto> getNotepadById(@PathVariable Long id) {
        return notepadService.getNotepadById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Create new notepad
    @PostMapping
    public ResponseEntity<NotepadDto> createNotepad(@RequestBody NotepadDto notepadDto) {
        NotepadDto createdNotepad = notepadService.createNotepad(notepadDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdNotepad);
    }
    
    // Update notepad
    @PutMapping("/{id}")
    public ResponseEntity<NotepadDto> updateNotepad(@PathVariable Long id, @RequestBody NotepadDto notepadDto) {
        return notepadService.updateNotepad(id, notepadDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Archive/Unarchive notepad
    @PatchMapping("/{id}/toggle-archive")
    public ResponseEntity<NotepadDto> toggleArchiveStatus(@PathVariable Long id) {
        return notepadService.toggleArchiveStatus(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Delete notepad
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotepad(@PathVariable Long id) {
        boolean deleted = notepadService.deleteNotepad(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Search notepads by title or content
    @GetMapping("/search")
    public ResponseEntity<List<NotepadDto>> searchNotepads(@RequestParam String q) {
        List<NotepadDto> notepads = notepadService.searchNotepads(q);
        return ResponseEntity.ok(notepads);
    }
    
    // Search notepads by title
    @GetMapping("/search/title")
    public ResponseEntity<List<NotepadDto>> searchByTitle(@RequestParam String title) {
        List<NotepadDto> notepads = notepadService.searchByTitle(title);
        return ResponseEntity.ok(notepads);
    }
    
    // Search notepads by content
    @GetMapping("/search/content")
    public ResponseEntity<List<NotepadDto>> searchByContent(@RequestParam String content) {
        List<NotepadDto> notepads = notepadService.searchByContent(content);
        return ResponseEntity.ok(notepads);
    }
    
    // Get notepads created after a specific date
    @GetMapping("/created-after")
    public ResponseEntity<List<NotepadDto>> getNotepadsCreatedAfter(@RequestParam String date) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(date);
            List<NotepadDto> notepads = notepadService.getNotepadsCreatedAfter(dateTime);
            return ResponseEntity.ok(notepads);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Get notepads updated after a specific date
    @GetMapping("/updated-after")
    public ResponseEntity<List<NotepadDto>> getNotepadsUpdatedAfter(@RequestParam String date) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(date);
            List<NotepadDto> notepads = notepadService.getNotepadsUpdatedAfter(dateTime);
            return ResponseEntity.ok(notepads);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Get statistics
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStatistics() {
        Map<String, Long> stats = Map.of(
            "total", notepadService.getTotalNotepadCount(),
            "active", notepadService.getActiveNotepadCount(),
            "archived", notepadService.getArchivedNotepadCount()
        );
        return ResponseEntity.ok(stats);
    }
    
    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = Map.of(
            "status", "UP",
            "message", "Notepad API is running"
        );
        return ResponseEntity.ok(response);
    }
} 