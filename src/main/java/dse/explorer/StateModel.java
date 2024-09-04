package dse.explorer;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.chart.XYChart;

import java.util.Arrays;

public class StateModel {

    private static final int MARGINS = 50;
    private int conversion = 100;
    private int elements = 1000;
    private int elementCounter;
    private double avg;

    private int averageOver = 50;
    private int movingAverageCounter;
    private int[] movingAverageArray;

    protected IntegerProperty measurementValue = new SimpleIntegerProperty();
    protected IntegerProperty averageValue = new SimpleIntegerProperty();
    protected IntegerProperty minimumValue = new SimpleIntegerProperty();
    protected IntegerProperty maximumValue = new SimpleIntegerProperty();
    protected IntegerProperty frequency = new SimpleIntegerProperty();
    //protected StringProperty message = new SimpleStringProperty();
    protected IntegerProperty lowerBound = new SimpleIntegerProperty();
    protected IntegerProperty upperBound = new SimpleIntegerProperty();

    protected final XYChart.Series<Number, Number> numberSeries1 = new XYChart.Series<>();
    //protected final XYChart.Series<Number, Number> numberSeries2 = new XYChart.Series<>();

    public void setConversion(int conversion) {
        this.conversion = conversion;
    }

    public void setAverageOver(int averageOver) {
        movingAverageCounter = 0;
        this.averageOver = averageOver;
        movingAverageArray = new int[averageOver];
    }

    public void setElements(int elements) {
        this.elements = elements;
    }

    public void setFrequency(int freq) {
        Platform.runLater(() -> {
            frequency.setValue(freq);
        });
    }

    public void setMeasurement(Integer measurement) {


        Platform.runLater(() -> {
            movingAverageArray[movingAverageCounter++] = measurement;
            avg = Arrays.stream(movingAverageArray).average().orElse(measurement);
            if(movingAverageCounter >= averageOver) {
                averageValue.setValue(avg);
                minimumValue.setValue(Arrays.stream(movingAverageArray).min().orElse(measurement));
                maximumValue.setValue(Arrays.stream(movingAverageArray).max().orElse(measurement));
                movingAverageCounter = 0;
            }
            this.measurementValue.setValue(avg);

            numberSeries1.getData().add(new XYChart.Data<>(elementCounter, avg / conversion));
            //numberSeries2.getData().add(new XYChart.Data<>(elementCounter, averageValue.getValue()));
            if(numberSeries1.getData().size() > elements) { numberSeries1.getData().remove(0, 5); }
            //if(numberSeries2.getData().size() > elements) { numberSeries2.getData().remove(0, 5); }

            elementCounter++;
            if(elementCounter > elements) {
                if(lowerBound.get() < avg / conversion - MARGINS) {
                    lowerBound.setValue(avg / conversion - MARGINS);
                    System.out.println("Lowerbound adjust");
                }
                if(upperBound.get() < (avg / conversion) + MARGINS) {
                    upperBound.setValue(avg / conversion + MARGINS);
                    System.out.println("Upperbound adjust");
                }
                //lastErrorMessage.setText("");
                //xAxis.autoRangingProperty().set(false);
                //xAxis.setUpperBound(pointsCounter);
                elementCounter = 0;
            }

        });
    }

}
