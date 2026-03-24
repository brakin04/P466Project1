package edu.iu.p466.great_speeches.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import edu.iu.p466.great_speeches.model.Speech;

@Repository
public interface SpeechRepository extends CrudRepository<Speech, Integer> {
    
}