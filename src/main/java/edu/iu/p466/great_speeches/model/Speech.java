package edu.iu.p466.great_speeches.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Speech {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String date;
    private String speaker;

    @jakarta.persistence.Column(length = 10000)
    private String content;
    private Set<String> topics = new HashSet<>();
    // Audio file
    private String audioPath;

    public Speech(String date, String speaker, String content, Set<String> topics, String audioPath) {
        // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        // this.date = LocalDate.parse(date, formatter);
        this.date = date;

        this.speaker = speaker;
        this.content = content;
        this.topics.addAll(topics);
        this.audioPath = audioPath;
    }

    public Speech() {
        // this.date = 
        this.speaker = ""; 
        this.content = "";
        this.topics = null;
        this.audioPath = "";
    }

    public Integer getId() {
        return id;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public String getSpeaker() {
        return speaker;
    }
    public void setSpeaker(String speaker) {
        this.speaker = speaker;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public Set<String> getTopics() {
        return topics;
    }
    public void setTopics(Set<String> topics) {
        this.topics = topics;
    }

    public String getAudio() {
        if (audioPath.isEmpty()) return "No";
        return "Yes";
    }
    public String getAudioPath() {
        return audioPath;
    }
    // Another one here for get playable audio
    public void setAudio(String audioPath) {
        this.audioPath = audioPath;
    }

   

    

    

}
