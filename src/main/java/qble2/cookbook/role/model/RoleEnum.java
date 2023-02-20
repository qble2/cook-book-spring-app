package qble2.cookbook.role.model;

import com.fasterxml.jackson.annotation.JsonProperty;

// @JsonFormat(shape = JsonFormat.Shape.STRING)
public enum RoleEnum {

  @JsonProperty("ROLE_USER")
  ROLE_USER,

  @JsonProperty("ROLE_ADMIN")
  ROLE_ADMIN

  ;

}
