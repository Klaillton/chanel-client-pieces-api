package com.channel.pieces.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.channel.pieces.domains.Piece;
import com.channel.pieces.domains.PieceType;

@Repository
public interface PieceRepository {

    Piece save(Piece piece);

    long count();

    List<Piece> findAll();

    List<Piece> findByFilters(PieceType type, String reference, String size);

}
