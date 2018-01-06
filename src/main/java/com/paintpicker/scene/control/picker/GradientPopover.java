package com.paintpicker.scene.control.picker;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 */
public class GradientPopover {

    private final Scene customScene;
    private final Stage stage = new Stage();

    public GradientPopover(Window owner, Node node) {

        if (owner != null) {
            stage.initOwner(owner);
        }

        stage.initModality(Modality.NONE);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(true);
        stage.addEventHandler(KeyEvent.ANY, keyEventListener);
        stage.getOwner().xProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                stage.setX(newValue.doubleValue());
            }
        });
        stage.getOwner().yProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                stage.setY(newValue.doubleValue() + stage.getOwner().getHeight());
            }
        });

        customScene = new Scene((Parent) node);
        stage.setScene(customScene);
    }

    public void hide() {
        stage.hide();
    }

    public boolean isShowing() {
        return stage.isShowing();
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
        Window window = stage.getOwner();
        double x = window.getX();
        double y = window.getHeight() + window.getY();
        stage.setX(x);
        stage.setY(y);
    }

    private final EventHandler<KeyEvent> keyEventListener = e -> {
        if (e.getCode().equals(KeyCode.ESCAPE)) {
            stage.close();
        }
    };
}
