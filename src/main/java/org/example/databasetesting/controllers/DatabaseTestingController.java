package org.example.databasetesting.controllers;

import org.example.databasetesting.requests.DatabaseActionRequest;
import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.services.KeyValueService;
import org.example.databasetesting.utils.DatabaseType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/key-value")
public class DatabaseTestingController {
    private final KeyValueService keyValueService;

    public DatabaseTestingController(KeyValueService keyValueService) {
        this.keyValueService = keyValueService;
    }

    @PutMapping
    public DatabaseActionResponse insertAll(@RequestBody final DatabaseActionRequest databaseActionRequest) {
        return keyValueService.batchInsert(databaseActionRequest);
    }

    @GetMapping
    public DatabaseActionResponse getAll(@RequestParam DatabaseType databaseType) {
        return keyValueService.getAll(databaseType);
    }

    @GetMapping("/{key}")
    public DatabaseActionResponse getByKey(@PathVariable final String key, @RequestParam DatabaseType databaseType) {
        return keyValueService.getbyKey(key, databaseType);
    }

    @DeleteMapping("/{key}")
    public DatabaseActionResponse deleteByKey(@PathVariable final String key, @RequestParam DatabaseType databaseType) {
        return keyValueService.deleteByKey(key, databaseType);
    }
}
