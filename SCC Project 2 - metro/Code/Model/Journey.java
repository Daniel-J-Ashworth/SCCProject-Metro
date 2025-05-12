package Code.Model; //folder for model
import java.util.*;

// Class for jounrney
public class Journey {
// stations list in journey
    private List<String> stations;
// list of lines used 
    private List<String> lines;
// total travel time
    private double totalTime;
// number of line changes
    private int changes;
// segment times for each connection
    private List<Double> connectionTimes;

// journey constructor
    public Journey(List<String> stations, List<String> lines, 
    double totalTime, int changes, List<Double> connectionTimes) {
        this.stations = stations;
        this.lines = lines;
        this.totalTime = totalTime;
        this.changes = changes;
        this.connectionTimes = connectionTimes;
    }

//format journey as string
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String prevLine = null;
// check through staions
        for (int i = 0; i < stations.size(); i++) {
            String station = stations.get(i);
            String line = lines.get(i);
// says if line changes
            if (i > 0 && !line.equals(prevLine) && !line.equals("walk") 
            && !prevLine.equals("walk")) { 
                sb.append("** Change Line to ").append(line).append(" line ***\n");
            }
// tells if walking and time for walk
            if (line.equals("walk") && i > 0) {
                sb.append("Walk to ").append(station).append
                (" (").append(String.format("%.1f", connectionTimes.get(i-1))).append(" mins)\n");
            } else {
                sb.append(station).append(" on ").append(line).append(" line\n");
            }
            prevLine = line;
        }
// append the total time and changes
        sb.append("\nTotal Journey Time (mins) = ").append(totalTime);
        sb.append("\nNumber of Changes = ").append(changes);
        return sb.toString();
    }

//get stations
    public List<String> getStations() {
        return stations;
    }

// get lines
    public List<String> getLines() {
        return lines;
    }

// get total time
    public double getTotalTime() {
        return totalTime;
    }

// get number of changes
    public int getChanges() {
        return changes;
    }
}