package co.eci.blacklist.api;

import co.eci.blacklist.BlacklistApiApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BlacklistApiApplication.class)
@AutoConfigureMockMvc
class BlacklistControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void shouldReturn200ForValidIPv4() throws Exception {
        mockMvc.perform(get("/api/v1/blacklist/check")
                        .param("ip", "200.24.34.55")
                        .param("threads", "4"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400ForInvalidIPv4() throws Exception {
        mockMvc.perform(get("/api/v1/blacklist/check")
                        .param("ip", "999.999.999.999"))
                .andExpect(status().isBadRequest());
    }
}
