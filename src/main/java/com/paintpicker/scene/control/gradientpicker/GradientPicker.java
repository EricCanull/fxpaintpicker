package com.paintpicker.scene.control.gradientpicker;

/*
 * Copyright (c) 2012, 2014, Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import com.paintpicker.scene.control.gradientslider.GradientSlider;
import com.paintpicker.scene.control.picker.PaintDialogController;
import com.paintpicker.scene.control.rotator.RotatorControl;
import com.paintpicker.utils.ColorEncoder;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.input.MouseButton;

/**
 * Controller class for the gradient part of the paint editor.
 */
public class GradientPicker extends VBox {
    public enum GradientType {
        LINEAR, RADIAL
    }
    @FXML private Pane track_pane;
    @FXML private Rectangle preview_rect;
    @FXML private StackPane slider_container;
    @FXML private VBox radial_container;
    @FXML private Slider startX_slider;
    @FXML private Slider endX_slider;
    @FXML private Slider startY_slider;
    @FXML private Slider endY_slider;
    @FXML private Slider centerX_slider;
    @FXML private Slider centerY_slider;
    @FXML private GridPane gradientControlGrid;
    @FXML private CheckBox proportional_checkbox;
    @FXML private ChoiceBox<CycleMethod> cycleMethod_choicebox;
    @FXML private ChoiceBox<GradientType> gradient_choicebox;

    public static GradientType getGradientType() {
        return GRADIENT_TYPE.get();
    }

    public static void setGradientType(GradientType gradientType) {
        GradientPicker.GRADIENT_TYPE.set(gradientType);
    }

    private static final ObjectProperty<GradientType> GRADIENT_TYPE = new SimpleObjectProperty<>(GradientType.LINEAR);

    private final PaintDialogController paintPicker;

    private final RotatorControl focusAngleRotator
            = new RotatorControl("Degree"); //NOI18N
    private final GradientSlider focusDistanceSlider
            = new GradientSlider("Distance", -1.0, 1.0, 0.0); //NOI18N
    private final GradientSlider radiusSlider
            = new GradientSlider("Radius", 0.0, 1.0, 0.5); //NOI18N
    private final List<GradientPickerStop> gradientPickerStops = new ArrayList<>();

    public GradientPicker(PaintDialogController pe) {
        this.paintPicker = pe;
        initialize();
    }

    public final PaintDialogController getPaintPickerController() {
        return paintPicker;
    }

    /**
     * Private
     */
    private void initialize() {

        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(GradientPicker.class.getResource("/fxml/FXMLGradientPicker.fxml")); //NOI18N
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(GradientPicker.class.getName()).log(Level.SEVERE, null, ex);
        }

        assert proportional_checkbox != null;
        assert cycleMethod_choicebox != null;
        assert startX_slider != null;
        assert endX_slider != null;
        assert startY_slider != null;
        assert endY_slider != null;
        assert centerX_slider != null;
        assert centerY_slider != null;
        assert radial_container != null;

        // Add two default stops
        final GradientPickerStop initSelectedStop = addStop(0.0, 1.0, 0.0, Color.web("#42b0fe"));
        addStop(0.0, 1.0, 1.0, Color.web("#041626"));
        
        // Select first default stop
        setSelectedStop(initSelectedStop);
        
        proportional_checkbox.setSelected(true);
        proportional_checkbox.setOnAction(Event::consume);
        proportional_checkbox.selectedProperty().addListener(o -> onValueChange());

        cycleMethod_choicebox.setItems(FXCollections.observableArrayList(CycleMethod.values()));
        cycleMethod_choicebox.getSelectionModel().selectFirst();
        cycleMethod_choicebox.addEventHandler(ActionEvent.ACTION, (EventHandler<Event>) Event::consume);
        cycleMethod_choicebox.getSelectionModel().selectedItemProperty().addListener(o -> onValueChange());

        gradient_choicebox.setItems(FXCollections.observableArrayList(GradientType.values()));
        gradient_choicebox.setValue(GradientType.LINEAR);
        gradient_choicebox.setOnAction(event -> {
          setGradientType(gradient_choicebox.getSelectionModel().getSelectedItem());
            final Paint value = getValue();
            updateUI(value);
        });

