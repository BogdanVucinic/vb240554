package gui;

import java.awt.Point;
import java.awt.geom.Point2D;

public class CoordinateMapper {

    private static final double WORLD_MIN_X = -180.0;
    private static final double WORLD_MAX_X = 180.0;
    private static final double WORLD_MIN_Y = -90.0;
    private static final double WORLD_MAX_Y = 90.0;

    private static final double MIN_SPAN_X = 10.0; // najmanji dozvoljeni opseg pri zumiranju
    private static final double MIN_SPAN_Y = 5.0;

    private static final int PADDING = 20; //margina

    private int panelWidth;
    private int panelHeight;

    private double minX = WORLD_MIN_X;
    private double maxX = WORLD_MAX_X;
    private double minY = WORLD_MIN_Y;
    private double maxY = WORLD_MAX_Y;

    public CoordinateMapper(int panelWidth, int panelHeight) {
        updatePanelSize(panelWidth, panelHeight);
    }

    public void updatePanelSize(int panelWidth, int panelHeight) {
        this.panelWidth = Math.max(panelWidth, 2 * PADDING + 1);
        this.panelHeight = Math.max(panelHeight, 2 * PADDING + 1);
    }

    public Point toScreen(double logicalX, double logicalY) {
        double usableWidth = panelWidth - 2.0 * PADDING;
        double usableHeight = panelHeight - 2.0 * PADDING;

        double normalizedX = (logicalX - minX) / (maxX - minX); // 0..1
        double normalizedY = (logicalY - minY) / (maxY - minY); // 0..1

        int screenX = PADDING + (int) Math.round(normalizedX * usableWidth);
        int screenY = PADDING + (int) Math.round((1 - normalizedY) * usableHeight);

        return new Point(screenX, screenY);
    }

    public void zoomIn(int screenX, int screenY) {
        Point2D.Double focus = toLogical(screenX, screenY);
        zoomByFactor(0.8, focus.x, focus.y);
    }

    public void zoomOut(int screenX, int screenY) {
        Point2D.Double focus = toLogical(screenX, screenY);
        zoomByFactor(1.25, focus.x, focus.y);
    }

    public void resetZoom() {
        minX = WORLD_MIN_X;
        maxX = WORLD_MAX_X;
        minY = WORLD_MIN_Y;
        maxY = WORLD_MAX_Y;
    }

    private Point2D.Double toLogical(int screenX, int screenY) {
        double usableWidth = panelWidth - 2.0 * PADDING;
        double usableHeight = panelHeight - 2.0 * PADDING;

        double normalizedX = (screenX - PADDING) / usableWidth;
        double normalizedY = 1.0 - (screenY - PADDING) / usableHeight;

        double logicalX = minX + normalizedX * (maxX - minX);
        double logicalY = minY + normalizedY * (maxY - minY);

        return new Point2D.Double(logicalX, logicalY);
    }

    // Zumira tako da tacka (focusX, focusY) ostane na istoj relativnoj poziciji u novom opsegu,
    // tj. zum se "dešava" tacno oko te tacke, a ne oko centra mape.
    private void zoomByFactor(double factor, double focusX, double focusY) {
        double newSpanX = clamp((maxX - minX) * factor, MIN_SPAN_X, WORLD_MAX_X - WORLD_MIN_X);
        double newSpanY = clamp((maxY - minY) * factor, MIN_SPAN_Y, WORLD_MAX_Y - WORLD_MIN_Y);

        double fracX = (focusX - minX) / (maxX - minX);
        double fracY = (focusY - minY) / (maxY - minY);

        minX = clamp(focusX - fracX * newSpanX, WORLD_MIN_X, WORLD_MAX_X - newSpanX);
        maxX = minX + newSpanX;
        minY = clamp(focusY - fracY * newSpanY, WORLD_MIN_Y, WORLD_MAX_Y - newSpanY);
        maxY = minY + newSpanY;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
