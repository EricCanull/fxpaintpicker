/*
 * Copyright (c) 2012, 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.paintpicker.scene.control.picker;

import com.paintpicker.scene.control.gradientpicker.GradientDialog;
import com.paintpicker.scene.control.fields.IntegerField;
import com.paintpicker.scene.control.fields.WebColorField;
import com.paintpicker.scene.control.gradientpicker.GradientControl;
import com.paintpicker.scene.control.gradientpicker.GradientPickerStop;
import com.paintpicker.scene.control.slider.PaintSlider;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

/**
 *
 *
 */
public class CustomPaintControl extends AnchorPane {
    
    @FXML private GridPane colorPickerGrid;
    @FXML private IntegerField hueTextField;
    @FXML private IntegerField satTextField;
    @FXML private IntegerField brightTextField;
    @FXML private IntegerField redTextField;
    @FXML private IntegerField greenTextField;
    @FXML private IntegerField blueTextField;
    @FXML private WebColorField hexTextField;
    @FXML private IntegerField alphaTextField;
    @FXML private Pane hueBar;
    @FXML private Region hueBarHandle;
    @FXML private StackPane colorRect;
    @FXML private StackPane alphaPane;
    @FXML private Pane colorRectHue;
    @FXML private Pane colorRectOverlayOne;
    @FXML private Pane colorRectOverlayTwo;
    @FXML private Region circleHandle;
    @FXML private Region previousColorRect;
    @FXML private Region currentColorRect;
    
    private final PaintSlider[] sliders = new PaintSlider[7];

    private final CustomPaintDialog customPaintDialog;

    private GradientDialog gradientDialog = null;
    
    private GradientControl gradientPicker = null;

