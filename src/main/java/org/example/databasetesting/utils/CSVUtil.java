package org.example.databasetesting.utils;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.time.format.DateTimeFormatter;

public class CSVUtil {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String RESULTS_CSV_FILENAME = "insert.csv";
    private static final String RESOURCES_PATH = "src/main/resources/results";
    private static final String[] CSV_HEADERS = {
            "databaseType", "numberOfRecords", "batchSize", "caching",
            "numberOfThreads", "queryType", "executionTime", "ramUsage", "cpuUsage"
    };

    private static final String READ_RESULTS_CSV_FILENAME = "read.csv";
    private static final String[] READ_CSV_HEADERS = {
            "databaseType", "numberOfRecords", "caching", "queryVersion",
            "queryType", "indexing", "executionTime", "ramUsage", "cpuUsage"
    };

    private static final String UPDATE_RESULTS_CSV_FILENAME = "update.csv";
    private static final String[] UPDATE_CSV_HEADERS = {
            "databaseType", "recordsInDatabase", "numberOfRecords", "caching",
            "queryType", "executionTime", "ramUsage", "cpuUsage"
    };

    private static final String DELETE_RESULTS_CSV_FILENAME = "delete.csv";
    private static final String[] DELETE_CSV_HEADERS = {
            "databaseType", "recordsInDatabase", "numberOfRecords", "caching",
            "queryType", "executionTime", "ramUsage", "cpuUsage"
    };

    private static final String ANALYTICAL_RESULTS_CSV_FILENAME = "analytical.csv";
    private static final String[] ANALYTICAL_CSV_HEADERS = {
            "databaseType", "numberOfRecords", "queryName", "caching", "executionTime", "ramUsage", "cpuUsage"
    };

    public static <T> List<List<T>> parseCSV(MultipartFile file, Class<T> clazz, int batchSize) {
        List<List<T>> batches = new ArrayList<>();

        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] headers = csvReader.readNext();
            if (headers == null) {
                throw new IllegalArgumentException("CSV file is empty!");
            }

            List<T> allRecords = new ArrayList<>();
            String[] values;

            // Read all records into memory
            while ((values = csvReader.readNext()) != null) {
                T instance = createInstance(clazz, headers, values);
                allRecords.add(instance);
            }

            // Split into batches
            for (int i = 0; i < allRecords.size(); i += batchSize) {
                int end = Math.min(i + batchSize, allRecords.size());
                batches.add(allRecords.subList(i, end));
            }

