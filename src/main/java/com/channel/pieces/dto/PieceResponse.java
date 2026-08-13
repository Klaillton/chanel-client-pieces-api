package com.channel.pieces.dto;

import com.channel.pieces.domains.PieceType;

public record PieceResponse(
    Long id,
    PieceType type,
    String serialNumber,
    String size,
    String productionDate,
    String reference
) {

}
