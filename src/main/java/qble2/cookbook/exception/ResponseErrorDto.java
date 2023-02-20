package qble2.cookbook.exception;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(Include.NON_NULL)
public class ResponseErrorDto {

  @JsonProperty("status")
  private int status;

  @JsonProperty("error")
  private String error; // technical message

  @JsonProperty("message")
  private String message; // a brief human-readable message

  @JsonProperty(value = "details")
  private List<String> details;

  @JsonProperty(value = "path")
  private String path;

}
