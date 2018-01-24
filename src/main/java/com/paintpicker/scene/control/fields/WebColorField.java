package com.paintpicker.scene.control.fields;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Paint;

/**
 *
 */
public class WebColorField extends InputField {
    /**
     * The value of the WebColorField. If null, the value will be treated as "#000000" black, but
     * will still actually be null.
     */
    private final ObjectProperty<Paint> value = new SimpleObjectProperty<>(this, "value");
    public final Paint getValue() { return value.get(); }
    public final void setValue(Paint value) { this.value.set(value); }
    public final ObjectProperty<Paint> valueProperty() { return value; }

    /**
     * Creates a new WebColorField. The style class is set to "webcolor-field".
     */
    public WebColorField() {
        getStyleClass().setAll("webcolor-field");
    }
}