package edu.iu.p466.great_speeches.controllers;

import edu.iu.p466.great_speeches.model.Speech;
import edu.iu.p466.great_speeches.data.SpeechRepository;
import edu.iu.p466.great_speeches.config.SecurityConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Arrays;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;

@WebMvcTest(HomeController.class)
@Import(SecurityConfig.class)
public class HomeControllerTest {

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
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
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
        mockMvc.perform(get("/").param("query", "MLK"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("speeches", hasSize(1)))
                // Verify that ONLY the MLK speech made it through the filter
                .andExpect(model().attribute("speeches", hasItem(
                    hasProperty("speaker", is("MLK"))
                )));

        // Clear query
        mockMvc.perform(get("/").param("query", ""))
                .andExpect(status().isOk())
                .andExpect(model().attribute("speeches", hasSize(2)));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    public void testSearchRedirect() throws Exception {
        mockMvc.perform(post("/search").param("query", "MLK").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?query=MLK"));

        mockMvc.perform(post("/search").param("query", "").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    public void test403Error() throws Exception {
        mockMvc.perform(get("/admin/"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testErrorPageContent() throws Exception {
        mockMvc.perform(get("/403"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("status", 403))
                .andExpect(model().attribute("error", "Forbidden"));
    }

    @Test
    void testResponseTimeIsUnderTwoSeconds() throws Exception {
        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/"))
            .andExpect(status().isOk());

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertTrue(duration < 2000, "Response time was " + duration + "ms, which is >= 2s");
    }

    @Test
    void testHandle1000ConcurrentUsers() throws InterruptedException {
        int numberOfUsers = 1000;
        ExecutorService service = Executors.newFixedThreadPool(numberOfUsers);
        CountDownLatch latch = new CountDownLatch(numberOfUsers);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfUsers; i++) {
            service.submit(() -> {
                try {
                    // Using MockMvc is safer for local threads than a real HTTP client
                    mockMvc.perform(get("/"))
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