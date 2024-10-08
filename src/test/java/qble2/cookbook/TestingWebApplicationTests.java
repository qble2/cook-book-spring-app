package qble2.cookbook;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

// In this test, the full Spring application context is started but without the server
@SpringBootTest
@AutoConfigureMockMvc
class TestingWebApplicationTests {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void shouldReturnUnauthorized() throws Exception {
    this.mockMvc.perform(get("/")).andDo(print()).andExpect(status().isUnauthorized());
  }

}
