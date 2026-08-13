package com.channel.pieces.domains;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class Piece {
    
    private Long id;
    private PieceType type;
    private String serialNumber;
    private String size;
    private LocalDate productionDate;
    private String reference;

}
