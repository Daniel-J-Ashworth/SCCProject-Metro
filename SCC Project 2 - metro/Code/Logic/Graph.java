package Code.Logic;//logic folder

import java.util.*;
import Code.Model.connections; //importing connection file

// grph for station & walking connectionms
public class Graph {
    private Map<String, List<connections>> adjacencyList = new HashMap<>();
    private Set<String> closedStations = new HashSet<>();

//add train connection
    public void addconnections(connections conn) {
//ignore if  station is closed
        if (closedStations.contains(conn.getFrom()) || closedStations.contains(conn.getTo())) return;
        adjacencyList.putIfAbsent(conn.getFrom(), new ArrayList<>());
        adjacencyList.putIfAbsent(conn.getTo(), new ArrayList<>());
        adjacencyList.get(conn.getFrom()).add(conn);
//revrse connection
        adjacencyList.get(conn.getTo()).add(new connections(
            conn.getTo(), conn.getFrom(), conn.getLine(), conn.getTime()
        ));
    }

//walking between 2 stations
    public void addWalkingconnections(String from, String to, double time) {
//ignore if station is closed
        if (closedStations.contains(from) || closedStations.contains(to)) return;
        adjacencyList.putIfAbsent(from, new ArrayList<>());
        adjacencyList.putIfAbsent(to, new ArrayList<>());
        adjacencyList.get(from).add(new connections(from, to, "walk", time));
//reverse walk connection
        adjacencyList.get(to).add(new connections(to, from, "walk", time));
    }

    //when station is closed removing it 
    public void closeStation(String station) {
// add station to closed set
        closedStations.add(station);
        adjacencyList.remove(station);
// removeing connections to  station from others
        for (List<connections> connectionss : adjacencyList.values()) {
            connectionss.removeIf(conn -> conn.getTo().equals(station));
        }
    }

// Reopens a previously closed station
    public void openStation(String station) {
        closedStations.remove(station);
    }

// updating travel time for connection and reverse
    public void updateconnectionsTime(String from, String to, String line, double newTotalTime) {
        List<connections> connectionss = adjacencyList.getOrDefault(from, new ArrayList<>());
        for (connections conn : connectionss) {
            if (conn.getTo().equals(to) && conn.getLine().equals(line)) {
                conn.setTime(newTotalTime);
            }
        }
// reverse connection
        connectionss = adjacencyList.getOrDefault(to, new ArrayList<>());
        for (connections conn : connectionss) {
            if (conn.getTo().equals(from) && conn.getLine().equals(line)) {
                conn.setTime(newTotalTime);
            }
        }
    }

// retrieve all connections to station
    public List<connections> getconnectionss(String station) {
// ifstation is closed return empty if not return connections
        return closedStations.contains(station) ? new ArrayList<>() : adjacencyList.getOrDefault(station, new ArrayList<>());
    }

// returns all open stations
    public Set<String> getAllStations() {
        Set<String> stations = new HashSet<>(adjacencyList.keySet());
        // exclude any closed stations
        stations.removeAll(closedStations);
        return stations;
    }
}