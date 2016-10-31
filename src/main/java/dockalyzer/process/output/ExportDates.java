package dockalyzer.process.output;

import dockalyzer.tools.csvutil.CSVUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by salizumberi-laptop on 30.10.2016.
 */
public class ExportDates {
    private static void exportDatesToCSV(ArrayList<Date> datesArray) throws IOException {
        String csvFile = "commitdates.csv";
        FileWriter writer = new FileWriter(csvFile);
        CSVUtils.writeLine(writer, datesArray);

        //custom separator + quote
        //CSVUtils.writeLine(writer, Arrays.asList("aaa", "bb,b", "cc,c"), ',', '"');

        //custom separator + quote
        // CSVUtils.writeLine(writer, Arrays.asList("aaa", "bbb", "cc,c"), '|', '\'');

        //double-quotes
        // CSVUtils.writeLine(writer, Arrays.asList("aaa", "bbb", "cc\"c"));

        writer.flush();
        writer.close();
    }
}
