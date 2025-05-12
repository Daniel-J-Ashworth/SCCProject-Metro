package Code.Model; //model folder

// Class for connecting stations
public class connections {
//stating station
    private String from;
// ending station
    private String to;
// check for line or walk
    private String line;
// time in minutes
    private double time;

    // connections constructor
    public connections(String from, String to, String line, double time) {
        this.from = from;
        this.to = to;
        this.line = line;
        this.time = time;
    }

// gets from station
    public String getFrom() {
        return from;
    }

// gets to station
    public String getTo() {
        return to;
    }

// gets line
    public String getLine() {
        return line;
    }

//gets time
    public double getTime() {
        return time;
    }

// time setter
    public void setTime(double time) {
        this.time = time;
    }
}