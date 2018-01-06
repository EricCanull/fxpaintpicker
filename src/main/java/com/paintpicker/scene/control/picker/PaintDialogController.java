/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.paintpicker.scene.control.picker;
import com.paintpicker.scene.control.fields.IntegerField;
import com.paintpicker.scene.control.fields.WebColorField;
import com.paintpicker.scene.control.gradientpicker.GradientPicker;
import com.paintpicker.scene.control.gradientpicker.GradientPickerStop;
import com.paintpicker.scene.control.picker.comboboxmode.Mode;
import com.paintpicker.scene.control.slider.PaintSlider;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.*;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PopupControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Window;

/**
 * FXML Controller class
 *
 * @author Eric Canull
 */
public class PaintDialogController extends AnchorPane {

    private final Scene customScene;

    private Runnable onSave;
    private Runnable onSelect;
    private Runnable onCancel;

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
    @FXML private Button saveButton;
    @FXML private Button popupButton;
    
    private PopupControl popupControl;
    private GradientPopover popOver;
    
    private final PaintPicker paintPicker;
    private GradientPicker gradientPicker = null;

    private final PaintSlider[] sliders = new PaintSlider[7];
    
    public final ObjectProperty<Mode> mode = new SimpleObjectProperty<>(Mode.SINGLE);
    public final ObjectProperty<Paint> outputPaintProperty = new SimpleObjectProperty<>(Color.WHITE);
    private final ObjectProperty<Paint> customColorProperty = new SimpleObjectProperty<>(Color.BLACK);
    private final ObjectProperty<Paint> currentColorProperty = new SimpleObjectProperty<>(Color.WHITE);

    private final Stage stage = new Stage();

    public PaintDialogController(PopupControl owner, PaintPicker paintPicker) {
        this.paintPicker = paintPicker;
        initialize();

        if (owner != null) {
            stage.initOwner(owner);
        }

        stage.setTitle("Custom Paints");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(true);
        stage.addEventHandler(KeyEvent.ANY, keyEventListener);
 
        customScene = new Scene(this);

        stage.setScene(customScene);
        initializeGradientPopup();

        stage.setOnCloseRequest(e -> {
            if (onSave != null) {
                onSave.run();
            }
        });
    }

