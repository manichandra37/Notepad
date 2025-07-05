package com.example.demo.config;

import com.example.demo.entity.Notepad;
import com.example.demo.repository.NotepadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private NotepadRepository notepadRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // Clear existing data
        notepadRepository.deleteAll();
        
        // Create sample notepads
        Notepad notepad1 = new Notepad("Welcome Note", "Welcome to your new notepad application! This is your first note.");
        notepadRepository.save(notepad1);
        
        Notepad notepad2 = new Notepad("Shopping List", "Milk\nBread\nEggs\nButter\nCheese");
        notepadRepository.save(notepad2);
        
        Notepad notepad3 = new Notepad("Meeting Notes", "Team meeting scheduled for Friday at 2 PM.\nAgenda:\n- Project updates\n- New features discussion\n- Q&A session");
        notepadRepository.save(notepad3);
        
        Notepad notepad4 = new Notepad("Ideas", "App ideas:\n- Task manager\n- Recipe book\n- Travel planner\n- Budget tracker");
        notepad4.setIsArchived(true);
        notepadRepository.save(notepad4);
        
        Notepad notepad5 = new Notepad("Quick Reminder", "Don't forget to:\n- Call mom\n- Pay bills\n- Buy groceries\n- Schedule dentist appointment");
        notepadRepository.save(notepad5);
        
        System.out.println("Sample notepads created successfully!");
        System.out.println("Total notepads: " + notepadRepository.count());
    }
} 