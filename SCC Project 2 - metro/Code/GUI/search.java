package Code.GUI; //GUI folder

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList; 
import java.util.List;

// class for searching in combobox
public class search implements DocumentListener {
//filter
    private JComboBox<String> combo;
// List all possible stations
    private List<String> allStations;
//storeing refrences & station list
    public search(JComboBox<String> combo, List<String> allStations) {
        this.combo = combo;
        this.allStations = new ArrayList<>(allStations);

// key trigger filtering on key release
        combo.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
// Updates filter when typing
                updateFilter();
            }
        });
    }

//handles text being typed
    @Override
    public void insertUpdate(DocumentEvent e) {
        updateFilter();
    }

//handles text being removed
    @Override
    public void removeUpdate(DocumentEvent e) {
        updateFilter();
    }

// handle attributes
    @Override
    public void changedUpdate(DocumentEvent e) {
        updateFilter();
    }

//filters depending on whats inputted 
    private void updateFilter() {
//checks text with diffrent cases &if the text is in matched in the midle 
        String input = combo.getEditor().getItem().toString().trim().toLowerCase();

        SwingUtilities.invokeLater(() -> {
            String currentText = combo.getEditor().getItem().toString();
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();

//stations that match input
            for (String station : allStations) {
                if (input.isEmpty() || station.toLowerCase().contains(input)) {
                    model.addElement(station);
                }
            }

// Updates combobox
            combo.setModel(model);
            combo.getEditor().setItem(currentText);

// Shows &hides popups on input checked with stations
            if (model.getSize() > 0 && !input.isEmpty()) {
                combo.showPopup();
            } else {
                combo.hidePopup();
            }
        });
    }
}