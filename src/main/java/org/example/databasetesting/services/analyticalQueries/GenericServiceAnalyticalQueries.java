package org.example.databasetesting.services.analyticalQueries;

import org.example.databasetesting.response.DatabaseActionResponse;
import org.example.databasetesting.utils.DatabaseType;

public interface GenericServiceAnalyticalQueries {
    DatabaseActionResponse analyticalQuery1(DatabaseType databaseType);
    DatabaseActionResponse analyticalQuery2(DatabaseType databaseType);
    DatabaseActionResponse analyticalQuery3(DatabaseType databaseType);
    DatabaseActionResponse analyticalQuery4(DatabaseType databaseType);
    DatabaseActionResponse analyticalQuery5(DatabaseType databaseType);
}
