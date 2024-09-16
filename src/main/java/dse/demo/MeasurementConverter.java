package dse.demo;

import javafx.util.StringConverter;


public class MeasurementConverter extends StringConverter<Number> {

    private final int factor;

    MeasurementConverter(int factor) {
        this.factor = factor;
    }

    @Override
    public String toString(Number object) {
        float converted = object.floatValue() / factor;
        return String.format("%.2f", converted);
    }

    @Override
    public Number fromString(String string) {
        return null;
    }
}
