package com.example.financetracker.controller;


import com.example.financetracker.FinanceTrackerApplication;
import com.example.financetracker.dto.*;
import com.example.financetracker.entity.CategoryEntity;
import com.example.financetracker.entity.UserEntity;
import com.example.financetracker.service.impl.*;
import com.example.financetracker.util.JwtTokenUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = FinanceTrackerApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityConfigTest {


    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthServiceImpl authService;

    @MockitoBean
    private UserServiceImpl userService;

    @MockitoBean
    private BudgetServiceImpl budgetService;

    @MockitoBean
    private CategoryServiceImpl categoryService;

    @MockitoBean
    private JwtTokenUtils jwtTokenUtils;
    private String token;

    @MockitoBean
    private TransactionServiceImpl transactionService;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        JwtRequestDto authRequestDto = new JwtRequestDto("testUser", "testPassword");
        JwtResponseDto jwtResponseDto = new JwtResponseDto("token", "refreshToken");
        when(authService.createAuthToken(any(JwtRequestDto.class))).thenReturn(jwtResponseDto);
        token = "Bearer " + jwtResponseDto.getToken();

        when(jwtTokenUtils.getUsername(anyString())).thenReturn("testUser");
        when(jwtTokenUtils.getRoles(anyString())).thenReturn(List.of("ROLE_USER"));

        UserDto user = new UserDto();
        user.setUsername("testUser");
        when(userService.getCurrentUserDto()).thenReturn(user);

        UserEntity testUser = new UserEntity();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setEmail("testEmail@gmail.com");
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(userService.createNewUser(any(RegistrationUserDto.class))).thenReturn(testUser);
        when(userService.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        CategoryEntity testCategory = new CategoryEntity();
        testCategory.setId(1L);
        testCategory.setName("Book");
        when(categoryService.getDefaultCategories()).thenReturn(List.of(testCategory));
        when(categoryService.addCategory(any(CategoryDto.class))).thenReturn(testCategory);

        TransactionResponseDto transactionResponseDto = new TransactionResponseDto(
                1L, BigDecimal.valueOf(100.0),
                "testTransactional",
                LocalDateTime.now(),
                "Book"
        );

        Page<TransactionResponseDto> transactionPage = new PageImpl<>(
                Collections.singletonList(transactionResponseDto),
                PageRequest.of(0, 10),
                1
        );
        when(transactionService.getUserTransactions(anyLong(), any(TransactionFilterRequestDto.class)))
                .thenReturn(transactionPage);
    }


    @Test
    void authEndpoint_ShouldReturnOK_WithoutAuthentication() throws Exception {
        // Arrange
        JwtRequestDto authRequestDto = new JwtRequestDto("testUser", "testPassword");

        // Act and Assert
        mockMvc.perform(post("/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void registerNewUser_ShouldReturnOK_WithAuthentication() throws Exception {
        // Arrange
        RegistrationUserDto registrationUserDto = new RegistrationUserDto("newUser", "newTestPassword", "newTestPassword", "newTestEmail@gmail.com");

        // Act and Assert
        mockMvc.perform(post("/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationUserDto)))
                .andExpect(status().isOk());
    }

    static String budgetJson = """
    {
      "categoryId": 2,
      "limitAmount": 1500,
      "period": "WEEKLY",
      "startDate": "2025-04-23T14:39:45.795Z",
      "endDate":   "2025-04-23T14:39:45.795Z"
    }""";

    static String transactionJson = """
    {
      "amount": 100,
      "description": "string",
      "categoryId": 2
      }""";

    static String beforeUpdateBudgetJson = """
    {
      "categoryId": 8,
      "categoryName": "Бензин",
      "limitAmount": 1100.00,
      "period": "WEEKLY",
      "startDate": "2025-04-15T12:37:38.491",
      "endDate": "2025-05-15T12:37:38.491"}""";

    static String addCategory = """
    {"name": "Book"}""";

    static final String url = "//financeTracker";


    static Stream<Arguments> securedEndpoints() {
        return Stream.of(
                // POST
                Arguments.of(HttpMethod.POST, url+"/add_category", addCategory, HttpStatus.CREATED),
                Arguments.of(HttpMethod.POST, url+"/budgets", budgetJson, HttpStatus.CREATED),
                Arguments.of(HttpMethod.POST, url+"/transactions", transactionJson, HttpStatus.CREATED),
                Arguments.of(HttpMethod.POST, url+"/category_to_current_user", addCategory, HttpStatus.OK),

                // GET
                Arguments.of(HttpMethod.GET, url+"/transactions", null, HttpStatus.OK),
                Arguments.of(HttpMethod.GET, url+"/get_my_categories", null, HttpStatus.OK),
                Arguments.of(HttpMethod.GET, url+"/budgets", null, HttpStatus.OK),
                Arguments.of(HttpMethod.GET, url+"/budgets/2", null, HttpStatus.OK),
                Arguments.of(HttpMethod.GET, url+"/monthlyStatistic?startDate=2025-04&endDate=2025-04", null, HttpStatus.OK),
                Arguments.of(HttpMethod.GET, url+"/dailyDynamics?startDate=2025-04-03&endDate=2025-05-03", null, HttpStatus.OK),
                Arguments.of(HttpMethod.GET, url+"/dailyDynamics?startDate=2025-04-03&endDate=2025-05-03", null, HttpStatus.OK),
                Arguments.of(HttpMethod.GET, url+"/categorySummary?startDate=2025-04-10&endDate=2025-04-15", null, HttpStatus.OK),
                Arguments.of(HttpMethod.GET, url+"/balanceSummary?startDate=2025-04-10&endDate=2025-04-15", null, HttpStatus.OK),
                Arguments.of(HttpMethod.GET, url+"/notifications?userId=3", null, HttpStatus.OK),

                // PUT
                Arguments.of(HttpMethod.PUT, url+"/budgets/2", beforeUpdateBudgetJson, HttpStatus.OK),

                // DELETE
                Arguments.of(HttpMethod.DELETE, url+"/budgets/2", null, HttpStatus.NO_CONTENT)
        );
    }

    @ParameterizedTest
    @MethodSource("securedEndpoints")
    void testSecuredEndpoints(HttpMethod httpMethod, String url, String requestBody, HttpStatus expectedStatus) throws Exception {
        // Act and Assert
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .request(httpMethod, url)
                .header("Authorization", token);

        if (httpMethod == HttpMethod.POST || httpMethod == HttpMethod.PUT) {
            requestBuilder
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody != null ? requestBody : "");
        }
        mockMvc.perform(requestBuilder)
                .andExpect(status().is(expectedStatus.value()));
    }
}
