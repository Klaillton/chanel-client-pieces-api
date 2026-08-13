package com.channel.pieces.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.channel.pieces.domains.Piece;
import com.channel.pieces.domains.PieceType;
import com.channel.pieces.dto.PieceResponse;
import com.channel.pieces.repository.PieceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PieceService {

    private final PieceRepository pieceRepository;

    public List<PieceResponse> findAll(PieceType type, String reference, String size) {
        return pieceRepository.findByFilters(type, reference, size)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public PieceResponse findById(Long id) {
        if(id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid id: " + id);
        }

        return pieceRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new NoSuchElementException("Piece not found with id: " + id));
    }

    private PieceResponse toResponse(Piece piece) {
        return new PieceResponse(
            piece.getId(),
            piece.getType(),
            piece.getSerialNumber(),
            piece.getSize(),
            piece.getProductionDate() != null ? piece.getProductionDate().toString() : null,
            piece.getReference()
        );
    }

}
