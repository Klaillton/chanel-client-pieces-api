package com.channel.pieces.repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.channel.pieces.domains.Piece;
import com.channel.pieces.domains.PieceType;

@Repository
public class InMemoryPieceRepository implements PieceRepository {

    private final List<Piece> pieces = new CopyOnWriteArrayList<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public Piece save(Piece piece) {
        Piece value = Objects.requireNonNull(piece, "piece must not be null");
        if(value.getId() == null) {
            value.setId(sequence.getAndIncrement());
        }
        pieces.add(value);
        return value;
    }

    @Override
    public long count(){
        return pieces.size();
    }
    
    @Override
    public List<Piece> findAll() {
        return new ArrayList<>(pieces);
    }

    public List<Piece> findByFilters(PieceType type, String reference, String size) {
                return pieces.stream()
                .filter(piece -> type == null || piece.getType() == type)
                .filter(piece -> reference == null || reference.isBlank()
            || reference.equalsIgnoreCase(piece.getReference()))
                .filter(piece -> size == null || size.isBlank()
            || size.equalsIgnoreCase(piece.getSize()))
            .toList();
    }
}