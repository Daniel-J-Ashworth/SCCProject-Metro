package Code.GUI; //GUI folder

//import from journey file
import Code.Model.Journey; 
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

// Class for visuals
public class visual extends JPanel {
//stores journey for the visual
    private Journey journey;
//maps the route and line colours 
    private static final Map<String, Color> line_colours = new HashMap<>();
//block for colours
    static {
        line_colours.put("yellow", Color.yellow);    // yellow line
        line_colours.put("purple", Color.magenta);   // purple line
        line_colours.put("green", Color.green);      // Green line
        line_colours.put("lightblue", Color.cyan);   // light blue line
        line_colours.put("pink", Color.pink);        // pink line
        line_colours.put("darkblue", Color.blue);    // dark blue line
        line_colours.put("red", Color.red);          // red line
        line_colours.put("walk", Color.gray);        // walking
    }

//visual pannel
    public visual() {
        setPreferredSize(new Dimension(800, 200));
        setBackground(Color.white);
    }

//set journey for visuals and triggers 
    public void setJourney(Journey journey) {
// Stores journey inputted
        this.journey = journey;
        repaint();
    }

// Overriding paintcomponent to draw route
    @Override
    protected void paintComponent(Graphics g) {
// checks parent method
        super.paintComponent(g);
//exits if no journey or no stations have been selected
        if (journey == null || journey.getStations().isEmpty()) return;

//2D graphics for beter visuals
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); //use antialiasing to create a smooth line

//check pannel size
        int width = getWidth();
        int height = getHeight();
//set nodes 
        int nodeSize = 10;
        int y = height / 2;
        int xStep = width / (journey.getStations().size() + 1);

// check stations to draw nodes
        for (int i = 0; i < journey.getStations().size(); i++) {
// x-coordinate for current station
            int x = (i + 1) * xStep;
 // Get station name
            String station = journey.getStations().get(i);
// Get line for this station
            String line = journey.getLines().get(i);

 // set staton node as black circle
            g2d.setColor(Color.black);
            g2d.fillOval(x - nodeSize / 2, y - nodeSize / 2, nodeSize, nodeSize);

// display station name & alter above and below
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            int textY = (i % 2 == 0) ? y - 15 : y + 25;
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(station);
            int textX = x - textWidth / 2; // Centers text
//reduce overlapping
            if (i % 2 != 0) {
                textX += 10;
            }
            g2d.drawString(station, textX, textY);

//if station is not first line is drawn to first
            if (i > 0) {
//find x-coordinate of previous station
                int prevX = i * xStep;
// Set color on routes line
                g2d.setColor(line_colours.getOrDefault(line, Color.black));
// line thicknes
                g2d.setStroke(new BasicStroke(3));
// draw line between staions
                g2d.drawLine(prevX, y, x, y);
            }
        }
    }
}