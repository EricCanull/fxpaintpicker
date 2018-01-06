package com.paintpicker.scene.control.fields;

import com.paintpicker.utils.ColorEncoder;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;

/**
 */
public class WebColorFieldSkin extends InputFieldSkin {
    private final InvalidationListener integerFieldValueListener;

    /**
     * Create a new IntegerFieldSkin.
     * @param control The IntegerField
     */
    public WebColorFieldSkin(final WebColorField control) {
        super(control);

        // Whenever the value changes on the control, we need to update the text
        // in the TextField. The only time this is not the case is when the update
        // to the control happened as a result of an update in the text textField.
        control.valueProperty().addListener(integerFieldValueListener = (Observable observable) -> {
            updateText();
        });
    }

    @Override public WebColorField getSkinnable() {
        return (WebColorField) control;
    }

    @Override public Node getNode() {
        return getTextField();
    }

    /**
     * Called by a Skinnable when the Skin is replaced on the Skinnable. This method
     * allows a Skin to implement any logic necessary to clean up itself after
     * the Skin is no longer needed. It may be used to release native resources.
     * The methods {@link #getSkinnable()} and {@link #getNode()}
     * should return null following a call to dispose. Calling dispose twice
     * has no effect.
     */
    @Override public void dispose() {
        ((WebColorField) control).valueProperty().removeListener(integerFieldValueListener);
        super.dispose();
    }

    @Override
    protected boolean accept(String text) {
        if (text.length() == 0) return true;
        return text.matches("#[A-F0-9]{6}") || text.matches("#[A-F0-9]{8}");
    }

    @Override
    protected void updateText() {
        if (((WebColorField) control).getValue() instanceof LinearGradient
                || ((WebColorField) control).getValue() instanceof RadialGradient) {
            getTextField().setText("Gradient");
        } else {
            Color color = (Color) ((WebColorField) control).getValue();
            if (color == null) {
                color = Color.BLACK;
            }
            getTextField().setText(ColorEncoder.encodeColor(color));
        }
    }

    @Override
    protected void updateValue() {
        if (((WebColorField) control).getValue() instanceof LinearGradient
                || ((WebColorField) control).getValue() instanceof RadialGradient) {
            return;
        }
        Color value = (Color) ((WebColorField) control).getValue();
        String text = getTextField().getText() == null ? "" : getTextField().getText().trim().toUpperCase();
        if (text.matches("#[A-F0-9]{6}") || text.matches("#[A-F0-9]{8}")) {
            try {
                Color newValue = Color.web(text);
                if (!newValue.equals(value)) {
                    ((WebColorField) control).setValue(newValue);
                }
            } catch (java.lang.IllegalArgumentException ex) {
                System.out.println("Failed to parse [" + text + "]");
            }
        }
    }
}
