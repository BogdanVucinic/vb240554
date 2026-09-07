package model;

import java.util.Objects;

public class Airport {

    private String code;
    private String name;
    private double x;
    private double y;

    public Airport(String code, String name, double x, double y) {
        this.code = code;
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }
    // Ako su im kodovi isti smatraju se istim
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Airport)) {
            return false;
        }
        Airport other = (Airport) obj;
        return Objects.equals(code, other.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return code + " - " + name + " (" + x + ", " + y + ")";
    }
}