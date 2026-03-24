package edu.iu.p466.great_speeches.controllers;

import edu.iu.p466.great_speeches.model.Speech;
import edu.iu.p466.great_speeches.data.SpeechRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile; // For creating the fake file
import java.util.List;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;

@WebMvcTest(AdminController.class)
// If you use Spring Security, you might need to mock a user with @WithMockUser
@WithMockUser(roles = "ADMIN") 
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SpeechRepository speechRepo; // Mocks the database

    @Test
    public void testLoadHome() throws Exception {
        // 1. Mock the data your repository would return
        List<Speech> mockSpeeches = Arrays.asList(
            new Speech("08/28/1963", "MLK", "I have a dream", new HashSet<>(), "uploads/mlk.mp3"),
            new Speech("01/01/1940", "Churchill", "We shall fight", new HashSet<>(), "")
        );
        
        when(speechRepo.findAll()).thenReturn(mockSpeeches);

        // 2. Perform the GET request
        mockMvc.perform(get("/admin/"))
                .andExpect(status().isOk())
                .andExpect(view().name("adminHome"))
                .andExpect(model().attributeExists("speeches"))
                // Verify that all 2 speeches are passed to the view
                .andExpect(model().attribute("speeches", hasSize(2)));
    }

    @Test
    public void testLoadHomeWithQuery() throws Exception {
        // 1. Setup mock data
        List<Speech> mockSpeeches = Arrays.asList(
            new Speech("08/08/1963", "MLK", "Dream content", new HashSet<>(), ""),
            new Speech("12/12/1940", "Churchill", "Fight content", new HashSet<>(), "")
        );
        
        when(speechRepo.findAll()).thenReturn(mockSpeeches);

        // 2. Perform GET with the query parameter "MLK"
        mockMvc.perform(get("/admin/").param("query", "MLK"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("speeches", hasSize(1)))
                // Verify that ONLY the MLK speech made it through the filter
                .andExpect(model().attribute("speeches", hasItem(
                    hasProperty("speaker", is("MLK"))
                )));

        // Clear query
        mockMvc.perform(get("/admin/").param("query", ""))
                .andExpect(status().isOk())
                .andExpect(model().attribute("speeches", hasSize(2)));
    }

    @Test
    public void testAddSpeechWithFileUpload() throws Exception {
        // Create a fake file
        MockMultipartFile mockFile = new MockMultipartFile(
                "audioFile", 
                "test-speech.mp3", 
                "audio/mpeg", 
                "fake audio content".getBytes()
        );

        mockMvc.perform(multipart("/admin/add-speech")
                .file(mockFile).with(csrf())
                .param("content", "I have a dream")
                .param("speaker", "MLK")
                .param("topics", "Freedom", "Justice")
                .param("date", "12/11/1098"))
                .andExpect(status().is3xxRedirection()) // Expect redirect to /admin/
                .andExpect(redirectedUrl("/admin/"));

        // Verify the repository actually tried to save a Speech object
        verify(speechRepo, times(1)).save(any(Speech.class));
    }

    @Test
    public void testSearchRedirect() throws Exception {
        mockMvc.perform(post("/admin/search").param("query", "MLK").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/?query=MLK"));

        mockMvc.perform(post("/admin/search").param("query", "").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/"));
    }

    @Test
    public void testEditSpeech() throws Exception {
        // 1. Create a fake speech to be "found" in the database
        Speech mockSpeech = new Speech("11/12/1963", "MLK", "I have a dream", new HashSet<>(), "/uploads/mlk.mp3");
        Speech mockSpeech2 = new Speech("11/11/1800", "John", "Something was said", new HashSet<>(), "");
        
        // 2. Tell the mock repository to return this speech when ID 1 is requested
        // Note: Use Optional.of() because findById returns an Optional
        when(speechRepo.findById(1)).thenReturn(Optional.of(mockSpeech));
        when(speechRepo.findById(2)).thenReturn(Optional.of(mockSpeech2));

        // 3. Perform the GET request to /admin/edit-speech?id=1
        mockMvc.perform(get("/admin/edit-speech").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("editSpeech")) // Check the HTML template name
                .andExpect(model().attributeExists("speech")) // Check the model key
                .andExpect(model().attribute("speech", hasProperty("speaker", is("MLK"))));

        // Check for second
        mockMvc.perform(get("/admin/edit-speech").param("id", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("editSpeech")) // Check the HTML template name
                .andExpect(model().attributeExists("speech")) // Check the model key
                .andExpect(model().attribute("speech", hasProperty("speaker", is("John"))));
    }

    @Test
    public void testUpdateSpeech() throws Exception {
        Speech s1 = new Speech("11/12/1963", "MLK", "I have a dream", new HashSet<>(), "/uploads/mlk.mp3");
        Speech s2 = new Speech("11/11/1800", "John", "Something was said", new HashSet<>(), "");
        when(speechRepo.findById(1)).thenReturn(Optional.of(s1));
        when(speechRepo.findById(2)).thenReturn(Optional.of(s2));

        mockMvc.perform(multipart("/admin/update-speech").with(csrf())
                .param("id", "1")
                .param("date", "11/11/1213")
                .param("speaker", "not MLK")
                .param("content", "speech content 1")
                .param("topics", "one", "two"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/"));

        mockMvc.perform(multipart("/admin/update-speech").with(csrf())
                .param("id", "2")
                .param("date", "01/01/1213")
                .param("speaker", "Willy")
                .param("content", "nothing in content")
                .param("topics", "four", "five"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/"));
        verify(speechRepo, times(2)).save(any(Speech.class));
    }

    @Test
    public void testDeleteSpeech() throws Exception {
        mockMvc.perform(get("/admin/delete-speech").param("id", "1"))
                .andExpect(status().is3xxRedirection());
        verify(speechRepo).deleteById(1);

        mockMvc.perform(get("/admin/delete-speech").param("id", "2"))
                .andExpect(status().is3xxRedirection());
        verify(speechRepo).deleteById(2);
    }

    @Test
    void testResponseTimeIsUnderTwoSeconds() throws Exception {
        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/admin/"))
            .andExpect(status().isOk());

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertTrue(duration < 2000, "Response time was " + duration + "ms, which is >= 2s");
    }

    @Test
    void testHandle1000ConcurrentUsers() throws InterruptedException {
        when(speechRepo.findAll()).thenReturn(Arrays.asList(new Speech()));

        int numberOfUsers = 1000;
        ExecutorService service = Executors.newFixedThreadPool(numberOfUsers);
        CountDownLatch latch = new CountDownLatch(numberOfUsers);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfUsers; i++) {
            service.submit(() -> {
                try {
                    mockMvc.perform(get("/admin/")
                            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                        .andExpect(status().isOk());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("Request failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait up to 30 seconds for all users to finish
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        
        assertTrue(completed, "Test timed out before all 1000 users finished");
        assertEquals(1000, successCount.get(), "Not all 1000 users received a 200 OK");
    }

}