package gui;

import java.awt.Frame;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class InactivityTimer {

    private static final int TOTAL_SECONDS = 60;
    private static final int WARNING_THRESHOLD_SECONDS = 5;

    private final Frame ownerFrame;
    private final Runnable onTimeout;
    private final Timer swingTimer;

    private int remainingSeconds = TOTAL_SECONDS;
    private InactivityWarningDialog warningDialog;
    private int pauseCount = 0; //brojac umesto booleana zato sto vise klasa koristi inactivity timer
    
    public InactivityTimer(Frame ownerFrame, Runnable onTimeout) {
        this.ownerFrame = ownerFrame;
        this.onTimeout = onTimeout;
        this.swingTimer = new Timer(1000, e -> tick());
    }

    public void start() {
        remainingSeconds = TOTAL_SECONDS;
        swingTimer.start();
    }

    public void reset() {
        remainingSeconds = TOTAL_SECONDS;
        closeWarningDialogIfOpen();
    }

    public void pause() {
        pauseCount++;
    }

    public void resume() {
        if(pauseCount>0) {
        	pauseCount--;
        }
        if(pauseCount==0) {
        	reset();
        }
    }

    private void tick() {
        if (pauseCount>0) {
            return;
        }

        remainingSeconds--;

        if (remainingSeconds <= 0) {
            swingTimer.stop();
            closeWarningDialogIfOpen();
            onTimeout.run();
            return;
        }

        if (remainingSeconds <= WARNING_THRESHOLD_SECONDS) {
            showOrUpdateWarningDialog();
        }
    }

    private void showOrUpdateWarningDialog() {
        if (warningDialog == null) {
            warningDialog = new InactivityWarningDialog(ownerFrame, this::reset);
            warningDialog.setSecondsRemaining(remainingSeconds);
            SwingUtilities.invokeLater(() -> {
                if (warningDialog != null) {
                    warningDialog.setVisible(true);
                }
            });
        } else {
            warningDialog.setSecondsRemaining(remainingSeconds);
        }
    }

    private void closeWarningDialogIfOpen() {
        if (warningDialog != null) {
            warningDialog.dispose();
            warningDialog = null;
        }
    }
}