package io.breen.socrates.view.setup;

import io.breen.socrates.criteria.Criteria;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class SetupView extends JFrame {

    private JPanel rootPanel;

    private JButton openButton;
    private JButton selectButton;
    private JButton quitButton;

    private JPanel openCriteriaPanel;
    private JPanel selectSubmissionsPanel;

    public SetupView() {
        super("Socrates");

        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setResizable(false);
        this.setContentPane(rootPanel);

        JRootPane root = this.getRootPane();
        root.setDefaultButton(openButton);

        this.setSize(new Dimension(500, 400));
        this.setLocationRelativeTo(null);

        quitButton.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        SetupView.this.dispatchEvent(
                                new WindowEvent(SetupView.this, WindowEvent.WINDOW_CLOSING)
                        );
                    }
                }
        );
    }

    private static void disableCancelButton(Container container) {
        for (int i = 0; i < container.getComponentCount(); i++) {
            Component c = container.getComponent(i);
            if (c instanceof JButton) {
                JButton b = (JButton)c;
                if (b.getText().equals("Cancel")) {
                    b.setEnabled(false);
                    return;
                }
            } else if (c instanceof Container) {
                disableCancelButton((Container)c);
            }
        }
    }

    private static String[] concat(String[] a, String[] b) {
        String[] newArray = new String[a.length + b.length];
        System.arraycopy(a, 0, newArray, 0, a.length);
        System.arraycopy(b, 0, newArray, a.length, b.length);
        return newArray;
    }

    public Path chooseCriteriaFile() {
        JFileChooser fc = new JFileChooser();

        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setMultiSelectionEnabled(false);
        fc.setAcceptAllFileFilterUsed(false);

        String[] any = concat(
                Criteria.CRITERIA_FILE_EXTENSIONS, Criteria.CRITERIA_PACKAGE_EXTENSIONS
        );
        fc.addChoosableFileFilter(new FileNameExtensionFilter("Any criteria type", any));

        fc.addChoosableFileFilter(
                new FileNameExtensionFilter(
                        "Criteria files", Criteria.CRITERIA_FILE_EXTENSIONS
                )
        );
        fc.addChoosableFileFilter(
                new FileNameExtensionFilter(
                        "Criteria packages", Criteria.CRITERIA_PACKAGE_EXTENSIONS
                )
        );

        int rv = fc.showOpenDialog(this);

        if (rv == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile().toPath();
        } else {
            return null;
        }
    }

    public List<Path> chooseSubmissions() {
        JFileChooser fc = new JFileChooser();

        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setMultiSelectionEnabled(true);

        int rv = fc.showOpenDialog(this);

        if (rv == JFileChooser.APPROVE_OPTION) {
            List<Path> paths = new LinkedList<>();
            for (java.io.File f : fc.getSelectedFiles())
                paths.add(f.toPath());
            return paths;
        } else {
            return null;
        }
    }

    public void showCriteriaCard() {
        CardLayout cardLayout = (CardLayout)rootPanel.getLayout();
        cardLayout.show(rootPanel, "CriteriaCard");
    }

    public void showSubmissionsCard() {
        CardLayout cardLayout = (CardLayout)rootPanel.getLayout();
        cardLayout.show(rootPanel, "SubmissionsCard");
    }

    public void addOpenCriteriaButtonActionListener(ActionListener l) {
        openButton.addActionListener(l);
    }

    public void addSubmissionsButtonActionListener(ActionListener l) {
        selectButton.addActionListener(l);
    }
}
