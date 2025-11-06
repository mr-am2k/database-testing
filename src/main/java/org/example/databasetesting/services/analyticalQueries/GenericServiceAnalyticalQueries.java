package org.example.databasetesting.services.analyticalQueries;

import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.utils.DatabaseType;

public interface GenericServiceAnalyticalQueries {
    DatabaseActionResponse analyticalQuery1(DatabaseType databaseType);
    // Add more analytical query methods here as needed with different response types
    // DatabaseActionResponse analyticalQuery2(DatabaseType databaseType);
    // DatabaseActionResponse analyticalQuery3(DatabaseType databaseType);
}
