package edu.iu.p466.great_speeches.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import edu.iu.p466.great_speeches.data.SpeechRepository;
import edu.iu.p466.great_speeches.model.Speech;

@Controller
public class HomeController {

    private final SpeechRepository speechRepo;

    @Autowired
    public HomeController(SpeechRepository speechRepo) {
        this.speechRepo = speechRepo;
    }

    @GetMapping("/")
    public String home(Model model, @RequestParam(required = false) String query) {
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
        return "home";
    }

    @PostMapping("/search")
    public String search(@RequestParam String query) {
        if (query.isEmpty() || query.equals("CLEAR")) {
            return "redirect:/";
        }
        return "redirect:/?query=" + query;
    }

    @GetMapping("/403")
    public String error(Model model) {
        model.addAttribute("status", 403);
        model.addAttribute("error", "Forbidden");
        model.addAttribute("message", "You do not have permission to access this resource.");
        return "error";
    }
}
