package org.example.databasetesting.utils;
import com.opencsv.CSVReader;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class CSVUtil {

    public static <T> List<T> parseCSV(MultipartFile file, Class<T> clazz) {
        List<T> records = new ArrayList<>();

        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] headers = csvReader.readNext();
            if (headers == null) {
                throw new IllegalArgumentException("CSV file is empty!");
            }

            String[] values;
            while ((values = csvReader.readNext()) != null) {
                T instance = clazz.getDeclaredConstructor().newInstance();
                for (int i = 0; i < headers.length; i++) {
                    String header = headers[i];
                    String value = i < values.length ? values[i] : null;
                    Field field = clazz.getDeclaredField(header);
                    field.setAccessible(true);

                    Object parsedValue = parseValue(value, field.getType());
                    field.set(instance, parsedValue);
                }
                records.add(instance);
            }
        } catch (Exception e) {
            throw new RuntimeException("An error has occurred while processing file: " + e.getMessage(), e);
        }
        return records;
    }

    private static Object parseValue(String value, Class<?> fieldType) {
        if (value == null) {
            return null;
        }
        if (fieldType == String.class) {
            return value;
        } else if (fieldType == int.class || fieldType == Integer.class) {
            return Integer.parseInt(value);
        } else if (fieldType == double.class || fieldType == Double.class) {
            return Double.parseDouble(value);
        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
            return Boolean.parseBoolean(value);
        }

        return null;
    }
}
