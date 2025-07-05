package com.example.demo.repository;

import com.example.demo.entity.Notepad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotepadRepository extends JpaRepository<Notepad, Long> {
    
    // Find all notepads that are not archived
    List<Notepad> findByIsArchivedFalse();
    
    // Find notepads by title containing the given text (case-insensitive)
    List<Notepad> findByTitleContainingIgnoreCase(String title);
    
    // Find notepads by content containing the given text (case-insensitive)
    List<Notepad> findByContentContainingIgnoreCase(String content);
    
    // Find notepads by title or content containing the given text
    @Query("SELECT n FROM Notepad n WHERE LOWER(n.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(n.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Notepad> findByTitleOrContentContaining(@Param("searchTerm") String searchTerm);
    
    // Find archived notepads
    List<Notepad> findByIsArchivedTrue();
    
    // Find notepads created after a specific date
    List<Notepad> findByCreatedAtAfter(java.time.LocalDateTime date);
    
    // Find notepads updated after a specific date
    List<Notepad> findByUpdatedAtAfter(java.time.LocalDateTime date);
    
    // Count notepads by archive status
    long countByIsArchived(Boolean isArchived);
} 