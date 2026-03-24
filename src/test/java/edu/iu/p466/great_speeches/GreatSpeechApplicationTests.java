package edu.iu.p466.great_speeches;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.iu.p466.great_speeches.controllers.AdminController;
import edu.iu.p466.great_speeches.controllers.HomeController;
import edu.iu.p466.great_speeches.controllers.LoginController;
import edu.iu.p466.great_speeches.controllers.SpeechController;
import edu.iu.p466.great_speeches.data.SpeechRepository;
import edu.iu.p466.great_speeches.model.Speech;

@SpringBootTest
@ActiveProfiles("test")
class GreatSpeechApplicationTests {

	@Test
	void contextLoads() {
	}

	@Autowired
	private AdminController ac;
	@Autowired
	private SpeechRepository speechRepo;
	@Autowired
	private HomeController hc;
	@Autowired
	private SpeechController sc;
	@Autowired
	private LoginController lc;

	@org.junit.jupiter.api.BeforeEach
    void cleanUp() {
        speechRepo.deleteAll(); // Ensures a fresh start for every test
    }

	@Test
	void beansAreInjected() {
		assertNotNull(ac);
		assertNotNull(hc);
		assertNotNull(lc);
		assertNotNull(sc);
		assertNotNull(speechRepo);
	}

	@Test
	void testDatabaseSaveAndRetrieve() {
		Speech speech = new Speech();
		speech.setSpeaker("Test Speaker");
		speech.setContent("Test Content");
		speech.setTopics(Set.of("one", "two"));
		speech.setDate("01/01/2000");

		speechRepo.save(speech);

		Iterable<Speech> found = speechRepo.findAll();
		
		assertTrue(found.iterator().hasNext());
		Speech firstSpeech = found.iterator().next();
        
        // 3. Assert the fields match what you saved
        assertEquals("Test Speaker", firstSpeech.getSpeaker());
        assertEquals("Test Content", firstSpeech.getContent());
        assertEquals("01/01/2000", firstSpeech.getDate());
		assertTrue(firstSpeech.getTopics().containsAll(Set.of("one", "two")));
		assertEquals("", firstSpeech.getAudioPath());
		assertEquals("No", firstSpeech.getAudio());
	}

}
