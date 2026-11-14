package com.learning.springbootdemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Regression: page=2147483647 used to reach Spring Data and fail with a 500 (offset overflow). It must be a 400. */
@WebMvcTest(SoftwareEngineerController.class)
class SoftwareEngineerControllerPagingTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    SoftwareEngineerService service;

    @Test
    void hugePageIsRejectedWith400() throws Exception {
        mvc.perform(get("/api/v1/software-engineers").param("page", "2147483647"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/software-engineers").param("page", "2147483647").param("size", "100"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/software-engineers").param("page", "100001"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void pageUpToTheCapIsAccepted() throws Exception {
        when(service.getSoftwareEngineersPage(100_000, 100)).thenReturn(List.of());
        mvc.perform(get("/api/v1/software-engineers").param("page", "100000").param("size", "100"))
                .andExpect(status().isOk());
    }
}
