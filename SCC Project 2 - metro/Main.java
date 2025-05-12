// import swing & GUI to open window
import javax.swing.SwingUtilities;
import Code.GUI.GUI;

// Main class 
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GUI gui = new GUI();
            gui.setVisible(true);
        });
    }
}