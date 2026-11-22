package com.athenet.statistics.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.athenet.statistics.config.CorsConfig;
import com.athenet.statistics.model.ScoreDocument;
import com.athenet.statistics.repository.ScoreRepository;

@WebMvcTest(ScoreController.class)
@Import(CorsConfig.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:5173")
class ScoreControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean ScoreRepository repositorio;

    @Test
    void actualDevuelveEncuentroEnCursoO204() throws Exception {
        ScoreDocument doc = new ScoreDocument("mongo-1", 31L, "USACH", "U. de Chile", 2, 1, "PUNTO");
        when(repositorio.findFirstByTipoNot("FINALIZADO")).thenReturn(Optional.of(doc));

        mvc.perform(get("/marcador/actual").header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(jsonPath("$.idEncuentro").value(31))
                .andExpect(jsonPath("$.puntosLocal").value(2))
                .andExpect(jsonPath("$.puntosVisita").value(1))
                .andExpect(jsonPath("$.tipo").value("PUNTO"));

        when(repositorio.findFirstByTipoNot("FINALIZADO")).thenReturn(Optional.empty());
        mvc.perform(get("/marcador/actual"))
                .andExpect(status().isNoContent());
    }

    @Test
    void porEncuentroDevuelveMarcadorDelPartidoO204() throws Exception {
        ScoreDocument doc = new ScoreDocument("mongo-2", 42L, "PUC", "UTFSM", 1, 3, "FINALIZADO");
        when(repositorio.findByIdEncuentro(42L)).thenReturn(Optional.of(doc));

        mvc.perform(get("/marcador/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEncuentro").value(42))
                .andExpect(jsonPath("$.equipoLocal").value("PUC"))
                .andExpect(jsonPath("$.tipo").value("FINALIZADO"));

        when(repositorio.findByIdEncuentro(99L)).thenReturn(Optional.empty());
        mvc.perform(get("/marcador/99"))
                .andExpect(status().isNoContent());
    }
}