        centerX_slider.visibleProperty().bind(centerY_slider.visibleProperty());
        centerY_slider.visibleProperty().bind(new ObjectBinding<Boolean>() {
            {bind(GRADIENT_TYPE);
            }
            @Override
            protected Boolean computeValue() {
                return GRADIENT_TYPE.get().equals(GradientType.RADIAL);
            }
        });

        endX_slider.visibleProperty().bind(startX_slider.visibleProperty());
        endY_slider.visibleProperty().bind(startX_slider.visibleProperty());
        startX_slider.visibleProperty().bind(startY_slider.visibleProperty());
        startY_slider.visibleProperty().bind(new ObjectBinding<Boolean>() {
            { bind(GRADIENT_TYPE);
            }
            @Override
            protected Boolean computeValue() {
                return GRADIENT_TYPE.get().equals(GradientType.LINEAR);
            }
        });

        startX_slider.valueProperty().addListener(o -> onValueChange());
        startY_slider.valueProperty().addListener(o -> onValueChange());
        endX_slider.valueProperty().addListener(o -> onValueChange());
        endY_slider.valueProperty().addListener(o -> onValueChange());

        centerX_slider.valueProperty().addListener(o -> onValueChange());
        centerY_slider.valueProperty().addListener(o -> onValueChange());
        focusAngleRotator.rotationProperty().addListener(o -> onValueChange());
        focusDistanceSlider.getSlider().valueProperty().addListener(o -> onValueChange());
        radiusSlider.getSlider().valueProperty().addListener(o -> onValueChange());

