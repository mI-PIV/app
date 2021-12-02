package testUtils;

import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.util.List;

public class ReadCsvFile {

    public static List<String[]> readCsv(File csvFile) {
        CSVReader reader;
        List<String[]> results;

        try {
            reader = new CSVReader(new FileReader(csvFile));
            results = reader.readAll();
        } catch (Exception e) {
            results = null;
            e.printStackTrace();
        }

        return results;
    }
}
