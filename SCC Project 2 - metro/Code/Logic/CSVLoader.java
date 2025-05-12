package Code.Logic; //logic folder
// Import for file handling
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import Code.Model.connections; // import connections fle

// Class to load data from CSV files
public class CSVLoader {
    public List<connections> loadconnectionss(String filePath) throws IOException {
        List<connections> connectionss = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(filePath));

// read each line ignoring header
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
// Skip any empty lines
            if (line.trim().isEmpty()) continue;
            String[] parts = line.split(",");
// Validate line 
            if (parts.length < 4) {
                System.err.println("Skipping malformed line " + i + ": " + line);
                continue;
            }
            try {
// Create& add connections
                connectionss.add(new connections(
                    parts[0].trim(),  // from station
                    parts[1].trim(),  // to station
                    parts[2].trim(),  // line
                    Double.parseDouble(parts[3].trim())  // time
                ));
            } catch (NumberFormatException e) {
// error for invalid time 
                System.err.println("Invalid time format in line " + i + ": " + line);
            }
        }
        return connectionss;
    }

//load walking times from CSV
    public Map<String, Map<String, Double>> loadWalkingTimes(String filePath) throws IOException {
// walk times mapping
        Map<String, Map<String, Double>> walkTimes = new HashMap<>();
// read all lines from file
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        if (lines.isEmpty()) return walkTimes;

// parse the headers
        String[] headers = lines.get(0).split(",");
        for (int i = 1; i < headers.length; i++) {
            headers[i] = headers[i].trim();
            walkTimes.putIfAbsent(headers[i], new HashMap<>());
        }

//check each row
        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(",");
            String fromStation = parts[0].trim();
            walkTimes.putIfAbsent(fromStation, new HashMap<>());
            for (int j = 1; j < parts.length && j < headers.length; j++) {
                String toStation = headers[j];
                try {
                    double time = Double.parseDouble(parts[j].trim());
// Add walking time
                    walkTimes.get(fromStation).put(toStation, time);
                    walkTimes.get(toStation).put(fromStation, time);
                } catch (NumberFormatException e) {
//check for invalid walk times
                    System.err.println("Invalid walk time at line " + i + ", column " + j);
                }
            }
        }
        return walkTimes;
    }
}