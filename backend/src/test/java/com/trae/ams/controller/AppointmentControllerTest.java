package com.trae.ams.controller;

import com.trae.ams.common.context.UserContext;
import com.trae.ams.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AppointmentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private AppointmentController appointmentController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(appointmentController).build();
    }

    @Test
    public void testListMy_Returns200() throws Exception {
        // Mock UserContext behavior if needed, but UserContext is static.
        // In standalone setup, filters/interceptors are not applied by default unless configured.
        // AppointmentController uses UserContext.getUserId(), which reads from ThreadLocal.
        // We might need to set it manually if the controller logic depends on it heavily before calling service.
        
        // However, the controller logic:
        // Long userId = UserContext.getUserId();
        // if (userId == null) userId = 1L;
        // This is safe even if UserContext is empty.

        mockMvc.perform(get("/appointments/me")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk());
    }
}
