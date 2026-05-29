package org.test.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.test.dto.request.AddEmailRequest;
import org.test.dto.request.AddPhoneRequest;
import org.test.dto.request.UpdatePhoneRequest;
import org.test.security.JwtService;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.cache.type", () -> "none");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    private String getAuthToken(Long userId) {
        return "Bearer " + jwtService.generateAccessToken(userId);
    }

    @Test
    @Order(1)
    void testGetUser() throws Exception {
        mockMvc.perform(get("/api/v1/users/1")
                        .header("Authorization", getAuthToken(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Иван Иванов"))
                .andExpect(jsonPath("$.emails").isArray())
                .andExpect(jsonPath("$.emails[0]").value("ivan@mail.com"))
                .andExpect(jsonPath("$.phones").isArray())
                .andExpect(jsonPath("$.phones[0]").value("79008007060"));
    }

    @Test
    @Order(2)
    void testGetUserNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/users/999")
                        .header("Authorization", getAuthToken(999L)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(3)
    void testSearchUsersByName() throws Exception {
        mockMvc.perform(get("/api/v1/users/search")
                        .param("name", "Иван")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", getAuthToken(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Иван Иванов"))
                .andExpect(jsonPath("$.content[0].emails[0]").value("ivan@mail.com"));
    }

    @Test
    @Order(4)
    void testSearchUsersByEmail() throws Exception {
        mockMvc.perform(get("/api/v1/users/search")
                        .param("email", "ivan@mail.com")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", getAuthToken(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].emails[0]").value("ivan@mail.com"));
    }

    @Test
    @Order(5)
    void testSearchUsersByPhone() throws Exception {
        mockMvc.perform(get("/api/v1/users/search")
                        .param("phone", "79008007060")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", getAuthToken(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].phones[0]").value("79008007060"));
    }

    @Test
    @Order(6)
    void testAddEmail() throws Exception {
        AddEmailRequest request = new AddEmailRequest();
        request.setEmail("new.email@example.com");

        mockMvc.perform(post("/api/v1/users/email")
                        .header("Authorization", getAuthToken(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emails").isArray());
    }

    @Test
    @Order(7)
    void testAddEmailAlreadyExists() throws Exception {
        AddEmailRequest request = new AddEmailRequest();
        request.setEmail("ivan@mail.com");

        mockMvc.perform(post("/api/v1/users/email")
                        .header("Authorization", getAuthToken(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(8)
    void testAddEmailUsedByOtherUser() throws Exception {
        AddEmailRequest request = new AddEmailRequest();
        request.setEmail("petr@mail.com");

        mockMvc.perform(post("/api/v1/users/email")
                        .header("Authorization", getAuthToken(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(9)
    void testUpdateEmail() throws Exception {
        mockMvc.perform(put("/api/v1/users/email/1")
                        .header("Authorization", getAuthToken(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"updated.email@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emails").isArray());
    }

    @Test
    @Order(10)
    void testUpdateEmailNotFound() throws Exception {
        mockMvc.perform(put("/api/v1/users/email/999")
                        .header("Authorization", getAuthToken(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"nonexistent@example.com\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(11)
    void testDeleteLastEmail() throws Exception {
        mockMvc.perform(delete("/api/v1/users/email/delete/2")
                        .header("Authorization", getAuthToken(2L)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(12)
    void testAddPhone() throws Exception {
        AddPhoneRequest request = new AddPhoneRequest();
        request.setPhone("79207865435");

        mockMvc.perform(post("/api/v1/users/phone")
                        .header("Authorization", getAuthToken(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phones").isArray());
    }

    @Test
    @Order(13)
    void testAddPhoneAlreadyExists() throws Exception {
        AddPhoneRequest request = new AddPhoneRequest();
        request.setPhone("79008007060");

        mockMvc.perform(post("/api/v1/users/phone")
                        .header("Authorization", getAuthToken(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(14)
    void testAddPhoneUsedByOtherUser() throws Exception {
        AddPhoneRequest request = new AddPhoneRequest();
        request.setPhone("79008007061");

        mockMvc.perform(post("/api/v1/users/phone")
                        .header("Authorization", getAuthToken(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(15)
    void testUpdatePhone() throws Exception {
        UpdatePhoneRequest request = new UpdatePhoneRequest();
        request.setPhone("79207865436");

        mockMvc.perform(put("/api/v1/users/phone/1")
                        .header("Authorization", getAuthToken(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phones").isArray());
    }

    @Test
    @Order(16)
    void testUpdatePhoneNotFound() throws Exception {
        UpdatePhoneRequest request = new UpdatePhoneRequest();
        request.setPhone("79207865437");

        mockMvc.perform(put("/api/v1/users/phone/999")
                        .header("Authorization", getAuthToken(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(17)
    void testDeletePhone() throws Exception {
        AddPhoneRequest addRequest = new AddPhoneRequest();
        addRequest.setPhone("79207865438");

        mockMvc.perform(post("/api/v1/users/phone")
                        .header("Authorization", getAuthToken(2L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/users/phone/delete/2")
                        .header("Authorization", getAuthToken(2L)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(18)
    void testDeleteLastPhone() throws Exception {
        mockMvc.perform(delete("/api/v1/users/phone/delete/3")
                        .header("Authorization", getAuthToken(3L)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(19)
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isForbidden());
    }
}