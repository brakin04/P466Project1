package edu.iu.p466.great_speeches.controllers;

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import edu.iu.p466.great_speeches.data.SpeechRepository;
import edu.iu.p466.great_speeches.model.Speech;
import java.util.Optional;

@Controller
@RequestMapping("/speeches")
// @SessionAttributes("speeches")
public class SpeechController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminController.class);
    private SpeechRepository speechRepo;

    public SpeechController(SpeechRepository speechRepo) {
        this.speechRepo = speechRepo;
    }

    @GetMapping("/view-speech")
    public String viewSpeech(Model model, @RequestParam Integer id) {
        Optional<Speech> theSpeech = speechRepo.findById(id);
        if (theSpeech.isPresent()) model.addAttribute("speech", theSpeech.get());
        else model.addAttribute("speech", new Speech());
        return "speechView";
    }    

}
