package com.channel.pieces.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.channel.pieces.domains.PieceType;
import com.channel.pieces.dto.PieceResponse;
import com.channel.pieces.exceptions.GlobalExceptionHandler;
import com.channel.pieces.service.PieceService;

@WebMvcTest(PiecesController.class)
@Import(GlobalExceptionHandler.class)
class PiecesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PieceService pieceService;

    @Test
    void findAllReturns200() throws Exception {
        PieceResponse response = new PieceResponse(1L, PieceType.WATCH, "SN0001", "38MM", "2019-05-12", "J12");

        when(pieceService.findAll(null, null, null)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/pieces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].type").value("WATCH"))
                .andExpect(jsonPath("$[0].reference").value("J12"));
    }

    @Test
    void findAllWithTypeFiltersReturns200() throws Exception {
        PieceResponse response = new PieceResponse(1L, PieceType.WATCH, "SN0001", "38MM", "2019-05-12", "J12");

        when(pieceService.findAll(PieceType.WATCH, null, null)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/pieces?type=WATCH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].type").value("WATCH"))
                .andExpect(jsonPath("$[0].reference").value("J12"));
    }

    @Test
    void findByIdWhenExistsReturns200() throws Exception {
        PieceResponse response = new PieceResponse(1L, PieceType.WATCH, "SN0001", "38MM", "2019-05-12", "J12");

        when(pieceService.findById(1L)).thenReturn(response);
        mockMvc.perform(get("/api/pieces/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.type").value("WATCH"))
                .andExpect(jsonPath("$.reference").value("J12"));
    }

    @Test
    void findByIdWhenNotExistsReturns404() throws Exception {
        when(pieceService.findById(999L)).thenThrow(new NoSuchElementException("Piece not found with id: 999"));
        mockMvc.perform(get("/api/pieces/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Piece not found with id: 999"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void findByIdWhenInvalidIdReturns400() throws Exception {
        when(pieceService.findById(-1L)).thenThrow(new IllegalArgumentException("Invalid piece ID: -1"));
        mockMvc.perform(get("/api/pieces/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid piece ID: -1"))
                .andExpect(jsonPath("$.status").value(400));
    }
}
