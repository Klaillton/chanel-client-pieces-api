package com.channel.pieces.infrastructure;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.channel.pieces.domains.Piece;
import com.channel.pieces.domains.PieceType;
import com.channel.pieces.repository.PieceRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CsvPieceLoader implements ApplicationRunner {

    private static final String CSV_FILE_PATH = "pieces.csv";
    private static final Logger logger = LoggerFactory.getLogger(CsvPieceLoader.class);
    private static final String CSV_SEPARATOR = ";";

    private final PieceRepository pieceRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if(pieceRepository.count() > 0) {
            logger.info("Pieces already exist in the list. Skipping CSV loading.");
            return;
        }

        AtomicInteger loaded = new AtomicInteger();
        AtomicInteger skipped = new AtomicInteger();

        try (Reader reader = new InputStreamReader(
            new ClassPathResource(CSV_FILE_PATH).getInputStream(), StandardCharsets.UTF_8)) {
            
                CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setDelimiter(CSV_SEPARATOR)
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .build();

                for (CSVRecord pieceRecord : format.parse(reader)) {
                    checkRecord(loaded, skipped, pieceRecord);
                }

            } catch (Exception e) {
                logger.warn("Error loading CSV data: {}", e.getMessage());
            }
            logger.info("CSV data loading completed. Loaded: {}, Skipped: {}", loaded.get(), skipped.get());
        }

        private void checkRecord(AtomicInteger loaded, AtomicInteger skipped, CSVRecord pieceRecord) {
            try {
                Piece piece = mapRecordToPiece(pieceRecord);
                pieceRepository.save(piece);
                loaded.incrementAndGet();
            } catch (Exception e) {
                logger.warn("Error processing record: {}. Error: {}", pieceRecord, e.getMessage());
                skipped.incrementAndGet();
            }
        }

    private Piece mapRecordToPiece(CSVRecord csvRecord) {
        String serialNumber = requiredField(csvRecord, 0, "serial_number");
        String reference = requiredField(csvRecord, 1, "reference");
        String size = requiredField(csvRecord, 2, "size");
        String type = requiredField(csvRecord, 3, "type");
        String productionDate = requiredField(csvRecord, 4, "production_date");
        

        Piece piece = new Piece();
        piece.setSerialNumber(serialNumber);
        piece.setReference(reference);
        piece.setSize(size);
        piece.setType(PieceType.fromCsv(type));
        piece.setProductionDate(parseDate(productionDate));
        
        return piece;
    }

    private String requiredField(CSVRecord record, int index, String fieldName) {
        String value = record.get(index);
        if(value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required field: " + fieldName);
        }
        return value.trim();
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format: " + dateStr);
        }
    }

}
