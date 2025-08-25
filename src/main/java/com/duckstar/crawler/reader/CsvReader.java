package com.duckstar.crawler.reader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Component
public class CsvReader {

    public <T> List<T> readCsv(String filePath, Function<CSVRecord, T> mapper) {
        try (
                Reader reader = Files.newBufferedReader(Paths.get(filePath));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())
        ) {
            List<T> result = new ArrayList<>();
            for (CSVRecord record : csvParser) {
                result.add(mapper.apply(record));
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException("CSV read error: " + filePath, e);
        }
    }
}
