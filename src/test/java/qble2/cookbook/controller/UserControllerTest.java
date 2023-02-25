package qble2.cookbook.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import qble2.cookbook.exception.ResourceNotFoundException;
import qble2.cookbook.user.UserController;
import qble2.cookbook.user.UserService;
import qble2.cookbook.user.dto.UserDto;
import qble2.cookbook.user.dto.UsersPageDto;
import qble2.cookbook.utils.TestUtils;

@WebMvcTest(controllers = UserController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ActiveProfiles("test")
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserService userService;

  /////
  ///// NOMINAL CASES
  /////

  @Test
  void given_none_getUsers_willReturnUsers() throws Exception {
    // given
    URI uri = TestUtils.toUri(TestUtils.USERS_PATH);
    String urlTemplate = TestUtils.toHttpUriString(uri);
    int page = 0;
    int size = 5;
    UsersPageDto usersPageDto =
        UsersPageDto.builder().users(List.of(TestUtils.createUser(UUID.randomUUID()))).build();
    given(userService.getUsers(PageRequest.of(page, size))).willReturn(usersPageDto);

    // when
    // then
    String selfLink = TestUtils.toHttpUriString(TestUtils.USERS_PATH,
        new TreeMap<>(Map.of("page", page, "size", size)));

    this.mockMvc.perform(get(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)).andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
        .andExpect(status().isOk()) //
        .andExpect(jsonPath("$._links.self.href", is(selfLink)));
  }

  @Test
  void given_userExists_getUser_willReturnUser() throws Exception {
    // given
    UserDto existingUser = TestUtils.createUser(UUID.randomUUID());
    URI uri = TestUtils.toUri(TestUtils.USERS_PATH + "{userId}", existingUser.getId());
    String urlTemplate = TestUtils.toHttpUriString(uri);
    given(userService.getUser(any())).willReturn(existingUser);

    // when
    // then
    this.mockMvc.perform(get(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)).andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
        .andExpect(status().isOk()) //
        .andExpect(jsonPath("$.id", is(existingUser.getId().toString())))
        .andExpect(jsonPath("$.password").doesNotExist());
  }

  /////
  ///// NON-NOMINAL CASES
  /////

  @Test
  void given_userDoesNotExist_getUser_willReturnNotFound() throws Exception {
    // given
    UUID unknownUserId = UUID.randomUUID();
    URI uri = TestUtils.toUri(TestUtils.USERS_PATH + "/{userId}", unknownUserId);
    String urlTemplate = TestUtils.toHttpUriString(uri);
    given(userService.getUser(any())).willThrow(new ResourceNotFoundException());

    // when
    // then
    final ResultActions resultActions =
        this.mockMvc.perform(get(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)).andDo(print())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));

    TestUtils.verifyResponseError(resultActions, uri, status().isNotFound(), HttpStatus.NOT_FOUND,
        ResourceNotFoundException.getFormattedMessage());
  }

  /////
  /////
  /////

}
