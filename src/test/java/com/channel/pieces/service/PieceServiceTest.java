package com.channel.pieces.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.channel.pieces.domains.Piece;
import com.channel.pieces.domains.PieceType;
import com.channel.pieces.dto.PieceResponse;
import com.channel.pieces.repository.PieceRepository;

@ExtendWith(MockitoExtension.class)
class PieceServiceTest {

    @Mock
    private PieceRepository pieceRepository;

    @InjectMocks
    private PieceService pieceService;

    private Piece watchPiece;

    @BeforeEach
    public void setUp() {
        watchPiece = new Piece();
        watchPiece.setId(1L);
        watchPiece.setSerialNumber("SN0001");
        watchPiece.setReference("J12");
        watchPiece.setSize("38MM");
        watchPiece.setType(PieceType.WATCH);
        watchPiece.setProductionDate(LocalDate.of(2019,5,12));       
    }

    @Test
    void findAllWithoutFiltersReturnsAllPieces() {
        when(pieceRepository.findByFilters(null, null, null))
            .thenReturn(List.of(watchPiece));

        List<PieceResponse> pieces = pieceService.findAll(null, null, null);
        assertThat(pieces).hasSize(1);
        assertThat(pieces.get(0).id()).isEqualTo(1L);
        assertThat(pieces.get(0).type()).isEqualTo(PieceType.WATCH);
        assertThat(pieces.get(0).reference()).isEqualTo("J12");
    }

    @Test
    void findAllWithTypeFilterReturnsFilteredPieces() {
        when(pieceRepository.findByFilters(PieceType.WATCH, null, null))
                .thenReturn(List.of(watchPiece));

        List<PieceResponse> result = pieceService.findAll(PieceType.WATCH, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).type()).isEqualTo(PieceType.WATCH);
    }

    @Test
    void findByIdWhenExistsReturnsPiece() {
        when(pieceRepository.findById(1L)).thenReturn(Optional.of(watchPiece));

        PieceResponse result = pieceService.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.serialNumber()).isEqualTo("SN0001");
    }

    @Test
    void findByIdWhenNotExistsThrowsNoSuchElementException() {
        when(pieceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pieceService.findById(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("999");
    }

    @Test
    void findByIdWhenInvalidIdThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> pieceService.findById(0L))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> pieceService.findById(-1L))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> pieceService.findById(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