    public CustomPaintControl(CustomPaintDialog customPaintDialog) {
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(CustomPaintControl.class.getResource("/fxml/FXMLCustomPaintControl.fxml"));
        loader.setController(CustomPaintControl.this);
        loader.setRoot(CustomPaintControl.this);

        try {
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(CustomPaintDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        this.customPaintDialog = customPaintDialog;
        initialize();
    }

    private void initialize() {

        IntStream.range(0, sliders.length).forEachOrdered(index -> {
            sliders[index] = new PaintSlider();
            sliders[index].getStyleClass().add("controls-paint-slider");

            int row = index > 2 ? index + 1 : index; // skips row 4
            GridPane.setConstraints(sliders[index], 6, row, 6, 1,
                    HPos.LEFT, VPos.CENTER, Priority.NEVER, Priority.ALWAYS);
            colorPickerGrid.getChildren().add(sliders[index]);
        });

        bindControlsValue(0, 360, hueProperty);
        bindControlsValue(1, 100, satProperty);
        bindControlsValue(2, 100, brightProperty);
        bindControlsValue(3, 255, redProperty);
        bindControlsValue(4, 255, greenProperty);
        bindControlsValue(5, 255, blueProperty);
        bindControlsValue(6, 100, alphaProperty);

        hueTextField.setMaxValue(360);
        satTextField.setMaxValue(100);
        brightTextField.setMaxValue(100);
        greenTextField.setMaxValue(255);
        redTextField.setMaxValue(255);
        blueTextField.setMaxValue(255);
        alphaTextField.setMaxValue(100);

        hueTextField.valueProperty().bindBidirectional(sliders[0].valueProperty());
        satTextField.valueProperty().bindBidirectional(sliders[1].valueProperty());
        brightTextField.valueProperty().bindBidirectional(sliders[2].valueProperty());
        redTextField.valueProperty().bindBidirectional(sliders[3].valueProperty());
        greenTextField.valueProperty().bindBidirectional(sliders[4].valueProperty());
        blueTextField.valueProperty().bindBidirectional(sliders[5].valueProperty());
        alphaTextField.valueProperty().bindBidirectional(sliders[6].valueProperty());
        hexTextField.valueProperty().bindBidirectional(customPaintDialog.customColorProperty);
        
        customPaintDialog.customColorProperty.addListener(o -> colorChanged());
        
        colorRectHue.backgroundProperty().bind(new ObjectBinding<Background>() {
            {
                bind(hueProperty);
            }

            @Override
            protected Background computeValue() {
                return new Background(new BackgroundFill(
                        Color.hsb(hueProperty.getValue(), 1.0, 1.0),
                        CornerRadii.EMPTY, Insets.EMPTY));
            }
        });

        colorRectOverlayOne.setBackground(new Background(new BackgroundFill(
                new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(255, 255, 255, 1)),
                        new Stop(1, Color.rgb(255, 255, 255, 0))),
                CornerRadii.EMPTY, Insets.EMPTY)));

        EventHandler<MouseEvent> rectMouseHandler = event -> {
            final double x = event.getX();
            final double y = event.getY();
            satProperty.set(clamp(x / colorRect.getWidth()) * 100);
            brightProperty.set(100 - (clamp(y / colorRect.getHeight()) * 100));
        };

        colorRectOverlayTwo.setBackground(new Background(new BackgroundFill(
                new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(0, 0, 0, 0)),
                        new Stop(1, Color.rgb(0, 0, 0, 1))),
                CornerRadii.EMPTY, Insets.EMPTY)));
        colorRectOverlayTwo.setOnMouseDragged(rectMouseHandler);
        colorRectOverlayTwo.setOnMousePressed(rectMouseHandler);

        // hueProperty bar
        hueBar.setBackground(new Background(new BackgroundFill(setHueGradient(),
                CornerRadii.EMPTY, Insets.EMPTY)));

        circleHandle.layoutXProperty().bind(satProperty.divide(100).multiply(colorRect.widthProperty()));
        circleHandle.layoutYProperty().bind(Bindings.subtract(1, brightProperty.divide(100)).multiply(colorRect.heightProperty()));
        hueBarHandle.layoutYProperty().bind(hueProperty.divide(360).multiply(hueBar.heightProperty()));
        alphaPane.opacityProperty().bind(alphaProperty.divide(100));

        EventHandler<MouseEvent> barMouseHandler = event -> {
            final double y = event.getY();
            hueProperty.set(clamp(y / hueBar.getHeight()) * 360);
        };

        hueBar.setOnMouseDragged(barMouseHandler);
        hueBar.setOnMousePressed(barMouseHandler);

        currentColorRect.backgroundProperty().bind(new ObjectBinding<Background>() {
            {
                bind(customPaintDialog.currentColorProperty);
            }

            @Override
            protected Background computeValue() {
                return new Background(new BackgroundFill(customPaintDialog.currentColorProperty.get(),
                        CornerRadii.EMPTY, Insets.EMPTY));
            }
        });
        previousColorRect.backgroundProperty().bind(new ObjectBinding<Background>() {
            {
                bind(customPaintDialog.customColorProperty);
            }

            @Override
            protected Background computeValue() {
                return new Background(new BackgroundFill(customPaintDialog.customColorProperty.get(),
                        CornerRadii.EMPTY, Insets.EMPTY));
            }
        });
    }

    @Override
    public void layoutChildren() {
        super.layoutChildren();

        circleHandle.setManaged(false);
        circleHandle.autosize();
    }
    
     protected void initializeGradientDialog() {
        if (gradientPicker == null) {
            gradientPicker = new GradientControl(customPaintDialog);
        }
        gradientDialog = new GradientDialog(this.getScene().getWindow(), gradientPicker);
    }
     
    private final Property[] boundProperties = new Property[7];

    private void bindControlsValue(int index, int maxValue, Property<Number> prop) {

        if (boundProperties[index] != null) {
            sliders[index].valueProperty().unbindBidirectional(boundProperties[index]);
        }

        sliders[index].valueProperty().bindBidirectional(prop);
        sliders[index].setMax(maxValue);

        boundProperties[index] = prop;
    }

    private boolean changeIsLocal = false;

    private final DoubleProperty hueProperty = new SimpleDoubleProperty(-1) {
        @Override
        protected void invalidated() {
            if (!changeIsLocal) {
                changeIsLocal = true;
                updateHSBColor();
                changeIsLocal = false;
            }
        }
    };
    private final DoubleProperty satProperty = new SimpleDoubleProperty(100) {
        @Override
        protected void invalidated() {
            if (!changeIsLocal) {
                changeIsLocal = true;
                updateHSBColor();
                changeIsLocal = false;
            }
        }
    };
    private final DoubleProperty brightProperty = new SimpleDoubleProperty(100) {
        @Override
        protected void invalidated() {
            if (!changeIsLocal) {
                changeIsLocal = true;
                updateHSBColor();
                changeIsLocal = false;
            }
        }
    };
    private final IntegerProperty redProperty = new SimpleIntegerProperty(-1) {
        @Override
        protected void invalidated() {
            if (!changeIsLocal) {
                changeIsLocal = true;
                updateRGBAColor();
                changeIsLocal = false;
            }
        }
    };

    private final IntegerProperty greenProperty = new SimpleIntegerProperty(-1) {
        @Override
        protected void invalidated() {
            if (!changeIsLocal) {
                changeIsLocal = true;
                updateRGBAColor();
                changeIsLocal = false;
            }
        }
    };

    private final IntegerProperty blueProperty = new SimpleIntegerProperty(-1) {
        @Override
        protected void invalidated() {
            if (!changeIsLocal) {
                changeIsLocal = true;
                updateRGBAColor();
                changeIsLocal = false;
            }
        }
    };

    private final DoubleProperty alphaProperty = new SimpleDoubleProperty(100) {
        @Override
        protected void invalidated() {
            if (!changeIsLocal) {
                changeIsLocal = true;
                updateRGBAColor();
                changeIsLocal = false;
            }
        }
    };

    private void updateRGBAColor() {
        Color newColor = Color.rgb(
                redProperty.get(),
                greenProperty.get(),
                blueProperty.get(),
                clamp(alphaProperty.divide(100).get()));
        hueProperty.set(newColor.getHue());
        satProperty.set(newColor.getSaturation() * 100);
        brightProperty.set(newColor.getBrightness() * 100);
        alphaProperty.set(newColor.getOpacity() * 100);
        customPaintDialog.customColorProperty.set(newColor);
        updateSlidersTrackColors();
        updateSelectedGradientStop(newColor);
    }

    private void updateHSBColor() {
        Color newColor = Color.hsb(
                hueProperty.get(),
                clamp(satProperty.get() / 100),
                clamp(brightProperty.get() / 100),
                clamp(alphaProperty.get() / 100));
        redProperty.set(doubleToInt(newColor.getRed()));
        greenProperty.set(doubleToInt(newColor.getGreen()));
        blueProperty.set(doubleToInt(newColor.getBlue()));
        alphaProperty.set(newColor.getOpacity() * 100);
        customPaintDialog.customColorProperty.set(newColor);
        updateSlidersTrackColors();
        updateSelectedGradientStop(newColor);
    }

    private void updateSlidersTrackColors() {
        double hue = hueProperty.get();
        double sat = satProperty.get() / 100;
        double bright = brightProperty.get() / 100;
        int red = redProperty.get();
        int green = greenProperty.get();
        int blue = blueProperty.get();
        double alpha = alphaProperty.get() / 100;

        Color newColor = Color.rgb(red, green, blue, alpha);
        sliders[0].setGradientForHueWithSaturation(sat, bright, alpha);
        sliders[1].setGradientForSaturationWithHue(hue, bright, alpha);
        sliders[2].setGradientForBrightnessWithHue(hue, sat, alpha);
        sliders[3].setGradientForRedWithGreen(green, blue, alpha);
        sliders[4].setGradientForGreenWithRed(blue, red, alpha);
        sliders[5].setGradientForBlueWithRed(green, red, alpha);
        sliders[6].setGradientForAlphaWithCurrentColor(newColor);
    }

    /**
     * Update hueProperty, satProperty, brightProperty, color, redProperty,
     * greenProperty and blueProperty automatically every time the values
     * change.
     */
    private void updateSelectedGradientStop(Color newColor) {
        if (gradientDialog.isShowing() && gradientDialog != null) {
            GradientPickerStop gradientPickerStop = gradientPicker.getSelectedStop();
            if (gradientPickerStop != null) {
                gradientPickerStop.setColor(newColor);
                // updateValues(newColor);
                // Update gradient preview
                final Paint paint = gradientPicker.getValue();
                gradientPicker.updatePreview(paint);
                //gradientPicker.updateUI(paint);
            }
        }
    }

    private void colorChanged() {
        if (!changeIsLocal) {
            changeIsLocal = true;
            hueProperty.set(customPaintDialog.getCustomColor().getHue());
            satProperty.set(customPaintDialog.getCustomColor().getSaturation() * 100);
            brightProperty.set(customPaintDialog.getCustomColor().getBrightness() * 100);
            redProperty.set(doubleToInt(customPaintDialog.getCustomColor().getRed()));
            greenProperty.set(doubleToInt(customPaintDialog.getCustomColor().getGreen()));
            blueProperty.set(doubleToInt(customPaintDialog.getCustomColor().getBlue()));
            alphaProperty.set(customPaintDialog.getCustomColor().getOpacity() * 100);
            updateSlidersTrackColors();
            changeIsLocal = false;
        }
    }

    /**
     * Updates hueProperty, satProperty, brightProperty, color, redProperty,
     * greenProperty and blueProperty and slider background colors whenever the
     * show() method for this dialog gets called.
     */
    protected void updateValues() {
        if (customPaintDialog.getCurrentColor() == null) {
            customPaintDialog.setCurrentColor(Color.TRANSPARENT);
        }
        changeIsLocal = true;
        //Initialize hue, sat, bright, color, red, green and blue
        hueProperty.set(customPaintDialog.getCustomColor().getHue());
        satProperty.set(customPaintDialog.getCustomColor().getSaturation() * 100);
        brightProperty.set(customPaintDialog.getCustomColor().getBrightness() * 100);
        alphaProperty.set(customPaintDialog.getCustomColor().getOpacity() * 100);
        customPaintDialog.setCustomColor(Color.hsb(hueProperty.get(), 
                clamp(satProperty.get() / 100), 
                clamp(brightProperty.get() / 100),
                clamp(alphaProperty.get() / 100)));
        redProperty.set(doubleToInt(customPaintDialog.getCustomColor().getRed()));
        greenProperty.set(doubleToInt(customPaintDialog.getCustomColor().getGreen()));
        blueProperty.set(doubleToInt(customPaintDialog.getCustomColor().getBlue()));
        updateSlidersTrackColors();
        changeIsLocal = false;
    }
    
    /**
     * @param e
     */
    @FXML
    private void onGradientButtonAction(ActionEvent e) {
        if (gradientDialog.isShowing() == false) {
            gradientDialog.show();
        } else {
            gradientDialog.hide();
        }
    }

    /**
     * @param event
     */
    @FXML
    private void onSaveButtonAction(ActionEvent event) {
        if (customPaintDialog.onSave != null) {
            customPaintDialog.onSave.run();
        }
        customPaintDialog.hide();
    }

    /**
     * 
     * @param e 
     */
    @FXML
    private void onCancelButtonAction(ActionEvent e) {
        customPaintDialog.customColorProperty.set(customPaintDialog.getCurrentColor());
        if (customPaintDialog.getOnCancel() != null) {
            customPaintDialog.getOnCancel().run();
        }
        customPaintDialog.hide();
    }

    private void setMode(Paint value) {
        if (value instanceof Color) {

        } else if (value instanceof LinearGradient) {
//            // make sure that a second click doesn't deselect the button
//            if (linearToggleButton.isSelected() == false) {
//                linearToggleButton.setSelected(true);
//            }
//            if (!root_vbox.getChildren().contains(gradientPicker)) {
//                root_vbox.getChildren().add(gradientPicker);
//            }
        } else if (value instanceof RadialGradient) {
//            // make sure that a second click doesn't deselect the button
//            if (radialToggleButton.isSelected() == false) {
//                radialToggleButton.setSelected(true);
//            }
        }
    }

    /**
     * @param value
     * @return
     */
    private double clamp(double value) {
        return value < 0 ? 0 : value > 1 ? 1 : value;
    }

    private LinearGradient setHueGradient() {
        Stop[] stops = new Stop[255];
        double offset;

        for (int y = 0; y < 255; y++) {
            offset = 1 - (1.0 / 255) * y;
            int h = (int) ((y / (double) 255) * 360);
            stops[y] = new Stop(offset, Color.hsb(h, 1.0, 1.0));
        }
        return new LinearGradient(0f, 1f, 0f, 0f, true, CycleMethod.NO_CYCLE, stops);
    }

    /**
     * @param value
     * @return
     */
    private int doubleToInt(double value) {
        return (int) (value * 255 + 0.5); // Adding 0.5 for rounding only
    }
}
