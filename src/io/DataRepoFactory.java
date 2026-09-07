package io;

import java.io.File;

public final class DataRepoFactory {

    private DataRepoFactory() {
        // Utility klasa
    }

    public static DataRepo forFile(File file) {
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".csv")) {
            return new CsvDataRepo();
        }
        if (fileName.endsWith(".json")) {
            return new JsonDataRepo();
        }

        throw new IllegalArgumentException(
                "Nepodrzan format fajla '" + file.getName() + "'. Podrzani formati su .csv i .json.");
    }
}