package edu.iu.p466.great_speeches.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Stream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import edu.iu.p466.great_speeches.data.SpeechRepository;
import edu.iu.p466.great_speeches.model.Speech;

@Configuration
public class DataConfig {

    @Bean
    public CommandLineRunner loadData(SpeechRepository repository) {
        return args -> {
            cleanUploadsFolder();
            // Add MLK Speech
            repository.save(new Speech(
                "08/28/1963",
                "Martin Luther King Jr.", 
                getSpeech("MLK:"), 
                Set.of("Civil Rights", "Equality"), 
                "/uploads/MLKDreamSpeech.mp3"
            ));

            // Add Lincoln Speech
            repository.save(new Speech(
                "11/19/1863",
                "Abraham Lincoln", 
                getSpeech("Lincoln:"), 
                Set.of("Unity", "History"), 
                ""
            ));

            // Add Lincoln Speech
            repository.save(new Speech(
                "03/23/1775",
                "Patrick Henry", 
                getSpeech("Patrick:"), 
                Set.of("History", "Freedom"), 
                ""
            ));
        };
    }

    public String getSpeech(String name) {
        // Use ClassLoader to look at the root of the classpath
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("defaultSpeeches.txt")) {
            if (is == null) {
                return "File not found!";
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                StringJoiner content = new StringJoiner("\n");
                String line;
                boolean nameFound = false;

                while ((line = br.readLine()) != null) {
                    if (!nameFound) {
                        if (line.trim().equalsIgnoreCase(name)) {
                            nameFound = true;
                        }
                        continue;
                    }

                    if (line.trim().isEmpty()) {
                        break; 
                    }

                    content.add(line);
                }

                return content.toString();
            }
        } catch (java.io.IOException e) {
            return "Error reading file: " + e.getMessage();
        } catch (Exception e) {
            return "General Error: " + e.getMessage();
        }
    }

    // Clear all .mp3 files besides defualt mlk one
    private void cleanUploadsFolder() {
        Path root = Paths.get("uploads");
        String defaultFile = "MLKDreamSpeech.mp3";

        if (Files.exists(root)) {
            try (Stream<Path> files = Files.list(root)) {
                files.forEach(file -> {
                    // Check if it's a file and no the default one
                    if (!file.getFileName().toString().equals(defaultFile)) {
                        try {
                            Files.delete(file);
                        } catch (IOException e) {
                            System.err.println("Could not delete file: " + file);
                        }
                    }
                });
            } catch (IOException e) {
                System.err.println("Could not list files in uploads: " + e.getMessage());
            }
        }
    }
}