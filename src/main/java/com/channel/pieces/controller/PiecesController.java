package com.channel.pieces.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.channel.pieces.domains.PieceType;
import com.channel.pieces.dto.PieceResponse;
import com.channel.pieces.service.PieceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pieces")
@RequiredArgsConstructor
public class PiecesController {

    private final PieceService pieceService;

    @GetMapping
    public ResponseEntity<List<PieceResponse>> findAll(
        @RequestParam(required = false) PieceType type,
        @RequestParam(required = false) String reference,
        @RequestParam(required = false) String size
    ) {

        List<PieceResponse> pieces = pieceService.findAll(type, reference, size);

        return ResponseEntity.ok(pieces);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PieceResponse> findById(@PathVariable Long id) {
        PieceResponse piece = pieceService.findById(id);
        return ResponseEntity.ok(piece);
    }

}
