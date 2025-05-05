//package com.example.financetracker;
//
//
//import com.example.financetracker.dto.JwtRequestDto;
//import com.example.financetracker.dto.RegistrationUserDto;
//import com.example.financetracker.entity.UserEntity;
//import com.example.financetracker.repository.UserRepository;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.test.web.servlet.MockMvc;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//
//@SpringBootTest(classes = FinanceTrackerApplication.class)
//@AutoConfigureMockMvc
//@Testcontainers
//public class AuthIntegrationTest {
//    @Container
//    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:11-alpine")
//            .withDatabaseName("financetracker")
//            .withUsername("root")
//            .withPassword("root");
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @BeforeEach
//    void setUp() {
//        userRepository.deleteAll();
//    }
//
//
//    @Test
//    void createAuthToken_ShouldReturnJwtToken_WhenCredentialsAreValid() throws Exception {
//        // Arrange
//        UserEntity user = new UserEntity();
//        user.setUsername("testUser");
//        user.setPassword(passwordEncoder.encode("testPassword"));
//        user.setEmail("testEmail@gmail.com");
//        userRepository.save(user);
//
//        JwtRequestDto authRequest = new JwtRequestDto("testUser", "testPassword");
//
//        // Act and Assert
//        mockMvc.perform(post("/auth")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(authRequest)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.token").exists());
//    }
//
//    @Test
//    void createAuthToken_ShouldReturnJwtToken_WhenCredentialsAreInvalid() throws Exception {
//        // Arrange
//        UserEntity user = new UserEntity();
//        user.setUsername("testUser");
//        user.setPassword(passwordEncoder.encode("testPassword"));
//        user.setEmail("testEmail@gmail.com");
//        userRepository.save(user);
//
//        JwtRequestDto authRequest = new JwtRequestDto("testUser", "wrongPassword");
//
//        // Act and Assert
//        mockMvc.perform(post("/auth")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(authRequest)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.status").value("400"))
//                .andExpect(jsonPath("$.message").value("Incorrect login or password"));
//    }
//
//    @Test
//    void createNewUser_ShouldReturnBadRequest_WhenPasswordsDoNotMatch() throws Exception {
//        // Arrange
//        RegistrationUserDto registrationUserDto = new RegistrationUserDto("testUser", "password", "differentPassword", "testEmail@gmail.com");
//
//        // Act and Assert
//        mockMvc.perform(post("/registration")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(registrationUserDto)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.status").value("400"))
//                .andExpect(jsonPath("$.message").value("Passwords do not match"));
//    }
//
//    @Test
//    void createNewUser_ShouldReturnBadRequest_WhenUserNameAlreadyExists() throws Exception {
//        // Arrange
//        UserEntity user = new UserEntity();
//        user.setUsername("testUser");
//        user.setPassword(passwordEncoder.encode("testPassword"));
//        user.setEmail("testEmail@gmail.com");
//        userRepository.save(user);
//
//        RegistrationUserDto registrationUserDto = new RegistrationUserDto("testUser", "password", "testPassword", "testEmail@gmail.com");
//
//        // Act and Asser
//        mockMvc.perform(post("/registration")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(registrationUserDto)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.status").value("400"))
//                .andExpect(jsonPath("$.message").value("Username already exists"));
//
//    }
//
//    @Test
//    void createNewUser_ShouldReturnUserDto_WhenCredentialsAreValid() throws Exception {
//        // Arrange
//        RegistrationUserDto registrationUserDto = new RegistrationUserDto("testUser", "testPassword", "testPassword", "testEmail@gmail.com");
//
//        // Act and Assert
//        mockMvc.perform(post("/registration")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(registrationUserDto)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.status").value("200"))
//                .andExpect(jsonPath("$.id").exists())
//                .andExpect(jsonPath("$.username").exists())
//                .andExpect(jsonPath("$.email").exists())
//                .andExpect(jsonPath("$.categories").exists());
//    }
//}