            return batches;
        } catch (Exception e) {
            throw new RuntimeException("An error has occurred while processing file: " + e.getMessage(), e);
        }
    }

    private static <T> T createInstance(Class<T> clazz, String[] headers, String[] values) throws Exception {
        T instance = clazz.getDeclaredConstructor().newInstance();
        for (int i = 0; i < headers.length; i++) {
            String header = headers[i];
            String value = i < values.length ? values[i] : null;
            Field field = clazz.getDeclaredField(header);
            field.setAccessible(true);

            Object parsedValue = parseValue(value, field.getType());
            field.set(instance, parsedValue);
        }
        return instance;
    }

    private static Object parseValue(String value, Class<?> fieldType) {
        if (value == null || value.isBlank()) {
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
        } else if (fieldType == LocalDate.class) {  // ✅ Handle LocalDate
            return LocalDate.parse(value, DATE_FORMATTER);
        }
        return null;
    }

    /**
     * Saves database operation results to a CSV file in the resources directory.
     *
     * @param databaseType The type of database (e.g., POSTGRESQL, MONGODB)
     * @param numberOfRecords Total number of records processed
     * @param batchSize Size of each processing batch
     * @param caching Caching strategy used
     * @param numberOfThreads Number of threads used for processing
     * @param queryType Type of query executed
     * @param executionTime Total execution time in milliseconds
     * @param ramUsage Peak RAM usage
     * @param cpuUsage Peak CPU usage
     */
    public static void saveInsertResultToCSV(
            String databaseType,
            int numberOfRecords,
            int batchSize,
            String caching,
            int numberOfThreads,
            String queryType,
            long executionTime,
            String ramUsage,
            String cpuUsage) {

        try {
            Path resourcesDir = Paths.get(RESOURCES_PATH);
            if (!Files.exists(resourcesDir)) {
                Files.createDirectories(resourcesDir);
            }

            Path csvFilePath = resourcesDir.resolve(RESULTS_CSV_FILENAME);
            boolean fileExists = Files.exists(csvFilePath);

            Path tempFile = Files.createTempFile("temp-", "-results.csv");

            if (fileExists) {
                Files.copy(csvFilePath, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }

            try (CSVWriter writer = new CSVWriter(new FileWriter(tempFile.toFile(), fileExists))) {
                if (!fileExists || Files.size(csvFilePath) == 0) {
                    writer.writeNext(CSV_HEADERS);
                }

                String[] dataRow = {
                        databaseType,
                        String.valueOf(numberOfRecords),
                        String.valueOf(batchSize),
                        caching,
                        String.valueOf(numberOfThreads),
                        queryType,
                        String.valueOf(executionTime),
                        ramUsage,
                        cpuUsage
                };

                writer.writeNext(dataRow);
            }

            Files.move(tempFile, csvFilePath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Results successfully saved to " + csvFilePath.toAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("Failed to save results to CSV: " + e.getMessage(), e);
        }
    }

    public static void saveReadResultsToCSV(
            String databaseType,
            String numberOfRecords,
            String caching,
            String queryVersion,
            String queryType,
            String indexing,
            long executionTime,
            String ramUsage,
            String cpuUsage) {

        try {
            Path resourcesDir = Paths.get(RESOURCES_PATH);
            if (!Files.exists(resourcesDir)) {
                Files.createDirectories(resourcesDir);
            }

            Path csvFilePath = resourcesDir.resolve(READ_RESULTS_CSV_FILENAME);
            boolean fileExists = Files.exists(csvFilePath);

            Path tempFile = Files.createTempFile("temp-", "-read-results.csv");

            if (fileExists) {
                Files.copy(csvFilePath, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }

            try (CSVWriter writer = new CSVWriter(new FileWriter(tempFile.toFile(), fileExists))) {
                if (!fileExists || Files.size(csvFilePath) == 0) {
                    writer.writeNext(READ_CSV_HEADERS);
                }

                String[] dataRow = {
                        databaseType,
                        String.valueOf(numberOfRecords),
                        caching,
                        queryVersion,
                        queryType,
                        indexing,
                        String.valueOf(executionTime),
                        ramUsage,
                        cpuUsage
                };

                writer.writeNext(dataRow);
            }

            Files.move(tempFile, csvFilePath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Read results successfully saved to " + csvFilePath.toAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("Failed to save read results to CSV: " + e.getMessage(), e);
        }
    }

    /**
     * Finds and loads CSV file from various locations including resources folder
     *
     * @param filename Name of the CSV file to find
     * @return File object if found, null otherwise
     */
    private static File findCsvFile(String filename) {
        try {
            try {
                Resource resource = new ClassPathResource(filename);
                if (resource.exists()) {
                    return resource.getFile();
                }
            } catch (Exception e) {
            }

            try {
                File file = ResourceUtils.getFile("classpath:" + filename);
                if (file.exists()) {
                    return file;
                }
            } catch (Exception e) {
            }

            Path resourcePath = Paths.get(RESOURCES_PATH, filename);
            if (Files.exists(resourcePath)) {
                return resourcePath.toFile();
            }

            File currentDirFile = new File(filename);
            if (currentDirFile.exists()) {
                return currentDirFile;
            }

            return new File(RESOURCES_PATH + filename);

        } catch (Exception e) {
            System.err.println("Error finding CSV file: " + e.getMessage());
            return new File(RESOURCES_PATH + filename);
        }
    }

    public static void saveUpdateResultsToCSV(
            String databaseType,
            String recordsInDatabase,
            String numberOfRecords,
            String caching,
            String queryType,
            long executionTime,
            String ramUsage,
            String cpuUsage) {

        try {
            Path resourcesDir = Paths.get(RESOURCES_PATH);
            if (!Files.exists(resourcesDir)) {
                Files.createDirectories(resourcesDir);
            }

            Path csvFilePath = resourcesDir.resolve(UPDATE_RESULTS_CSV_FILENAME);
            boolean fileExists = Files.exists(csvFilePath);

            Path tempFile = Files.createTempFile("temp-", "-update-results.csv");

            if (fileExists) {
                Files.copy(csvFilePath, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }

            try (CSVWriter writer = new CSVWriter(new FileWriter(tempFile.toFile(), fileExists))) {
                if (!fileExists || Files.size(csvFilePath) == 0) {
                    writer.writeNext(UPDATE_CSV_HEADERS);
                }

                String[] dataRow = {
                        databaseType,
                        recordsInDatabase,
                        numberOfRecords,
                        caching,
                        queryType,
                        String.valueOf(executionTime),
                        ramUsage,
                        cpuUsage
                };

                writer.writeNext(dataRow);
            }

            Files.move(tempFile, csvFilePath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Update results successfully saved to " + csvFilePath.toAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("Failed to save update results to CSV: " + e.getMessage(), e);
        }
    }


    public static void saveDeleteResultsToCSV(
            String databaseType,
            String recordsInDatabase,
            String numberOfRecords,
            String caching,
            String queryType,
            long executionTime,
            String ramUsage,
            String cpuUsage) {

        try {
            Path resourcesDir = Paths.get(RESOURCES_PATH);
            if (!Files.exists(resourcesDir)) {
                Files.createDirectories(resourcesDir);
            }

            Path csvFilePath = resourcesDir.resolve(DELETE_RESULTS_CSV_FILENAME);
            boolean fileExists = Files.exists(csvFilePath);

            Path tempFile = Files.createTempFile("temp-", "-delete-results.csv");

            if (fileExists) {
                Files.copy(csvFilePath, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }

            try (CSVWriter writer = new CSVWriter(new FileWriter(tempFile.toFile(), fileExists))) {
                if (!fileExists || Files.size(csvFilePath) == 0) {
                    writer.writeNext(DELETE_CSV_HEADERS);
                }

                String[] dataRow = {
                        databaseType,
                        recordsInDatabase,
                        numberOfRecords,
                        caching,
                        queryType,
                        String.valueOf(executionTime),
                        ramUsage,
                        cpuUsage
                };

                writer.writeNext(dataRow);
            }

            Files.move(tempFile, csvFilePath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Update results successfully saved to " + csvFilePath.toAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("Failed to save update results to CSV: " + e.getMessage(), e);
        }
    }

    /**
     * Saves analytical query results to a CSV file in the resources directory.
     *
     * @param databaseType The type of database (e.g., POSTGRESQL, MONGODB)
     * @param queryName The name of the analytical query (e.g., ANALYTICAL_QUERY_1, ANALYTICAL_QUERY_2)
     * @param caching Caching strategy used
     * @param executionTime Total execution time in milliseconds
     * @param ramUsage Peak RAM usage
     * @param cpuUsage Peak CPU usage
     */
    public static void saveAnalyticalQueryResultsToCSV(
            String databaseType,
            String numberOfRecords,
            String queryName,
            String caching,
            long executionTime,
            String ramUsage,
            String cpuUsage) {

        try {
            Path resourcesDir = Paths.get(RESOURCES_PATH);
            if (!Files.exists(resourcesDir)) {
                Files.createDirectories(resourcesDir);
            }

            Path csvFilePath = resourcesDir.resolve(ANALYTICAL_RESULTS_CSV_FILENAME);
            boolean fileExists = Files.exists(csvFilePath);

            Path tempFile = Files.createTempFile("temp-", "-analytical-results.csv");

            if (fileExists) {
                Files.copy(csvFilePath, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }

            try (CSVWriter writer = new CSVWriter(new FileWriter(tempFile.toFile(), fileExists))) {
                if (!fileExists || Files.size(csvFilePath) == 0) {
                    writer.writeNext(ANALYTICAL_CSV_HEADERS);
                }

                String[] dataRow = {
                        databaseType,
                        numberOfRecords,
                        queryName,
                        caching,
                        String.valueOf(executionTime),
                        ramUsage,
                        cpuUsage
                };

                writer.writeNext(dataRow);
            }

            Files.move(tempFile, csvFilePath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Analytical query results successfully saved to " + csvFilePath.toAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("Failed to save analytical query results to CSV: " + e.getMessage(), e);
        }
    }
}