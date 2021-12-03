package testUtils;

import com.opencsv.CSVReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class ReadCsvFile {
    public static List<String[]> readCsv(InputStream csvStream) {
        CSVReader reader;
        List<String[]> results;

        try {
            reader = new CSVReader(new InputStreamReader(csvStream));
            results = reader.readAll();
        } catch (Exception e) {
            results = null;
            e.printStackTrace();
        }

        return results;
    }
}
