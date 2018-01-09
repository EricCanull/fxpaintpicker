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

import com.paintpicker.scene.control.picker.mode.Mode;
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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.PopupControl;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Screen;
import javafx.stage.Window;

/**
 * FXML Controller class
 *
 * @author Eric Canull
 */
public class CustomPaintDialog {
    
    Runnable onSave;
    private Runnable onSelect;
    private Runnable onCancel;
    
    private final CustomPaintControl customPaintControl;

    private final Scene customScene;
    private final Stage stage = new Stage();
    
    final ObjectProperty<Paint> customColorProperty = new SimpleObjectProperty<>(Color.BLACK);
    final ObjectProperty<Paint> currentColorProperty = new SimpleObjectProperty<>(Color.WHITE);
    final ObjectProperty<Paint> customPaintProperty = new SimpleObjectProperty<>(Color.BLACK);
    final ObjectProperty<Paint> currentPaintProperty = new SimpleObjectProperty<>(Color.WHITE);

    public CustomPaintDialog(PopupControl owner, PaintPicker paintPicker) {
        if (owner != null) {
            stage.initOwner(owner);
        }
     
        customPaintControl = new CustomPaintControl(CustomPaintDialog.this);

        stage.setTitle("Custom Paints");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(true);
        stage.addEventHandler(KeyEvent.ANY, keyEventListener);

        customScene = new Scene(customPaintControl);

        stage.setScene(customScene);
        
        if (paintPicker.getMode().equals(Mode.GRADIENT)) {
            customPaintControl.initializeGradientDialog();
        }

        stage.setOnCloseRequest((WindowEvent e) -> {
            if (onSave != null) {
                onSave.run();
            }
        });
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
        customPaintControl.updateValues();
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
    
    public ReadOnlyBooleanProperty showingProperty() {
        return stage.showingProperty();
    }
    
    public boolean isShowing() {
        return stage.isShowing();
    }

    public ObjectProperty<Paint> customColorProperty() {
        return customColorProperty;
    }

    ObjectProperty<Paint> currentColorProperty() {
        return currentColorProperty;
    }
    
    public ObjectProperty<Paint> customPaintProperty() {
        return customColorProperty;
    }

    ObjectProperty<Paint> currentPaintProperty() {
        return currentColorProperty;
    }

    Color getCustomColor() {
        return (Color) customColorProperty.get();
    }

    void setCustomColor(Color color) {
        customColorProperty.set(color);
    }

    public Color getCurrentColor() {
        return (Color) currentColorProperty.get();
    }

    public void setCurrentColor(Color color) {
        currentColorProperty.set(color);
    }

    private final EventHandler<KeyEvent> keyEventListener = e -> {
        if (e.getCode().equals(KeyCode.ESCAPE)) {
            stage.close();
        }
    };

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

    /**
     *
     * @return
     */
    public Runnable getOnSave() {
        return onSave;
    }

    /**
     *
     * @return
     */
    public Runnable getOnCancel() {
        return onCancel;
    }

    /**
     *
     * @param onCancel
     */
    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    /**
     *
     * @param onSave
     */
    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }

    /**
     *
     * @return
     */
    public Runnable getOnUse() {
        return onSelect;
    }

    /**
     *
     * @param onUse
     */
    public void setOnUse(Runnable onUse) {
        this.onSelect = onUse;
    }
}