    /**
     * Initializes the controller class.
     */
    private void initialize() {
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(PaintDialogController.class.getResource("/fxml/FXMLPaintDialog.fxml"));
        loader.setController(this);
        loader.setRoot(this);

        try {
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(PaintDialogController.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        mode.set(paintPicker.getMode());


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
        hexTextField.valueProperty().bindBidirectional(customColorProperty);
      
        customColorProperty.addListener(o -> colorChanged());
     
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
                bind(currentColorProperty);
            }

            @Override
            protected Background computeValue() {
                return new Background(new BackgroundFill(currentColorProperty.get(),
                        CornerRadii.EMPTY, Insets.EMPTY));
            }
        });
        previousColorRect.backgroundProperty().bind(new ObjectBinding<Background>() {
            {
                bind(customColorProperty);
            }

            @Override
            protected Background computeValue() {
                return new Background(new BackgroundFill(customColorProperty.get(),
                        CornerRadii.EMPTY, Insets.EMPTY));
            }
        });
    }
    
    private void initializeGradientPopup() {
          gradientPicker = new GradientPicker(this);
        popOver = new GradientPopover(stage.getScene().getWindow(), gradientPicker);

        popupButton.setOnAction(e -> {
            if (popOver.isShowing() == false) {
                   popOver.show();
            } else {
                popOver.hide();
            }
        });
    }

    private final EventHandler<KeyEvent> keyEventListener = e -> {
        if (e.getCode().equals(KeyCode.ESCAPE)) {
            stage.close();
        }
    };

    @FXML
    private void onCancelButtonAction(ActionEvent e) {
        customColorProperty.set(getCurrentColor());
        if (onCancel != null) {
            onCancel.run();
        }
        stage.hide();

    }

    public Runnable getOnSave() {
        return onSave;
    }

    // JDK-8161449
    public void setSaveBtnToOk() {
        stage.show();
    }

    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }

    public Runnable getOnUse() {
        return onSelect;
    }

    public void setOnUse(Runnable onUse) {
        this.onSelect = onUse;
    }

    // JDK-8161449
    public void setShowUseBtn(boolean showUseBtn) {
//        this.showSelectBtn = showUseBtn;
        stage.show();
    }

    public Runnable getOnCancel() {
        return onCancel;
    }

    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    public void setOnHidden(EventHandler<WindowEvent> onHidden) {
        stage.setOnHidden(onHidden);
    }

    public Stage getDialog() {
        return stage;
    }
        
    @FXML
    public void onSaveButtonAction(ActionEvent event) {
        if (onSave != null) {
            onSave.run();
        }
        updateValues();
        stage.hide();
    }

    public void hide() {
        if (stage.getOwner() != null) {
            stage.hide();
        }
    }

    public void show() {
        if (stage.getOwner() != null) {
            // Workaround of RT-29871: Instead of just invoking fixPosition()
            // here need to use listener that fixes dialog position once both
            // width and height are determined
            stage.widthProperty().addListener(positionAdjuster);
            stage.heightProperty().addListener(positionAdjuster);
            positionAdjuster.invalidated(null);
        }
        if (stage.getScene() == null) {
            stage.setScene(customScene);
        }
        updateValues();
        stage.show();
    }
   
    private InvalidationListener positionAdjuster = new InvalidationListener() {
        @Override
        public void invalidated(Observable ignored) {
            if (Double.isNaN(stage.getWidth()) || Double.isNaN(stage.getHeight())) {
                return;
            }
            stage.widthProperty().removeListener(positionAdjuster);
            stage.heightProperty().removeListener(positionAdjuster);
            fixPosition();
        }

    };

    private void fixPosition() {
        Window w = stage.getOwner();
        Screen s = com.sun.javafx.util.Utils.getScreen(w);
        Rectangle2D sb = s.getBounds();
        double xR = w.getX() + w.getWidth();
        double xL = w.getX() - stage.getWidth();
        double x, y;
        if (sb.getMaxX() >= xR + stage.getWidth()) {
            x = xR;
        } else if (sb.getMinX() <= xL) {
            x = xL;
        } else {
            x = Math.max(sb.getMinX(), sb.getMaxX() - stage.getWidth());
        }
        y = Math.max(sb.getMinY(), Math.min(sb.getMaxY() - stage.getHeight(), w.getY()));
        stage.setX(x);
        stage.setY(y);
    }

    @Override
    public void layoutChildren() {
        super.layoutChildren(); 

        circleHandle.setManaged(false);
        circleHandle.autosize();
    }

    public ObjectProperty<Paint> customColorProperty() {
        return customColorProperty;
    }

    public ObjectProperty<Paint> currentColorProperty() {
        return currentColorProperty;
    }

    private Color getCustomColor() {
        return (Color) customColorProperty.get();
    }

    private void setCustomColor(Color color) {
        customColorProperty.set(color);
    }

    public Color getCurrentColor() {
        return (Color) currentColorProperty.get();
    }

    void setCurrentColor(Color color) {
        currentColorProperty.set(color);
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
        customColorProperty.set(newColor);
          updateSlidersTrackColors();
        updateSelectedGradientStop(newColor);
    }

    private void updateHSBColor() {
    Color newColor = Color.hsb(
            hueProperty.get(), 
            clamp(satProperty.get()    / 100),
            clamp(brightProperty.get() / 100), 
            clamp(alphaProperty.get()  / 100));
            redProperty.set(doubleToInt(newColor.getRed()));
            greenProperty.set(doubleToInt(newColor.getGreen()));
            blueProperty.set(doubleToInt(newColor.getBlue()));
            alphaProperty.set(newColor.getOpacity() * 100);
            customColorProperty.set(newColor);
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
     * Update hueProperty, satProperty, brightProperty, color, redProperty, greenProperty and blueProperty
     * automatically every time the values change.
     */
    private void updateSelectedGradientStop(Color newColor) {
        if(mode.get().equals(Mode.GRADIENT)) {
            GradientPickerStop gradientPickerStop = gradientPicker.getSelectedStop();
            if(gradientPickerStop != null) {
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
                hueProperty.set(getCustomColor().getHue());
                satProperty.set(getCustomColor().getSaturation() * 100);
                brightProperty.set(getCustomColor().getBrightness() * 100);
                redProperty.set(doubleToInt(getCustomColor().getRed()));
                greenProperty.set(doubleToInt(getCustomColor().getGreen()));
                blueProperty.set(doubleToInt(getCustomColor().getBlue()));
                alphaProperty.set(getCustomColor().getOpacity() * 100);
                updateSlidersTrackColors();
                changeIsLocal = false;
            }
        }

    /**
     * Updates hueProperty, satProperty, brightProperty, color, redProperty, 
     * greenProperty and blueProperty and slider background colors
     * whenever the show() method for this dialog gets called.
     */
    private void updateValues() {
        if (getCurrentColor() == null) {
            setCurrentColor(Color.TRANSPARENT);
        }
        changeIsLocal = true;
        //Initialize hue, sat, bright, color, red, green and blue
        hueProperty.set(getCurrentColor().getHue());
        satProperty.set(getCurrentColor().getSaturation() * 100);
        brightProperty.set(getCurrentColor().getBrightness() * 100);
        alphaProperty.set(getCurrentColor().getOpacity() * 100);
        setCustomColor(Color.hsb(hueProperty.get(), clamp(satProperty.get() / 100), clamp(brightProperty.get() / 100),
                clamp(alphaProperty.get() / 100)));
        redProperty.set(doubleToInt(getCustomColor().getRed()));
        greenProperty.set(doubleToInt(getCustomColor().getGreen()));
        blueProperty.set(doubleToInt(getCustomColor().getBlue()));
        updateSlidersTrackColors();
        changeIsLocal = false;
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


    private int doubleToInt(double value) {
        return (int) (value * 255 + 0.5); // Adding 0.5 for rounding only
    }
}