        gradientControlGrid.add(radiusSlider, 1, 1, 2, 1);
        gradientControlGrid.add(focusDistanceSlider, 1, 2, 2, 1);
        gradientControlGrid.add(focusAngleRotator, 1, 3, 2, 1);
        gradientControlGrid.visibleProperty().bind(centerY_slider.visibleProperty());

//        final ChangeListener<Boolean> liveUpdateListener = (ov, oldValue, newValue) -> paintPicker.liveUpdate.set(newValue);
//        startX_slider.pressedProperty().addListener(liveUpdateListener);
//        startY_slider.pressedProperty().addListener(liveUpdateListener);
//        endX_slider.pressedProperty().addListener(liveUpdateListener);
//        endY_slider.pressedProperty().addListener(liveUpdateListener);
//        centerX_slider.pressedProperty().addListener(liveUpdateListener);
//        centerY_slider.pressedProperty().addListener(liveUpdateListener);
//        radiusSlider.pressedProperty().addListener(liveUpdateListener);
//        focusDistanceSlider.pressedProperty().addListener(liveUpdateListener);
//        focusAngleRotator.pressedProperty().addListener(liveUpdateListener);
//        slider_container.pressedProperty().addListener(liveUpdateListener);
    }

    private void onValueChange() {
        final Paint value = getValue();
        // Update UI
        preview_rect.setFill(value);
        // Update model
        paintPicker.outputPaintProperty.set(value);
    }

    public Paint getValue() {
        final Paint paint;
        switch (GRADIENT_TYPE.get()) {
            case LINEAR:
                double startX = startX_slider.getValue();
                double startY = startY_slider.getValue();
                double endX = endX_slider.getValue();
                double endY = endY_slider.getValue();
                boolean linear_proportional = proportional_checkbox.isSelected();
                final CycleMethod linear_cycleMethod = cycleMethod_choicebox.getValue();
                paint = new LinearGradient(startX, startY, endX, endY,
                        linear_proportional, linear_cycleMethod, getStops());
                break;
            case RADIAL:
                double focusAngle = focusAngleRotator.getRotationProperty();
                double focusDistance = focusDistanceSlider.getSlider().getValue();
                double centerX = centerX_slider.getValue();
                double centerY = centerY_slider.getValue();
                double radius = radiusSlider.getSlider().getValue();
                boolean radial_proportional = proportional_checkbox.isSelected();
                final CycleMethod radial_cycleMethod = cycleMethod_choicebox.getValue();
                paint = new RadialGradient(focusAngle, focusDistance, centerX, centerY, radius,
                        radial_proportional, radial_cycleMethod, getStops());
                break;
            default:
                assert false;
                paint = null;
                break;
        }
        return paint;
    }

    public boolean isGradientStopsEmpty() {
        return gradientPickerStops.isEmpty();
    }

    public List<GradientPickerStop> getGradientStops() {
        return gradientPickerStops;
    }

    public GradientPickerStop getSelectedStop() {
        GradientPickerStop selectedThumb = null;
        for (GradientPickerStop gradientStopThumb : gradientPickerStops) {
            if (gradientStopThumb.isSelected()) {
                selectedThumb = gradientStopThumb;
            }
        }
        return selectedThumb;
    }

    public void updateUI(Paint value) {
        assert value instanceof LinearGradient || value instanceof RadialGradient;
        if (value instanceof LinearGradient) {
            final LinearGradient linear = (LinearGradient) value;
            startX_slider.setValue(linear.getStartX());
            startY_slider.setValue(linear.getStartY());
            endX_slider.setValue(linear.getEndX());
            endY_slider.setValue(linear.getEndY());
            proportional_checkbox.setSelected(linear.isProportional());
            cycleMethod_choicebox.setValue(linear.getCycleMethod());
            // clear first
            removeAllStops();
            linear.getStops().forEach((stop) ->
                // Update stops
                addStop(0.0, 1.0, stop.getOffset(), stop.getColor()));
        } else {
            final RadialGradient radial = (RadialGradient) value;
            centerX_slider.setValue(radial.getCenterX());
            centerY_slider.setValue(radial.getCenterY());
            focusAngleRotator.setRotationProperty(radial.getFocusAngle());
            focusDistanceSlider.getSlider().setValue(radial.getFocusDistance());
            radiusSlider.getSlider().setValue(radial.getRadius());
            proportional_checkbox.setSelected(radial.isProportional());
            cycleMethod_choicebox.setValue(radial.getCycleMethod());
            // clear first
            removeAllStops();
            radial.getStops().forEach((stop) -> {
                // Update stops
                addStop(0.0, 1.0, stop.getOffset(), stop.getColor());
            });
        }
        updatePreview(value);
    }

    public void updatePreview(Paint value) {
        preview_rect.setFill(value);
    }

    @FXML
    void onSliderClicked(MouseEvent event) {
        if (event.getButton().equals(MouseButton.PRIMARY)) {
            if (event.getClickCount() == 2) {
                double percentH = ((100.0 / track_pane.getWidth()) * event.getX()) / 100;
                final Color color = paintPicker.getCurrentColor();
                addStop(0.0, 1.0, percentH, color);
                updateAngle();
            }
        }
    }

    @FXML
    void onSliderDragged(MouseEvent event) {
        updateAngle();
    }

    void updateAngle() {
        final Paint value = getValue();
        // Update gradient preview rectangle
        preview_rect.setFill(value);
        // Update paint picker 
        paintPicker.outputPaintProperty.set(value);
    }

    GradientPickerStop addStop(double min, double max, double value, Color color) {
        int maxStops = 12;
        if (gradientPickerStops.size() < maxStops) {
            final GradientPickerStop gradientStop
                    = new GradientPickerStop(this, min, max, value, color);
            track_pane.getChildren().add(gradientStop);
            gradientPickerStops.add(gradientStop);
            return gradientStop;
        }
        return null;
    }

    void removeStop(GradientPickerStop gradientStop) {
        track_pane.getChildren().remove(gradientStop);
        gradientPickerStops.remove(gradientStop);
    }

    void removeAllStops() {
        track_pane.getChildren().clear();
        gradientPickerStops.clear();
    }

    public void setSelectedStop(GradientPickerStop gradientStop) {
        gradientPickerStops.forEach((stop) -> {
            stop.setSelected(false); // turn them all false
        });

        if (gradientStop != null) {
            gradientStop.setSelected(true);
            if (gradientStop.backgroundProperty().get() != null) {
                paintPicker.customColorProperty().set(gradientStop.getColor());
            }
        }
    }

    private List<Stop> getStops() {
        final List<Stop> stops = new ArrayList<>();
        getGradientStops().stream().map((stop) ->
                new Stop(stop.getOffset(), stop.getColor())).forEachOrdered(stops::add);
        return stops;
    }

    @FXML
    private void copyCSSAction() {
        if (GRADIENT_TYPE.get().equals(GradientType.RADIAL)) {
            System.out.println(ColorEncoder.encodeRadialToCSS(getValue()));
        } else {
            System.out.println(ColorEncoder.encodeLinearToCSS(getValue()));
        }
    }
}