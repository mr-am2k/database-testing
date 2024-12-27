package org.example.databasetesting.utils;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.RFC4180Parser;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CSVUtilBatches {
    public static <T> void parseCSVInBatches(MultipartFile file, Class<T> clazz,
                                             Consumer<List<T>> batchProcessor, int batchSize) {
        // Use BufferedReader for efficient reading
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            // Configure CSV parser with optimal settings
            RFC4180Parser parser = new RFC4180Parser();
            CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withCSVParser(parser)
                    .build();

            // Read headers once
            String[] headers = csvReader.readNext();
            if (headers == null) {
                throw new IllegalArgumentException("CSV file is empty!");
            }

            List<T> batch = new ArrayList<>(batchSize);
            String[] values;

            // Process the file in batches
            while ((values = csvReader.readNext()) != null) {
                T instance = createInstance(clazz, headers, values);
                batch.add(instance);

                // When batch is full, process it and create new batch
                if (batch.size() >= batchSize) {
                    batchProcessor.accept(batch);
                    batch = new ArrayList<>(batchSize);
                }
            }

            // Process any remaining records
            if (!batch.isEmpty()) {
                batchProcessor.accept(batch);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error processing CSV file: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a single instance of the target class from a row of CSV data
     */
    private static <T> T createInstance(Class<T> clazz, String[] headers, String[] values)
            throws Exception {
        T instance = clazz.getDeclaredConstructor().newInstance();

        for (int i = 0; i < headers.length; i++) {
            if (i < values.length && values[i] != null && !values[i].isEmpty()) {
                Field field = clazz.getDeclaredField(headers[i]);
                field.setAccessible(true);
                field.set(instance, parseValue(values[i], field.getType()));
            }
        }

        return instance;
    }

    private static Object parseValue(String value, Class<?> fieldType) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            if (fieldType == String.class) {
                return value;
            } else if (fieldType == Integer.class || fieldType == int.class) {
                return Integer.parseInt(value.trim());
            } else if (fieldType == Double.class || fieldType == double.class) {
                return Double.parseDouble(value.trim());
            } else if (fieldType == Boolean.class || fieldType == boolean.class) {
                return Boolean.parseBoolean(value.trim());
            }
        } catch (NumberFormatException e) {
            // Return null for unparseable values instead of throwing exception
            return null;
        }

        return null;
    }
}