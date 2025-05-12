package Code.Logic; //logic folder

import java.io.IOException;
import java.util.*;
//importing model folder
import Code.Model.connections;
import Code.Model.Journey;

//calulator class (dijkstra)
public class RouteCalc {
    //walk & tram Connections
    private Graph graph;
    //assumsion that changes take 2 minutes
    private static final double CHANGE_PENALTY = 2.0;

    //graph constructor 
    public RouteCalc(String tramFilePath, String walkFilePath) throws IOException {
        this.graph = new Graph();
         //load Connections
        CSVLoader loader = new CSVLoader();
        loader.loadconnectionss(tramFilePath).forEach(graph::addconnections);
        //add walking times to graph
        Map<String, Map<String, Double>> walkTimes = loader.loadWalkingTimes(walkFilePath);
        for (String from : walkTimes.keySet()) {
            for (String to : walkTimes.get(from).keySet()) {
                if (!from.equals(to)) {
                    graph.addWalkingconnections(from, to, walkTimes.get(from).get(to));
                }
            }
        }
    }

    // close station
    public void closeStation(String station) {
        graph.closeStation(station);
    }
    //update travel time 
    public void updateconnectionsTime(String from, String to, String line, double newTotalTime) {
        graph.updateconnectionsTime(from, to, line, newTotalTime);
    }
    //fewest or shortest time 
    public Journey calculateRoute(String start, String end, boolean minimizeChanges) {
        return minimizeChanges ? calculateFewestChangesRoute(start, end) : calculateShortestTimeRoute(start, end);
    }

    //shortest (DIJ algo)
    private Journey calculateShortestTimeRoute(String start, String end) {
        // data strcuture
        Map<String, Double> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        Map<String, String> prevLine = new HashMap<>();
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(n -> n.distance));
        Set<String> visited = new HashSet<>();
        Map<String, Integer> changes = new HashMap<>();
        Map<String, Double> connectionTimesMap = new HashMap<>();

        // start distance & changes
        for (String station : graph.getAllStations()) {
            distances.put(station, Double.MAX_VALUE);
            changes.put(station, Integer.MAX_VALUE);
        }
        distances.put(start, 0.0);
        changes.put(start, 0);
        pq.offer(new Node(start, 0.0, null));

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            String currentStation = current.station;
            String currentLine = current.line;

            if (visited.contains(currentStation)) continue;
            visited.add(currentStation);

            // reconstruct journey if matched
            if (currentStation.equals(end)) {
                List<String> stations = new ArrayList<>();
                List<String> lines = new ArrayList<>();
                List<Double> connectionTimes = new ArrayList<>();
                String station = end;
                while (station != null) {
                    stations.add(0, station);
                    lines.add(0, prevLine.getOrDefault(station, "walk"));
                    if (previous.containsKey(station)) {
                        connectionTimes.add(0, connectionTimesMap.getOrDefault(station, 0.0));
                    }
                    station = previous.get(station);
                }
                return new Journey(stations, lines, distances.get(end), changes.get(end), connectionTimes);
            }

            // check for other jouneys
            for (connections conn : graph.getconnectionss(currentStation)) {
                String other = conn.getTo();
                double edgeTime = conn.getTime();
                //apply changes if line switched 
                int changeCost = (currentLine != null && !currentLine.equals(conn.getLine()) && !conn.getLine().equals("walk")) ? 1 : 0;
                double newDist = distances.get(currentStation) + edgeTime + (changeCost * CHANGE_PENALTY);

                // update if shorter path found
                if (newDist < distances.get(other)) {
                    distances.put(other, newDist);
                    previous.put(other, currentStation);
                    prevLine.put(other, conn.getLine());
                    changes.put(other, changes.get(currentStation) + changeCost);
                    connectionTimesMap.put(other, edgeTime);
                    pq.offer(new Node(other, newDist, conn.getLine()));
                }
            }
        }

        //retun empty if no route found
        return new Journey(new ArrayList<>(), new ArrayList<>(), 0.0, 0, new ArrayList<>());
    }

    // calc fewest changes
    private Journey calculateFewestChangesRoute(String start, String end) {
        // data structures
        Queue<Node> queue = new LinkedList<>();
        Map<String, Integer> changes = new HashMap<>();
        Map<String, Double> times = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        Map<String, String> prevLine = new HashMap<>();
        Set<String> visited = new HashSet<>();
        Map<String, Double> connectionTimesMap = new HashMap<>();

        // changes &times all stations
        for (String station : graph.getAllStations()) {
            changes.put(station, Integer.MAX_VALUE);
            times.put(station, Double.MAX_VALUE);
        }
        changes.put(start, 0);
        times.put(start, 0.0);
        queue.offer(new Node(start, 0.0, null));
        visited.add(start);

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            String currentStation = current.station;
            String currentLine = current.line;

            // reconstruct journey if matched
            if (currentStation.equals(end)) {
                List<String> stations = new ArrayList<>();
                List<String> lines = new ArrayList<>();
                List<Double> connectionTimes = new ArrayList<>();
                String station = end;
                while (station != null) {
                    stations.add(0, station);
                    lines.add(0, prevLine.getOrDefault(station, "walk"));
                    if (previous.containsKey(station)) {
                        connectionTimes.add(0, connectionTimesMap.getOrDefault(station, 0.0));
                    }
                    station = previous.get(station);
                }
                return new Journey(stations, lines, times.get(end), changes.get(end), connectionTimes);
            }

            // check others
            for (connections conn : graph.getconnectionss(currentStation)) {
                String other = conn.getTo();
                if (visited.contains(other)) continue;

                // Calc changes time
                int newChanges = changes.get(currentStation) + ((currentLine != null && !currentLine.equals(conn.getLine()) && !conn.getLine().equals("walk")) ? 1 : 0);
                double newTime = times.get(currentStation) + conn.getTime() + (newChanges > changes.get(currentStation) ? CHANGE_PENALTY : 0);

                // update if fewer changes or less time is found
                if (newChanges < changes.get(other) || (newChanges == changes.get(other) && newTime < times.get(other))) {
                    changes.put(other, newChanges);
                    times.put(other, newTime);
                    previous.put(other, currentStation);
                    prevLine.put(other, conn.getLine());
                    connectionTimesMap.put(other, conn.getTime());
                    queue.offer(new Node(other, newTime, conn.getLine()));
                    visited.add(other);
                }
            }
        }
        // return empty if not
        return new Journey(new ArrayList<>(), new ArrayList<>(), 0.0, 0, new ArrayList<>());
    }

    // list of all stations
    public List<String> getAllStations() {
        return new ArrayList<>(graph.getAllStations());
    }

    // return connections for station
    public List<connections> getGraphconnectionss(String station) {
        return graph.getconnectionss(station);
    }

    //class for node
    private static class Node {
        String station; // Station name
        double distance; // total distance time
        String line; // curent line

        Node(String station, double distance, String line) {
            this.station = station;
            this.distance = distance;
            this.line = line;
        }
    }
}