package edu.iu.p466.great_speeches.controllers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import edu.iu.p466.great_speeches.data.SpeechRepository;
import edu.iu.p466.great_speeches.model.Speech;
import jakarta.annotation.security.RolesAllowed;

@Controller
@RequestMapping("/admin")
@RolesAllowed("ADMIN")
public class AdminController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminController.class);

    private final SpeechRepository speechRepo;
  
    @Autowired
    public AdminController(SpeechRepository speechRepo) {
        this.speechRepo = speechRepo;
    }

    @GetMapping("/")
    public String adminHome(Model model, @RequestParam(required = false) String query) {
        Iterable<Speech> allSpeeches = speechRepo.findAll();
        List<Speech> filteredSpeeches = new ArrayList<>();
        if (query != null) {
            String lowerQuery = query.toLowerCase();
            for (Speech s : allSpeeches) {
                boolean topicMatch = s.getTopics().stream().anyMatch(t -> t.toLowerCase().contains(lowerQuery));
                if (topicMatch || s.getSpeaker().toLowerCase().contains(lowerQuery) 
                    || s.getContent().toLowerCase().contains(lowerQuery)) {
                    filteredSpeeches.add(s);
                }
            }
        } else {
            allSpeeches.forEach(filteredSpeeches::add);
        }
        model.addAttribute("speeches", filteredSpeeches);
        return "adminHome";
    }

    @PostMapping("/search")
    public String search(@RequestParam String query) {
        if (query.isEmpty() || query.equals("CLEAR")) {
            return "redirect:/admin/";
        }
        return "redirect:/admin/?query=" + query;
    }

    @PostMapping(value = "/add-speech", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE) 
    public String addSpeech(@RequestParam String content, @RequestParam String speaker,
                        @RequestParam String[] topics, @RequestParam MultipartFile audioFile,
                        @RequestParam String date) throws IOException {
        
        String audioFilePath = "";
        if (audioFile != null && !audioFile.isEmpty()) {
            String uploadDir = "uploads/";
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs(); 
            }

            File destination = new File(directory.getAbsolutePath() + File.separator + audioFile.getOriginalFilename());
            audioFile.transferTo(destination);
            
            audioFilePath = "/uploads/" + audioFile.getOriginalFilename();
        }

        Set<String> topicsSet = new HashSet<>(java.util.Arrays.asList(topics));
        
        Speech newSpeech = new Speech(date, speaker, content, topicsSet, audioFilePath);
        speechRepo.save(newSpeech);
        
        return "redirect:/admin/";
    }

    @GetMapping("/edit-speech") 
    public String editSpeech(Model model, @RequestParam Integer id) {
        Optional<Speech> theSpeech = speechRepo.findById(id);
        if (theSpeech.isPresent()) model.addAttribute("speech", theSpeech.get());
        return "editSpeech";
    }

    @PostMapping(value = "/update-speech", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public String updateSpeech(@RequestParam Integer id, @RequestParam String date, @RequestParam String content, @RequestParam String speaker,
                        @RequestParam String[] topics, @RequestParam(value = "audioFile", required = false) MultipartFile audioFile) 
                        throws IOException {
        // Work here
        Optional<Speech> theSpeech = speechRepo.findById(id);
        if (theSpeech.isPresent()) {
            // add topics to set instead of array
            Set<String> newTopics = new HashSet<>();
            for (String topic : topics) {
                newTopics.add(topic);
            }
            Speech speech = theSpeech.get();
            speech.setDate(date);
            speech.setTopics(newTopics);
            speech.setSpeaker(speaker);
            speech.setContent(content);

            if (audioFile != null && !audioFile.isEmpty()) {
                System.out.println("audiofile is " + audioFile);
                String uploadDir = System.getProperty("user.dir") + "/uploads/";
                File directory = new File(uploadDir);

                if (!directory.exists()) directory.mkdirs();
                String audioFilePath = uploadDir + audioFile.getOriginalFilename();
                File destination = new File(audioFilePath);
                audioFile.transferTo(destination);

                speech.setAudio("/uploads/" + audioFile.getOriginalFilename());
            } 
            // save it 
            speechRepo.save(speech);

        }
        return "redirect:/admin/";
    }

    @GetMapping("/delete-speech")
    public String deleteSpeech(Model model, @RequestParam Integer id) {
        speechRepo.deleteById(id);
        return "redirect:/admin/";
    }

}
