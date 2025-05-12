package Code.GUI; // GUI folder

import javax.swing.*;
import Code.Logic.RouteCalc; //importing route calc file
import Code.Model.Journey; //importing jouney file
import Code.Model.connections; //importing connection file
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;

// Main GUI class 
public class GUI extends JFrame {
    private JComboBox<String> startCombo, endCombo; // selecting start and end stations
    private JTextArea resultArea; // text to display the route result
    private RouteCalc calculator; // route calc to find journey
    private JList<String> closedStationsList, delayList; // stores list closed staions& applied delay
    private DefaultListModel<String> closedStationsModel, delayListModel; // closed & delay models
    private JComboBox<String> delayFromCombo, delayToCombo, delayLineCombo; // selecting delay inputs
    private JTextField delayTimeField; // inputing delay time
    private JRadioButton shortestTimeRadio, fewestChangesRadio; // radio button to select short or few
    private visual visual; // pannel to display route visual
    private List<DelayEntry> appliedDelays; // list of aplied delays
    private JComboBox<String> stationSelector; // select stations to close

    public GUI() {
        setTitle("Metrolink"); 
        setSize(1200, 1000); 
        setDefaultCloseOperation(EXIT_ON_CLOSE); 

        // route calc and delay list
        try {
            // load CSV files
            calculator = new RouteCalc("Data/Metrolink_times_linecolour.csv", "Data/walktimes.csv");
        } catch (Exception e) {
            // display error if data fails
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        // list to store delays
        appliedDelays = new LinkedList<>();

        //main panel 
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.lightGray);
        add(mainPanel);

        //input panel 
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(Color.lightGray);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        //selecting stations pannel
        JPanel stationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        stationPanel.setBackground(Color.lightGray); 
       
        //start station combobox
        startCombo = new JComboBox<>();
        startCombo.setEditable(true);
        startCombo.setPreferredSize(new Dimension(200, 30));
       
        //end station combobox
        endCombo = new JComboBox<>();
        endCombo.setEditable(true);
        endCombo.setPreferredSize(new Dimension(200, 30));
       
        //labels 
        stationPanel.add(new JLabel("From:"));
        stationPanel.add(startCombo);
        stationPanel.add(Box.createHorizontalStrut(15));
        stationPanel.add(new JLabel("To:"));
        stationPanel.add(endCombo);
       
        // add to input panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        inputPanel.add(stationPanel, gbc);

        //radio buttons for options (few & short)
        shortestTimeRadio = new JRadioButton("Shortest Time", true);
        fewestChangesRadio = new JRadioButton("Fewest Changes");
        shortestTimeRadio.setBackground(Color.lightGray); 
        fewestChangesRadio.setBackground(Color.lightGray); 

        //only allowing 1 input on options
        ButtonGroup selectionGroup = new ButtonGroup();
        selectionGroup.add(shortestTimeRadio);
        selectionGroup.add(fewestChangesRadio);

        //options pannel
        JPanel selectionPanel = new JPanel(new FlowLayout());
        selectionPanel.setBackground(Color.lightGray);
        selectionPanel.add(new JLabel("Select Journey type:"));
        selectionPanel.add(shortestTimeRadio);
        selectionPanel.add(fewestChangesRadio);

        // add to input panel.
        gbc.gridy = 1;
        inputPanel.add(selectionPanel, gbc);

        // closed station list 
        closedStationsModel = new DefaultListModel<>();
        closedStationsList = new JList<>(closedStationsModel);
        closedStationsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // scroll for closed box
        JScrollPane closedStationsScroll = new JScrollPane(closedStationsList);
        closedStationsScroll.setPreferredSize(new Dimension(200, 200));

        // box to select closed station
        stationSelector = new JComboBox<>();
        stationSelector.setEditable(true);
        stationSelector.setPreferredSize(new Dimension(200, 30));
        calculator.getAllStations().forEach(stationSelector::addItem); 

        // close station button
        JButton addClosedButton = new JButton("Close Station");
        addClosedButton.setBackground(Color.blue); 
        addClosedButton.setForeground(Color.white);
        addClosedButton.addActionListener(e -> {
            String selected = (String) stationSelector.getSelectedItem(); 
            if (selected != null && !closedStationsModel.contains(selected) && calculator.getAllStations().contains(selected)) {
                closedStationsModel.addElement(selected); 
                calculator.closeStation(selected);
                updateStationCombos(); 
                stationSelector.setSelectedItem(null); 
            } else {
                // error if invalid input
                JOptionPane.showMessageDialog(this, "Please select a valid, open station to close.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // button to clear 
        JButton clearClosedButton = new JButton("Clear Closures");
        clearClosedButton.setBackground(Color.blue);
        clearClosedButton.setForeground(Color.white);
        clearClosedButton.addActionListener(e -> {
            try {
                //keep delays
                List<DelayEntry> savedDelays = new ArrayList<>(appliedDelays);
               
                //relaod data
                calculator = new RouteCalc("Data/Metrolink_times_linecolour.csv", "Data/walktimes.csv");
               
                // add back delays
                for (DelayEntry delay : savedDelays) {
                    double originalTime = 0.0;
                    for (connections conn : calculator.getGraphconnectionss(delay.from)) {
                        if (conn.getTo().equals(delay.to) && conn.getLine().equals(delay.line)) {
                            originalTime = conn.getTime();
                            break;
                        }
                    }
                    // calc new total time
                    double newTotalTime = originalTime + delay.time;
                    calculator.updateconnectionsTime(delay.from, delay.to, delay.line, newTotalTime); 
                    delayListModel.addElement(String.format("%s to %s, %s, +%.1f mins delay", delay.from, delay.to, delay.line, delay.time)); 
                }
               
                // clear closed stations box & refreshes
                closedStationsModel.clear();
                updateStationCombos();
               
                // if successful message displayed
                JOptionPane.showMessageDialog(this, "Closed stations cleared successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                // message if error
                JOptionPane.showMessageDialog(this, "Error clearing closed stations: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        //closed station pannel
        JPanel closedPanel = new JPanel(new BorderLayout());
        closedPanel.setBackground(Color.lightGray);
        closedPanel.add(new JLabel("Closed Stations:"), BorderLayout.NORTH);
        closedPanel.add(closedStationsScroll, BorderLayout.CENTER);

        //panel for closed buttons
        JPanel closedButtonPanel = new JPanel(new FlowLayout());
        closedButtonPanel.setBackground(Color.lightGray);
        closedButtonPanel.add(stationSelector);
        closedButtonPanel.add(addClosedButton);
        closedButtonPanel.add(clearClosedButton);
        closedPanel.add(closedButtonPanel, BorderLayout.SOUTH);

        // add to input panel
        gbc.gridy = 2;
        inputPanel.add(closedPanel, gbc);

        // delay input boxs
        delayFromCombo = new JComboBox<>();
        delayFromCombo.setEditable(true);
        delayFromCombo.setPreferredSize(new Dimension(200, 30));
        delayToCombo = new JComboBox<>();
        delayToCombo.setEditable(true);
        delayToCombo.setPreferredSize(new Dimension(200, 30));
        delayLineCombo = new JComboBox<>();
        delayLineCombo.setEditable(true);
        delayLineCombo.setPreferredSize(new Dimension(150, 30));
        //add data to boxes
        calculator.getAllStations().forEach(station -> {
            delayFromCombo.addItem(station);
            delayToCombo.addItem(station);
        });

        // have input time
        delayTimeField = new JTextField(5);

        //apply button
        JButton applyDelayButton = new JButton("Apply Delay");
        applyDelayButton.setBackground(Color.blue); 
        applyDelayButton.setForeground(Color.white); 
        applyDelayButton.addActionListener(e -> applyDelay());

        //delay list
        delayListModel = new DefaultListModel<>();
        delayList = new JList<>(delayListModel);
        JScrollPane delayListScroll = new JScrollPane(delayList);
        delayListScroll.setPreferredSize(new Dimension(300, 100));

        //button to clear
        JButton clearDelaysButton = new JButton("Clear Delays");
        clearDelaysButton.setBackground(Color.blue); 
        clearDelaysButton.setForeground(Color.white);
        clearDelaysButton.addActionListener(e -> {
            try {
                // keep closed station
                List<String> closedStations = new ArrayList<>();
                for (int i = 0; i < closedStationsModel.size(); i++) {
                    closedStations.add(closedStationsModel.getElementAt(i));
                }
                // reload data file
                calculator = new RouteCalc("Data/Metrolink_times_linecolour.csv", "Data/walktimes.csv");
                // add closed stations back
                for (String station : closedStations) {
                    calculator.closeStation(station);
                }
                // Clear & refresh dlay
                appliedDelays.clear();
                delayListModel.clear();
                updateStationCombos();
                // if success shows message
                JOptionPane.showMessageDialog(this, "Delays cleared successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                // error if not
                JOptionPane.showMessageDialog(this, "Error clearing delays: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // delay pannel
        JPanel delayPanel = new JPanel(new BorderLayout());
        delayPanel.setBackground(Color.lightGray); 
        JPanel delayInputPanel = new JPanel(new FlowLayout());
        delayInputPanel.setBackground(Color.lightGray);
        delayInputPanel.add(new JLabel("Delay - From:"));
        delayInputPanel.add(delayFromCombo);
        delayInputPanel.add(new JLabel("To:"));
        delayInputPanel.add(delayToCombo);
        delayInputPanel.add(new JLabel("Line:"));
        delayInputPanel.add(delayLineCombo);
        delayInputPanel.add(new JLabel("Delay Time (mins):"));
        delayInputPanel.add(delayTimeField);
        delayInputPanel.add(applyDelayButton);
        delayInputPanel.add(clearDelaysButton);
        delayPanel.add(delayInputPanel, BorderLayout.NORTH);
        delayPanel.add(new JLabel("Applied Delays:"), BorderLayout.CENTER);
        delayPanel.add(delayListScroll, BorderLayout.SOUTH);

        // add to input paneel
        gbc.gridy = 3;
        inputPanel.add(delayPanel, gbc);

        //update drop down for line
        delayFromCombo.addActionListener(e -> updateLineCombo());
        delayToCombo.addActionListener(e -> updateLineCombo());

        // calc route button
        JButton calculateBtn = new JButton("Find Route");
        calculateBtn.setBackground(Color.blue); 
        calculateBtn.setForeground(Color.white); 
        calculateBtn.addActionListener(e -> calculateRoute());

        // add calc to input panel
        gbc.gridy = 4;
        inputPanel.add(calculateBtn, gbc);

        // add input to main
        mainPanel.add(inputPanel, BorderLayout.NORTH);

        //centre pannel for results & visua
        JPanel centrePanel = new JPanel(new BorderLayout());
        centrePanel.setBackground(Color.lightGray);

        // results area
        resultArea = new JTextArea(15, 50);
        resultArea.setEditable(false);
        resultArea.setBackground(Color.white);
        resultArea.setFont(new Font("Arial", Font.PLAIN, 14));
        centrePanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);

        // visual pannel
        visual = new visual();
        visual.setBackground(Color.white);
        visual.setPreferredSize(new Dimension(800, 200));
        centrePanel.add(visual, BorderLayout.SOUTH);

        // add centre to main
        mainPanel.add(centrePanel, BorderLayout.CENTER);

        // start load stations 
        loadStations();
        new search(startCombo, calculator.getAllStations());
        new search(endCombo, calculator.getAllStations());
        new search(delayFromCombo, calculator.getAllStations());
        new search(delayToCombo, calculator.getAllStations());
        new search(stationSelector, calculator.getAllStations());
        updateLineCombo();
    }

    // load into combo boxs
    private void loadStations() {
        startCombo.removeAllItems();
        endCombo.removeAllItems();
        delayFromCombo.removeAllItems();
        delayToCombo.removeAllItems();
        stationSelector.removeAllItems();
        for (String station : calculator.getAllStations()) {
            startCombo.addItem(station); 
            endCombo.addItem(station); 
            delayFromCombo.addItem(station); 
            delayToCombo.addItem(station); 
            stationSelector.addItem(station); 
        }
    }

    // update with current stations
    private void updateStationCombos() {
        List<String> currentStations = calculator.getAllStations(); 
       
        //keep current selections
        String startText = startCombo.getEditor().getItem() != null ? startCombo.getEditor().getItem().toString() : "";
        String endText = endCombo.getEditor().getItem() != null ? endCombo.getEditor().getItem().toString() : "";
        String delayFromText = delayFromCombo.getEditor().getItem() != null ? delayFromCombo.getEditor().getItem().toString() : "";
        String delayToText = delayToCombo.getEditor().getItem() != null ? delayToCombo.getEditor().getItem().toString() : "";
        String selectorText = stationSelector.getEditor().getItem() != null ? stationSelector.getEditor().getItem().toString() : "";

        // clear boxs
        startCombo.removeAllItems();
        endCombo.removeAllItems();
        delayFromCombo.removeAllItems();
        delayToCombo.removeAllItems();
        stationSelector.removeAllItems();

        // reload boxs with data
        currentStations.forEach(station -> {
            startCombo.addItem(station);
            endCombo.addItem(station);
            delayFromCombo.addItem(station);
            delayToCombo.addItem(station);
            stationSelector.addItem(station);
        });

        // if valid restor selections
        if (!currentStations.contains(startText)) {
            startCombo.setSelectedIndex(currentStations.isEmpty() ? -1 : 0);
        } else {
            startCombo.setSelectedItem(startText);
        }
        if (!currentStations.contains(endText)) {
            endCombo.setSelectedIndex(currentStations.isEmpty() ? -1 : 0);
        } else {
            endCombo.setSelectedItem(endText);
        }
        if (currentStations.contains(delayFromText)) {
            delayFromCombo.setSelectedItem(delayFromText);
        } else {
            delayFromCombo.getEditor().setItem(delayFromText);
        }
        if (currentStations.contains(delayToText)) {
            delayToCombo.setSelectedItem(delayToText);
        } else {
            delayToCombo.getEditor().setItem(delayToText);
        }
        if (currentStations.contains(selectorText)) {
            stationSelector.setSelectedItem(selectorText);
        } else {
            stationSelector.getEditor().setItem(selectorText);
        }

        // search listeners to boxs
        new search(startCombo, currentStations);
        new search(endCombo, currentStations);
        new search(delayFromCombo, currentStations);
        new search(delayToCombo, currentStations);
        new search(stationSelector, currentStations);
        updateLineCombo(); 
    }

    //update delay with boxs
    private void updateLineCombo() {
        String from = (String) delayFromCombo.getSelectedItem();
        String to = (String) delayToCombo.getSelectedItem(); 
        delayLineCombo.removeAllItems(); 

        List<String> validLines = new ArrayList<>();
        if (from != null && to != null) {
            for (connections conn : getGraphconnectionss(from)) {
                if (conn.getTo().equals(to) && !conn.getLine().equals("walk")) {
                    validLines.add(conn.getLine()); 
                }
            }
            validLines.forEach(delayLineCombo::addItem); 
            if (validLines.isEmpty()) {
                delayLineCombo.addItem("No valid lines"); 
                delayLineCombo.setEnabled(false); 
            } else {
                delayLineCombo.setEnabled(true); 
            }
        } else {
            delayLineCombo.addItem("Select stations first");
            delayLineCombo.setEnabled(false); 
        }
        new search(delayLineCombo, validLines.isEmpty() ? new ArrayList<>() : validLines);
    }

    // apply delay to selected stations &has to match up
    private void applyDelay() {
        String from = (String) delayFromCombo.getSelectedItem(); 
        String to = (String) delayToCombo.getSelectedItem(); 
        String line = (String) delayLineCombo.getSelectedItem(); 
        String timeStr = delayTimeField.getText().trim(); 

        // checks if matched
        if (from == null || to == null || line == null || timeStr.isEmpty() || line.equals("No valid lines") || line.equals("Select stations first")) {
            JOptionPane.showMessageDialog(this, "Please select valid stations and line, and enter a delay time.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double delayTime;
        try {
            delayTime = Double.parseDouble(timeStr); // read delay inputed
            if (delayTime < 0) {
                JOptionPane.showMessageDialog(this, "Delay time must be a non-negative number.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid delay time format: " + timeStr, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // check if delay is already applied
        for (DelayEntry delay : appliedDelays) {
            if (delay.from.equals(from) && delay.to.equals(to) && delay.line.equals(line)) {
                // output message for delay already applied 
                JOptionPane.showMessageDialog(this, "A delay is already applied to this connections. Clear the existing delay first.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        try {
            // get original connection time
            double originalTime = 0.0;
            for (connections conn : calculator.getGraphconnectionss(from)) {
                if (conn.getTo().equals(to) && conn.getLine().equals(line)) {
                    originalTime = conn.getTime();
                    break;
                }
            }

            // calc new time with dlay
            double newTotalTime = originalTime + delayTime;

            // appky the new time
            calculator.updateconnectionsTime(from, to, line, newTotalTime);
            // store delay
            appliedDelays.add(new DelayEntry(from, to, line, delayTime));
            //display the inputed delay
            delayListModel.addElement(String.format("%s to %s, %s, +%.1f mins delay", from, to, line, delayTime));
            JOptionPane.showMessageDialog(this, "Delay applied successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            delayTimeField.setText(""); 
        } catch (Exception e) {
            //display error if failed
            JOptionPane.showMessageDialog(this, "Error applying delay: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // clac route between stations
    private void calculateRoute() {
        String start = (String) startCombo.getSelectedItem();
        String end = (String) endCombo.getSelectedItem();

        // check if inputs valid
        if (start == null || end == null || start.trim().isEmpty() || end.trim().isEmpty()) {
            resultArea.setText("Please select valid start and end stations.");
            return;
        }

        List<String> validStations = calculator.getAllStations();
        if (!validStations.contains(start)) {
            resultArea.setText("Invalid start station: " + start + "\n Please Enter a Valid Station");
            return;
        }
        if (!validStations.contains(end)) {
            resultArea.setText("Invalid end station: " + end + "\n Please Enter a Valid Station");
            return;
        }

        try {
            boolean minimiseChanges = fewestChangesRadio.isSelected(); 
            //calc route
            Journey journey = calculator.calculateRoute(start, end, minimiseChanges); 
            if (journey.getStations().isEmpty()) {
                resultArea.setText("No route found from " + start + " to " + end + ".");
            } else {
                // display route with chosen selection
                resultArea.setText((minimiseChanges ? "*** Route with Fewest Changes ***\n" : "*** Shortest Time Route ***\n") + journey.toString());
                //update visuals
                visual.setJourney(journey); 
            }
        } catch (Exception e) {
            //error if calc fails
            resultArea.setText("Error calculating route: " + e.getMessage());
        }
    }

    // get connections between stations
    private List<connections> getGraphconnectionss(String station) {
        return calculator.getGraphconnectionss(station);
    }

    // store delay info
    private static class DelayEntry {
        String from, to, line;
        double time;

        DelayEntry(String from, String to, String line, double time) {
            this.from = from;
            this.to = to;
            this.line = line;
            this.time = time;
        }
    }
}