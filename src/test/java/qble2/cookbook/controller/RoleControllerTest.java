package qble2.cookbook.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.net.URI;
import java.util.UUID;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import qble2.cookbook.exception.ExceptionsControllerAdvice;
import qble2.cookbook.exception.ResourceNotFoundException;
import qble2.cookbook.role.RoleController;
import qble2.cookbook.role.RoleRepository;
import qble2.cookbook.role.RoleService;
import qble2.cookbook.role.dto.RoleDto;
import qble2.cookbook.role.model.RoleEnum;
import qble2.cookbook.utils.TestUtils;

@WebMvcTest(controllers = RoleController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:ValidationMessages.properties")
class RoleControllerTest {

  @Autowired
  private Environment env;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private RoleService roleService;

  @MockBean
  private RoleRepository roleRepository; // called during Validation

  /////
  ///// NOMINAL CASES
  /////

  @Test
  void given_none_getRoles_willReturnRoles() throws Exception {
    // given
    URI uri = TestUtils.toUri(TestUtils.ROLES_PATH);
    String urlTemplate = TestUtils.toHttpUriString(uri);

    // when
    // then
    String selfLink = urlTemplate;

    this.mockMvc.perform(get(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)).andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
        .andExpect(status().isOk()) //
        .andExpect(jsonPath("$._links.self.href", is(selfLink)));
  }

  @Test
  void given_roleExists_getRole_willReturnRole() throws Exception {
    // given
    RoleDto existingRole = TestUtils.createRole(UUID.randomUUID(), RoleEnum.ROLE_USER);
    URI uri = TestUtils.toUri(TestUtils.ROLES_PATH + "/{roleId}", existingRole.getId());
    String urlTemplate = TestUtils.toHttpUriString(uri);
    given(roleService.getRole(any())).willReturn(existingRole);

    // when
    // then
    // String selfLink = urlTemplate;

    this.mockMvc.perform(get(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)).andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
        .andExpect(status().isOk()) //
        .andExpect(jsonPath("$.id", is(existingRole.getId().toString())))
        .andExpect(jsonPath("$.name", is(existingRole.getName().toString())))
    // TODO BKE links created with MapStruct @AfterMapping are not generated in testing context
    // .andExpect(jsonPath("$._links.self.href", is(selfLink)))
    ;
  }

  @Test
  void given_validRole_createRole_willReturnCreatedRole() throws Exception {
    // given
    URI uri = TestUtils.toUri(TestUtils.ROLES_PATH);
    String urlTemplate = TestUtils.toHttpUriString(uri);
    RoleDto rolePayload = TestUtils.createRole(null, RoleEnum.ROLE_USER);
    RoleDto createdRole = TestUtils.createRoleFrom(UUID.randomUUID(), rolePayload);
    given(roleService.createRole(any())).willReturn(createdRole);

    // when
    // then
    // String selfLink =
    // TestUtils.toHttpUriString(TestUtils.ROLE_PATH + "/{roleId}", createdRole.getId()).toString();

    this.mockMvc
        .perform(post(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(rolePayload)))
        .andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(createdRole.getId().toString())))
        .andExpect(jsonPath("$.name", is(createdRole.getName().toString())))
    // TODO BKE links created with MapStruct @AfterMapping are not generated in testing context
    // .andExpect(jsonPath("$._links.self.href", is(selfLink)))
    ;
  }

  /////
  ///// NON-NOMINAL CASES
  /////

  @Test
  void given_roleDoesNotExist_getRole_willReturnResourceNotFound() throws Exception {
    // given
    UUID unknownRoleId = UUID.randomUUID();
    URI uri = TestUtils.toUri(TestUtils.ROLES_PATH + "/{roleId}", unknownRoleId);
    String urlTemplate = TestUtils.toHttpUriString(uri);
    given(roleService.getRole(any())).willThrow(new ResourceNotFoundException());

    // when
    // then
    final ResultActions resultActions =
        this.mockMvc.perform(get(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)).andDo(print())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));

    TestUtils.verifyResponseError(resultActions, uri, status().isNotFound(), HttpStatus.NOT_FOUND,
        ResourceNotFoundException.getFormattedMessage());
  }

  @Test
  void given_invalidIdProperty_createRole_willReturnBadRequest() throws Exception {
    // given
    RoleDto rolePayload = TestUtils.createRole(UUID.randomUUID(), RoleEnum.ROLE_USER);
    URI uri = TestUtils.toUri(TestUtils.ROLES_PATH);
    String urlTemplate = TestUtils.toHttpUriString(uri);

    // when
    // then
    final ResultActions resultActions = this.mockMvc
        .perform(post(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(rolePayload)))
        .andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));

    TestUtils.verifyResponseError(resultActions, uri, status().isBadRequest(),
        HttpStatus.BAD_REQUEST, ExceptionsControllerAdvice.CONSTRAINT_VIOLATION_MESSAGE,
        env.getProperty("role.id.OnCreate.Null.message"));
  }

  @Test
  void given_nameAlreadyExists_createRole_willReturnBadRequest() throws Exception {
    // given
    URI uri = TestUtils.toUri(TestUtils.ROLES_PATH);
    String urlTemplate = TestUtils.toHttpUriString(uri);
    RoleDto rolePayload = TestUtils.createRole(null, RoleEnum.ROLE_USER);
    given(roleRepository.existsByName(rolePayload.getName())).willReturn(true);

    // when
    // then
    final ResultActions resultActions = this.mockMvc
        .perform(post(urlTemplate).accept(MediaTypes.HAL_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(rolePayload)))
        .andDo(print())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));

    TestUtils.verifyResponseError(resultActions, uri, status().isBadRequest(),
        HttpStatus.BAD_REQUEST, ExceptionsControllerAdvice.CONSTRAINT_VIOLATION_MESSAGE,
        env.getProperty("role.name.Taken.message"));
  }

  /////
  /////
  /////

}
