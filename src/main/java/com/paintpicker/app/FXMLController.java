package com.paintpicker.app;

import com.paintpicker.scene.control.picker.PaintPicker;
import java.net.URL;
import java.util.ResourceBundle;

import com.paintpicker.scene.control.picker.comboboxmode.Mode;
import javafx.beans.binding.ObjectBinding;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class FXMLController implements Initializable {
    
       
    @FXML StackPane rootPane;
    @FXML HBox menuBar;
    private PaintPicker paintPicker; 

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        paintPicker = new PaintPicker(Color.web("#1A4C9C"), Mode.GRADIENT);

        menuBar.getChildren().add(paintPicker);

        rootPane.backgroundProperty().bind(new ObjectBinding<Background>() {
            {
                bind(paintPicker.valueProperty());
            }

            @Override
            protected Background computeValue() {
                return new Background(new BackgroundFill(paintPicker.getValue(),
                        CornerRadii.EMPTY, Insets.EMPTY));
            }
        });
    }
}
