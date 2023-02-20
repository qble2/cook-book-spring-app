package cookbook.integration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import com.fasterxml.jackson.databind.ObjectMapper;
import cookbook.utils.TestUtils;
import qble2.cookbook.user.UserRepository;
import qble2.cookbook.user.dto.UserDto;
import qble2.cookbook.user.model.User;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@EnableAutoConfiguration(
    exclude = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@ActiveProfiles("test")
public class AuthControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @Test
  void given_validUser_registerUser_willReturnCreatedUser() throws Exception {
    // given
    UserDto userPayload = TestUtils.createUser(null);

    URI uri = TestUtils.toUri(TestUtils.AUTH_PATH + "/signup");
    String urlTemplate = TestUtils.toHttpUriString(uri);

    // when
    // then
    final ResultActions resultActions = this.mockMvc
        .perform(post(urlTemplate).contentType(MediaType.APPLICATION_JSON)
            .accept(MediaTypes.HAL_JSON_VALUE)
            .content(objectMapper.writeValueAsString(userPayload)))
        .andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
        .andExpect(status().isCreated()).andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.username", is(userPayload.getUsername())))
        .andExpect(jsonPath("$.password").doesNotExist())
        .andExpect(jsonPath("$.firstName", is(userPayload.getFirstName())))
        .andExpect(jsonPath("$.lastName", is(userPayload.getLastName())))
        .andExpect(jsonPath("$.email", is(userPayload.getEmail())));

    User user = userRepository.findByUsername(userPayload.getUsername()).get();
    TestUtils.verifyUserLinks(resultActions, user.getId());
  }

}
