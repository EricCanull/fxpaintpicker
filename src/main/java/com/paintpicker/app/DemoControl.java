package com.paintpicker.app;

import com.paintpicker.scene.control.picker.PaintPicker;
import java.net.URL;
import java.util.ResourceBundle;

import com.paintpicker.scene.control.picker.mode.Mode;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * 
 * @author andje22
 */
public class DemoControl implements Initializable {
    
       
    @FXML StackPane rootPane;
    @FXML HBox menuBar;
    private PaintPicker paintPicker; 

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        paintPicker = new PaintPicker(Color.web("#1A4C9C"), Mode.GRADIENT);

        menuBar.getChildren().add(paintPicker);
        
        rootPane.backgroundProperty().bind(Bindings.createObjectBinding(()->
                new Background(new BackgroundFill(paintPicker.getValue(),
                        CornerRadii.EMPTY, Insets.EMPTY)), paintPicker.valueProperty()));
    }
}
