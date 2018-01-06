package com.paintpicker.scene.control.fields;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 *
 */
public class IntegerField extends InputField {
    /**
    /**
     * The value of the IntegerField. If null, the value will be treated as "0", but
     * will still actually be null.
     */
    private final IntegerProperty value = new SimpleIntegerProperty(this, "value");
    public final int getValue() { return value.get(); }

    public final void setValue(int value) {
        value = value < 0 ? 0 : value > maxValue.get() ? maxValue.get() : value;
        this.value.set(value);
    }
    public final IntegerProperty valueProperty() { return value; }

    private final IntegerProperty maxValue = new SimpleIntegerProperty(this, "maxValue", -1);
    public final int getMaxValue() { return maxValue.get(); }
    public final void setMaxValue(int maxVal) {this.maxValue.set(maxVal); }
    public final IntegerProperty maxValueProperty() { return maxValue; }
    /**
     * Creates a new IntegerField. The style class is set to "money-field".
     */
    public IntegerField() {
       this(-1); // setting to default
    }
    public IntegerField(int maxVal) {
        getStyleClass().setAll("integer-field");
        setMaxValue(maxVal);
    }  
}