package com.example.demo.service;

import com.example.demo.dto.NotepadDto;
import com.example.demo.entity.Notepad;
import com.example.demo.repository.NotepadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotepadService {
    
    @Autowired
    private NotepadRepository notepadRepository;
    
    // Convert Entity to DTO
    private NotepadDto convertToDto(Notepad notepad) {
        return new NotepadDto(
            notepad.getId(),
            notepad.getTitle(),
            notepad.getContent(),
            notepad.getCreatedAt(),
            notepad.getUpdatedAt(),
            notepad.getIsArchived()
        );
    }
    
    // Convert DTO to Entity
    private Notepad convertToEntity(NotepadDto dto) {
        Notepad notepad = new Notepad(dto.getTitle(), dto.getContent());
        if (dto.getId() != null) {
            notepad.setId(dto.getId());
        }
        if (dto.getIsArchived() != null) {
            notepad.setIsArchived(dto.getIsArchived());
        }
        return notepad;
    }
    
    // Get all notepads
    public List<NotepadDto> getAllNotepads() {
        return notepadRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // Get all active notepads (not archived)
    public List<NotepadDto> getActiveNotepads() {
        return notepadRepository.findByIsArchivedFalse().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // Get all archived notepads
    public List<NotepadDto> getArchivedNotepads() {
        return notepadRepository.findByIsArchivedTrue().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // Get notepad by ID
    public Optional<NotepadDto> getNotepadById(Long id) {
        return notepadRepository.findById(id)
                .map(this::convertToDto);
    }
    
    // Create new notepad
    public NotepadDto createNotepad(NotepadDto notepadDto) {
        Notepad notepad = convertToEntity(notepadDto);
        Notepad savedNotepad = notepadRepository.save(notepad);
        return convertToDto(savedNotepad);
    }
    
    // Update notepad
    public Optional<NotepadDto> updateNotepad(Long id, NotepadDto notepadDto) {
        return notepadRepository.findById(id)
                .map(existingNotepad -> {
                    existingNotepad.setTitle(notepadDto.getTitle());
                    existingNotepad.setContent(notepadDto.getContent());
                    if (notepadDto.getIsArchived() != null) {
                        existingNotepad.setIsArchived(notepadDto.getIsArchived());
                    }
                    Notepad updatedNotepad = notepadRepository.save(existingNotepad);
                    return convertToDto(updatedNotepad);
                });
    }
    
    // Archive/Unarchive notepad
    public Optional<NotepadDto> toggleArchiveStatus(Long id) {
        return notepadRepository.findById(id)
                .map(notepad -> {
                    notepad.setIsArchived(!notepad.getIsArchived());
                    Notepad updatedNotepad = notepadRepository.save(notepad);
                    return convertToDto(updatedNotepad);
                });
    }
    
    // Delete notepad
    public boolean deleteNotepad(Long id) {
        if (notepadRepository.existsById(id)) {
            notepadRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    // Search notepads by title or content
    public List<NotepadDto> searchNotepads(String searchTerm) {
        return notepadRepository.findByTitleOrContentContaining(searchTerm).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // Search notepads by title
    public List<NotepadDto> searchByTitle(String title) {
        return notepadRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // Search notepads by content
    public List<NotepadDto> searchByContent(String content) {
        return notepadRepository.findByContentContainingIgnoreCase(content).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // Get notepads created after a specific date
    public List<NotepadDto> getNotepadsCreatedAfter(LocalDateTime date) {
        return notepadRepository.findByCreatedAtAfter(date).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // Get notepads updated after a specific date
    public List<NotepadDto> getNotepadsUpdatedAfter(LocalDateTime date) {
        return notepadRepository.findByUpdatedAtAfter(date).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // Get statistics
    public long getActiveNotepadCount() {
        return notepadRepository.countByIsArchived(false);
    }
    
    public long getArchivedNotepadCount() {
        return notepadRepository.countByIsArchived(true);
    }
    
    public long getTotalNotepadCount() {
        return notepadRepository.count();
    }
} 