package gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.function.IntConsumer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;

public class SimulationControlPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int MIN_SPEED = 1;
    private static final int MAX_SPEED = 10;
    private static final int DEFAULT_SPEED = 1;

    private final JLabel clockLabel = new JLabel("00:00");

    public SimulationControlPanel(Runnable onStart, Runnable onPause, Runnable onReset,
            IntConsumer onSpeedChange, Runnable onZoomReset) {
        super(new FlowLayout(FlowLayout.LEFT));
        setBorder(new TitledBorder("Simulacija letenja"));

        JButton startButton = new JButton("Pokreni");
        JButton pauseButton = new JButton("Pauziraj");
        JButton resetButton = new JButton("Resetuj");

        startButton.addActionListener(e -> onStart.run());
        pauseButton.addActionListener(e -> onPause.run());
        resetButton.addActionListener(e -> onReset.run());

        JSlider speedSlider = new JSlider(JSlider.HORIZONTAL, MIN_SPEED, MAX_SPEED, DEFAULT_SPEED);
        speedSlider.setPreferredSize(new Dimension(100, 20));
        speedSlider.setToolTipText("Brzina simulacije");
        speedSlider.addChangeListener(e -> onSpeedChange.accept(speedSlider.getValue()));

        JButton zoomResetButton = new JButton("Reset zuma");
        zoomResetButton.addActionListener(e -> onZoomReset.run());

        add(startButton);
        add(pauseButton);
        add(resetButton);
        add(new JLabel("Simulaciono vreme:"));
        add(clockLabel);
        add(new JLabel("Brzina:"));
        add(speedSlider);
        add(zoomResetButton);
    }

    public void setClockText(String formattedTime) {
        clockLabel.setText(formattedTime);
    }
}
