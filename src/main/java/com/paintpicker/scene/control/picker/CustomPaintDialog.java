/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.paintpicker.scene.control.picker;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Screen;
import javafx.stage.Window;

/**
 * FXML Controller class
 *
 * @author Eric Canull
 */
public class CustomPaintDialog {

    private final Scene customScene;
    private final Stage stage = new Stage();

    public CustomPaintDialog(Window owner, CustomPaintControl node) {
        if (owner != null) {
            stage.initOwner(owner);
        }

        stage.setTitle("Custom Paints");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(true);
        stage.addEventHandler(KeyEvent.ANY, keyEventListener);

        customScene = new Scene((Parent) node);

        stage.setScene(customScene);

        stage.setOnCloseRequest((WindowEvent e) -> {
            if (node.getOnSave() != null) {
                node.getOnSave().run();
            }
        });
    }

    private final EventHandler<KeyEvent> keyEventListener = e -> {
        if (e.getCode().equals(KeyCode.ESCAPE)) {
            stage.close();
        }
    };

    // JDK-8161449
    public void setShowUseBtn(boolean showUseBtn) {
//     this.showSelectBtn = showUseBtn;
        stage.show();
    }

    public void setOnHidden(EventHandler<WindowEvent> onHidden) {
        stage.setOnHidden(onHidden);
    }

    public Stage getDialog() {
        return stage;
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
}
