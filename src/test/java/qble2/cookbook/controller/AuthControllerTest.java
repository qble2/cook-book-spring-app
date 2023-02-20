package qble2.cookbook.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import qble2.cookbook.auth.AuthController;
import qble2.cookbook.exception.ExceptionsControllerAdvice;
import qble2.cookbook.security.JwtUtils;
import qble2.cookbook.user.UserRepository;
import qble2.cookbook.user.UserService;
import qble2.cookbook.user.dto.UserDto;
import qble2.cookbook.utils.TestUtils;

@WebMvcTest(controllers = AuthController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:ValidationMessages.properties")
class AuthControllerTest {

  @Autowired
  private Environment env;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private JwtUtils jwtUtils;

  @MockBean
  private UserService userService;

  @MockBean
  private UserRepository userRepository; // called during validation

  @BeforeEach
  void setUp() {}

  /////
  ///// NOMINAL CASES
  /////

  @Test
  void given_validUser_registerUser_willReturnCreatedUser() throws Exception {
    // given
    String urlTemplate = TestUtils.AUTH_PATH + "/signup";
    UserDto userPayload = TestUtils.createUser(null);
    UserDto createdUser = TestUtils.createUserFrom(UUID.randomUUID(), userPayload);
    given(userService.createUser(any())).willReturn(createdUser);

    // when
    // then
    final ResultActions resultActions = this.mockMvc
        .perform(post(urlTemplate).contentType(MediaType.APPLICATION_JSON)
            .accept(MediaTypes.HAL_JSON_VALUE)
            .content(objectMapper.writeValueAsString(userPayload)))
        .andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
        .andExpect(status().isCreated());

    verifyResponse(resultActions, userPayload);
  }

  /////
  ///// NON-NOMINAL CASES
  /////

  @Test
  void given_invalidIdProperty_registerUser_willReturnBadRequest()
      throws JsonProcessingException, Exception {
    // given
    URI uri = TestUtils.toUri(TestUtils.AUTH_PATH + "/signup");
    String urlTemplate = TestUtils.toHttpUriString(uri);
    UserDto userPayload = TestUtils.createUser(UUID.randomUUID());

    // when
    // then
    final ResultActions resultActions = this.mockMvc
        .perform(post(urlTemplate).contentType(MediaType.APPLICATION_JSON)
            .accept(MediaTypes.HAL_JSON_VALUE)
            .content(objectMapper.writeValueAsString(userPayload)))
        .andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));

    TestUtils.verifyResponseError(resultActions, uri, status().isBadRequest(),
        HttpStatus.BAD_REQUEST, ExceptionsControllerAdvice.CONSTRAINT_VIOLATION_MESSAGE,
        env.getProperty("user.id.OnCreate.Null.message"));
  }

  @Test
  void given_missingUsername_registerUser_willReturnBadRequest()
      throws JsonProcessingException, Exception {
    // given
    URI uri = TestUtils.toUri(TestUtils.AUTH_PATH + "/signup");
    String urlTemplate = TestUtils.toHttpUriString(uri);
    UserDto userPayload = TestUtils.createUser(null).setUsername(null);

    // when
    // then
    final ResultActions resultActions = this.mockMvc
        .perform(post(urlTemplate).contentType(MediaType.APPLICATION_JSON)
            .accept(MediaTypes.HAL_JSON_VALUE)
            .content(objectMapper.writeValueAsString(userPayload)))
        .andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));

    TestUtils.verifyResponseError(resultActions, uri, status().isBadRequest(),
        HttpStatus.BAD_REQUEST, ExceptionsControllerAdvice.METHOD_ARGUMENT_NOT_VALID_MESSAGE,
        env.getProperty("user.username.NotBlank.message"));
  }

  @Test
  void given_blankUsername_registerUser_willReturnBadRequest()
      throws JsonProcessingException, Exception {
    // given
    URI uri = TestUtils.toUri(TestUtils.AUTH_PATH + "/signup");
    String urlTemplate = TestUtils.toHttpUriString(uri);
    UserDto userPayload = TestUtils.createUser(null).setUsername("");

    // when
    // then
    final ResultActions resultActions = this.mockMvc
        .perform(post(urlTemplate).contentType(MediaType.APPLICATION_JSON)
            .accept(MediaTypes.HAL_JSON_VALUE)
            .content(objectMapper.writeValueAsString(userPayload)))
        .andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));

    TestUtils.verifyResponseError(resultActions, uri, status().isBadRequest(),
        HttpStatus.BAD_REQUEST, ExceptionsControllerAdvice.METHOD_ARGUMENT_NOT_VALID_MESSAGE,
        env.getProperty("user.username.NotBlank.message"));
  }

  @Test
  void given_usernameAlreadyTaken_registerUser_willReturnBadRequest()
      throws JsonProcessingException, Exception {
    // given
    URI uri = TestUtils.toUri(TestUtils.AUTH_PATH + "/signup");
    String urlTemplate = TestUtils.toHttpUriString(uri);
    UserDto userPayload = TestUtils.createUser(null);
    given(userRepository.existsByUsername(any())).willReturn(true);
    given(userRepository.existsByEmail(any())).willReturn(false);

    // when
    // then
    final ResultActions resultActions = this.mockMvc
        .perform(post(urlTemplate).contentType(MediaType.APPLICATION_JSON)
            .accept(MediaTypes.HAL_JSON_VALUE)
            .content(objectMapper.writeValueAsString(userPayload)))
        .andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));

    TestUtils.verifyResponseError(resultActions, uri, status().isBadRequest(),
        HttpStatus.BAD_REQUEST, ExceptionsControllerAdvice.CONSTRAINT_VIOLATION_MESSAGE,
        env.getProperty("user.username.Taken.message"));
  }

  @Test
  void given_missingPassword_registerUser_willReturnBadRequest()
      throws JsonProcessingException, Exception {
    // given
    URI uri = TestUtils.toUri(TestUtils.AUTH_PATH + "/signup");
    String urlTemplate = TestUtils.toHttpUriString(uri);
    UserDto userPayload = TestUtils.createUser(null).setPassword(null);
    given(userRepository.existsByUsername(any())).willReturn(false);
    given(userRepository.existsByEmail(any())).willReturn(false);

    // when
    // then
    final ResultActions resultActions = this.mockMvc
        .perform(post(urlTemplate).contentType(MediaType.APPLICATION_JSON)
            .accept(MediaTypes.HAL_JSON_VALUE)
            .content(objectMapper.writeValueAsString(userPayload)))
        .andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));

    TestUtils.verifyResponseError(resultActions, uri, status().isBadRequest(),
        HttpStatus.BAD_REQUEST, ExceptionsControllerAdvice.METHOD_ARGUMENT_NOT_VALID_MESSAGE,
        env.getProperty("user.password.NotBlank.message"));
  }

  @Test
  void given_blankPassword_registerUser_willReturnBadRequest()
      throws JsonProcessingException, Exception {
    // given
    URI uri = TestUtils.toUri(TestUtils.AUTH_PATH + "/signup");
    String urlTemplate = TestUtils.toHttpUriString(uri);
    UserDto userPayload = TestUtils.createUser(null).setPassword("");
    given(userRepository.existsByUsername(any())).willReturn(false);
    given(userRepository.existsByEmail(any())).willReturn(false);

    // when
    // then
    final ResultActions resultActions = this.mockMvc
        .perform(post(urlTemplate).contentType(MediaType.APPLICATION_JSON)
            .accept(MediaTypes.HAL_JSON_VALUE)
            .content(objectMapper.writeValueAsString(userPayload)))
        .andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));

    TestUtils.verifyResponseError(resultActions, uri, status().isBadRequest(),
        HttpStatus.BAD_REQUEST, ExceptionsControllerAdvice.METHOD_ARGUMENT_NOT_VALID_MESSAGE,
        env.getProperty("user.password.NotBlank.message"));
  }

  @Test
  void given_missingFirstName_registerUser_willReturnBadRequest()
      throws JsonProcessingException, Exception {
    // given
    URI uri = TestUtils.toUri(TestUtils.AUTH_PATH + "/signup");
    String urlTemplate = TestUtils.toHttpUriString(uri);
    UserDto userPayload = TestUtils.createUser(null).setFirstName(null);
    given(userRepository.existsByUsername(any())).willReturn(false);
    given(userRepository.existsByEmail(any())).willReturn(false);

    // when
    // then
    final ResultActions resultActions = this.mockMvc
        .perform(post(urlTemplate).contentType(MediaType.APPLICATION_JSON)
            .accept(MediaTypes.HAL_JSON_VALUE)
            .content(objectMapper.writeValueAsString(userPayload)))
        .andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));

    TestUtils.verifyResponseError(resultActions, uri, status().isBadRequest(),
        HttpStatus.BAD_REQUEST, ExceptionsControllerAdvice.METHOD_ARGUMENT_NOT_VALID_MESSAGE,
        env.getProperty("user.firstName.NotBlank.message"));
  }

  @Test
  void given_blankFirstName_registerUser_willReturnBadRequest()
      throws JsonProcessingException, Exception {
    // given
    URI uri = TestUtils.toUri(TestUtils.AUTH_PATH + "/signup");
    String urlTemplate = TestUtils.toHttpUriString(uri);
    UserDto userPayload = TestUtils.createUser(null).setFirstName("");
    given(userRepository.existsByUsername(any())).willReturn(false);
    given(userRepository.existsByEmail(any())).willReturn(false);

    // when
    // then
    final ResultActions resultActions = this.mockMvc
        .perform(post(urlTemplate).contentType(MediaType.APPLICATION_JSON)
            .accept(MediaTypes.HAL_JSON_VALUE)
            .content(objectMapper.writeValueAsString(userPayload)))
        .andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));

    TestUtils.verifyResponseError(resultActions, uri, status().isBadRequest(),
        HttpStatus.BAD_REQUEST, ExceptionsControllerAdvice.METHOD_ARGUMENT_NOT_VALID_MESSAGE,
        env.getProperty("user.firstName.NotBlank.message"));
  }

  @Test
  void given_missingLastName_registerUser_willReturnBadRequest()
      throws JsonProcessingException, Exception {
    // given
    URI uri = TestUtils.toUri(TestUtils.AUTH_PATH + "/signup");
    String urlTemplate = TestUtils.toHttpUriString(uri);
    UserDto userPayload = TestUtils.createUser(null).setLastName(null);
    given(userRepository.existsByUsername(any())).willReturn(false);
    given(userRepository.existsByEmail(any())).willReturn(false);

    // when
    // then
    final ResultActions resultActions = this.mockMvc
        .perform(post(urlTemplate).contentType(MediaType.APPLICATION_JSON)
            .accept(MediaTypes.HAL_JSON_VALUE)
            .content(objectMapper.writeValueAsString(userPayload)))
        .andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));

    TestUtils.verifyResponseError(resultActions, uri, status().isBadRequest(),
        HttpStatus.BAD_REQUEST, ExceptionsControllerAdvice.METHOD_ARGUMENT_NOT_VALID_MESSAGE,
        env.getProperty("user.lastName.NotBlank.message"));
  }

  @Test
  void given_blankLastName_registerUser_willReturnBadRequest()
      throws JsonProcessingException, Exception {
    // given
    URI uri = TestUtils.toUri(TestUtils.AUTH_PATH + "/signup");
    String urlTemplate = TestUtils.toHttpUriString(uri);
    UserDto userPayload = TestUtils.createUser(null).setLastName("");
    given(userRepository.existsByUsername(any())).willReturn(false);
    given(userRepository.existsByEmail(any())).willReturn(false);

    // when
    // then
    final ResultActions resultActions = this.mockMvc
        .perform(post(urlTemplate).contentType(MediaType.APPLICATION_JSON)
            .accept(MediaTypes.HAL_JSON_VALUE)
            .content(objectMapper.writeValueAsString(userPayload)))
        .andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));

    TestUtils.verifyResponseError(resultActions, uri, status().isBadRequest(),
        HttpStatus.BAD_REQUEST, ExceptionsControllerAdvice.METHOD_ARGUMENT_NOT_VALID_MESSAGE,
        env.getProperty("user.lastName.NotBlank.message"));
  }

  @Test
  void given_missingEmail_registerUser_willReturnBadRequest()
      throws JsonProcessingException, Exception {
    // given
    URI uri = TestUtils.toUri(TestUtils.AUTH_PATH + "/signup");
    String urlTemplate = TestUtils.toHttpUriString(uri);
    UserDto userPayload = TestUtils.createUser(null).setEmail(null);
    given(userRepository.existsByUsername(any())).willReturn(false);
    given(userRepository.existsByEmail(any())).willReturn(false);

    // when
    // then
    final ResultActions resultActions = this.mockMvc
        .perform(post(urlTemplate).contentType(MediaType.APPLICATION_JSON)
            .accept(MediaTypes.HAL_JSON_VALUE)
            .content(objectMapper.writeValueAsString(userPayload)))
        .andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));

    TestUtils.verifyResponseError(resultActions, uri, status().isBadRequest(),
        HttpStatus.BAD_REQUEST, ExceptionsControllerAdvice.METHOD_ARGUMENT_NOT_VALID_MESSAGE,
        env.getProperty("user.email.NotBlank.message"));
  }

  @Test
  void given_blankEmail_registerUser_willReturnBadRequest()
      throws JsonProcessingException, Exception {
    // given
    URI uri = TestUtils.toUri(TestUtils.AUTH_PATH + "/signup");
    String urlTemplate = TestUtils.toHttpUriString(uri);
    UserDto userPayload = TestUtils.createUser(null).setEmail("");
    given(userRepository.existsByUsername(any())).willReturn(false);
    given(userRepository.existsByEmail(any())).willReturn(false);

    // when
    // then
    final ResultActions resultActions = this.mockMvc
        .perform(post(urlTemplate).contentType(MediaType.APPLICATION_JSON)
            .accept(MediaTypes.HAL_JSON_VALUE)
            .content(objectMapper.writeValueAsString(userPayload)))
        .andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));

    TestUtils.verifyResponseError(resultActions, uri, status().isBadRequest(),
        HttpStatus.BAD_REQUEST, ExceptionsControllerAdvice.METHOD_ARGUMENT_NOT_VALID_MESSAGE,
        env.getProperty("user.email.NotBlank.message"));
  }

  @Test
  void given_emailAlreadyTaken_registerUser_willReturnBadRequest()
      throws JsonProcessingException, Exception {
    // given
    URI uri = TestUtils.toUri(TestUtils.AUTH_PATH + "/signup");
    String urlTemplate = TestUtils.toHttpUriString(uri);
    UserDto userPayload = TestUtils.createUser(null);
    given(userRepository.existsByUsername(any())).willReturn(false);
    given(userRepository.existsByEmail(any())).willReturn(true);

    // when
    // then
    final ResultActions resultActions = this.mockMvc
        .perform(post(urlTemplate).contentType(MediaType.APPLICATION_JSON)
            .accept(MediaTypes.HAL_JSON_VALUE)
            .content(objectMapper.writeValueAsString(userPayload)))
        .andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));

    TestUtils.verifyResponseError(resultActions, uri, status().isBadRequest(),
        HttpStatus.BAD_REQUEST, ExceptionsControllerAdvice.CONSTRAINT_VIOLATION_MESSAGE,
        env.getProperty("user.email.Taken.message"));
  }

  @Test
  void given_usernameAndEmailAlreadyTaken_registerUser_willReturnBadRequest()
      throws JsonProcessingException, Exception {
    // given
    URI uri = TestUtils.toUri(TestUtils.AUTH_PATH + "/signup");
    String urlTemplate = TestUtils.toHttpUriString(uri);
    UserDto userPayload = TestUtils.createUser(null);
    given(userRepository.existsByUsername(any())).willReturn(true);
    given(userRepository.existsByEmail(any())).willReturn(true);

    // when
    // then
    final ResultActions resultActions = this.mockMvc
        .perform(post(urlTemplate).contentType(MediaType.APPLICATION_JSON)
            .accept(MediaTypes.HAL_JSON_VALUE)
            .content(objectMapper.writeValueAsString(userPayload)))
        .andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));

    TestUtils.verifyResponseError(resultActions, uri, status().isBadRequest(),
        HttpStatus.BAD_REQUEST, ExceptionsControllerAdvice.CONSTRAINT_VIOLATION_MESSAGE,
        env.getProperty("user.username.Taken.message"),
        env.getProperty("user.email.Taken.message"));
  }

  /////
  /////
  /////

  private void verifyResponse(final ResultActions resultActions, UserDto user) throws Exception {
    resultActions.andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.username", is(user.getUsername())))
        .andExpect(jsonPath("$.password").doesNotExist())
        .andExpect(jsonPath("$.firstName", is(user.getFirstName())))
        .andExpect(jsonPath("$.lastName", is(user.getLastName())))
        .andExpect(jsonPath("$.email", is(user.getEmail())));
  }

}
