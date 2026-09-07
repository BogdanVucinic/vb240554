package gui;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class InactivityWarningDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final JLabel messageLabel = new JLabel("", SwingConstants.CENTER);

    public InactivityWarningDialog(Frame owner, Runnable onContinue) {
        super(owner, "Upozorenje - neaktivnost", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JButton continueButton = new JButton("Nastavi rad");
        continueButton.addActionListener(e -> {
            onContinue.run();
            dispose();
        });

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20));
        content.add(messageLabel, BorderLayout.CENTER);
        content.add(continueButton, BorderLayout.SOUTH);

        setContentPane(content);
        setResizable(false);
        setSize(360, 130);
        setLocationRelativeTo(owner);
    }

    public void setSecondsRemaining(int seconds) {
        messageLabel.setText("<html><div style='text-align:center;'>Program ce se automatski zatvoriti za "
                + seconds + " sekundi zbog neaktivnosti.<br>Da li zelite da nastavite rad?</div></html>");
    }
}