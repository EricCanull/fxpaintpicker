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
import static com.paintpicker.scene.control.picker.PaintPickerSkin.getString;
import com.paintpicker.scene.control.picker.mode.Mode;
import com.sun.javafx.scene.traversal.Algorithm;
import com.sun.javafx.scene.traversal.Direction;
import com.sun.javafx.scene.traversal.ParentTraversalEngine;
import com.sun.javafx.scene.traversal.TraversalContext;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

import java.util.List;
import sun.util.calendar.CalendarUtils;

public class PaintPalette extends Region {

    private static final int SQUARE_SIZE = 15;

    // package protected for testing purposes
    ColorPickerGrid colorPickerGrid;
    final Hyperlink customColorLink = new Hyperlink(getString("customColorLink"));
    private PaintPicker paintPicker;
    private CustomPaintControl customPaintControl = null;
     
    private final GridPane customColorGrid = new GridPane();
    private final Label customColorLabel = new Label(getString("customColorLabel"));

    private PopupControl popupControl;
    private ColorSquare focusedSquare;
    private ContextMenu contextMenu = null;

    private Color mouseDragColor = null;
    private boolean dragDetected = false;

    // Metrics for custom colors
    private int customColorNumber = 0;
    private int customColorRows = 0;
    private int customColorLastRowLength = 0;

    private final ColorSquare hoverSquare = new ColorSquare();

    public PaintPalette(final PaintPicker paintPicker) {
        getStyleClass().add("color-palette-region");
        getStylesheets().add(PaintPalette.class.getResource("/styles/paint-pallette.css").toExternalForm());

        this.paintPicker = paintPicker;
        colorPickerGrid = new ColorPickerGrid();
        customColorLabel.getStyleClass().add("custom-color-label");
        colorPickerGrid.getChildren().get(0).requestFocus();
        customColorLabel.setAlignment(Pos.CENTER_LEFT);

        customColorLink.getStyleClass().add("custom-color-link");
        customColorLink.setPrefWidth(colorPickerGrid.prefWidth(-1));
        customColorLink.setAlignment(Pos.CENTER);
        customColorLink.setFocusTraversable(true);
        customColorLink.setVisited(true); // so that it always appears blue
        customColorLink.setOnAction((ActionEvent t) -> {
            if (customPaintControl == null) {
                customPaintControl = new CustomPaintControl(popupControl, paintPicker.getMode());
                customPaintControl.customColorProperty().addListener((observable) -> {
                    if(!customPaintControl.isShowing())
                    paintPicker.setValue(customPaintControl.customColorProperty().get());
                        });

                customPaintControl.customPaintProperty().addListener((observable) -> {
                    if (customPaintControl.isGradientShowing())
                    paintPicker.setValue(customPaintControl.customPaintProperty().get());
                });

                customPaintControl.setOnSave(() -> {
                    Paint customColor = customPaintControl.customColorProperty().get();
                    buildCustomColors();
                    paintPicker.getCustomColors().add(customColor);
                    updateSelection(customColor);
                    Event.fireEvent(paintPicker, new ActionEvent());
                    paintPicker.hide();
                });
                customPaintControl.setOnUse(() -> {
                    Event.fireEvent(paintPicker, new ActionEvent());
                    paintPicker.hide();
                });
            }
            
            customPaintControl.currentColorProperty().set(paintPicker.valueProperty().get());

            if (popupControl != null) {
                popupControl.setAutoHide(false);
                paintPicker.hide();
                customPaintControl.show();
            }

            customPaintControl.setOnHidden(event -> {
                if (popupControl != null) {
                    popupControl.setAutoHide(true);
                }
            });
        });
        
        buildColorPalette();
    }
    
    private void buildColorPalette() {
        initNavigation();
        customColorGrid.getStyleClass().add("color-picker-grid");
        customColorGrid.setVisible(false);
        buildCustomColors();
        paintPicker.getCustomColors().addListener((ListChangeListener.Change<? extends Paint> change) -> {
            buildCustomColors();
        });
        
        Separator separator = new Separator();
        separator.getStyleClass().add("separator");

        VBox paletteBox = new VBox();
        paletteBox.getStyleClass().add("color-palette");
        paletteBox.getChildren().addAll(colorPickerGrid, customColorLabel, customColorGrid, separator, customColorLink);

        hoverSquare.setMouseTransparent(true);
        hoverSquare.getStyleClass().addAll("hover-square");
        setFocusedSquare(null);
        getChildren().addAll(paletteBox, hoverSquare);
    }

    private void setFocusedSquare(ColorSquare square) {
        if (square == focusedSquare) {
            return;
        }
        focusedSquare = square;

        hoverSquare.setVisible(focusedSquare != null);
        if (focusedSquare == null) {
            return;
        }

        if (!focusedSquare.isFocused()) {
            focusedSquare.requestFocus();
        }

        hoverSquare.rectangle.setFill(focusedSquare.rectangle.getFill());

        Bounds b = square.localToScene(square.getLayoutBounds());

        double x = b.getMinX();
        double y = b.getMinY();

        double xAdjust;
        double scaleAdjust = hoverSquare.getScaleX() == 1.0 ? 0 : hoverSquare.getWidth() / 4.0;

        if (paintPicker.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT) {
            x = focusedSquare.getLayoutX();
            xAdjust = -focusedSquare.getWidth() + scaleAdjust;
        } else {
            xAdjust = focusedSquare.getWidth() / 2.0 + scaleAdjust;
        }

        hoverSquare.setLayoutX(snapPosition(x) - xAdjust);
        hoverSquare.setLayoutY(snapPosition(y) - focusedSquare.getHeight() / 2.0 + (hoverSquare.getScaleY() == 1.0 ? 0 : focusedSquare.getHeight() / 4.0));
    }

    private void buildCustomColors() {
        final ObservableList<Paint> customColors = paintPicker.getCustomColors();
        customColorNumber = customColors.size();

        customColorGrid.getChildren().clear();
        if (customColors.isEmpty()) {
            customColorLabel.setVisible(false);
            customColorLabel.setManaged(false);
            customColorGrid.setVisible(false);
            customColorGrid.setManaged(false);
            return;
        } else {
            customColorLabel.setVisible(true);
            customColorLabel.setManaged(true);
            customColorGrid.setVisible(true);
            customColorGrid.setManaged(true);
            if (contextMenu == null) {
                MenuItem item = new MenuItem(getString("removeColor"));
                item.setOnAction((ActionEvent e) -> {
                    ColorSquare square = (ColorSquare)contextMenu.getOwnerNode();
                    customColors.remove(square.rectangle.getFill());
                    buildCustomColors();
                });
                contextMenu = new ContextMenu(item);
            }
        }

        int customColumnIndex = 0;
        int customRowIndex = 0;
        int remainingSquares = customColors.size() % NUM_OF_COLUMNS;
        int numEmpty = (remainingSquares == 0) ? 0 : NUM_OF_COLUMNS - remainingSquares;
        customColorLastRowLength = remainingSquares == 0 ? 12 : remainingSquares;

        for (int i = 0; i < customColors.size(); i++) {
            Paint c = customColors.get(i);
            ColorSquare square = new ColorSquare(c, i, true);
            square.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent e) -> {
                if (e.getCode() == KeyCode.DELETE) {
                    customColors.remove(square.rectangle.getFill());
                    buildCustomColors();
                }
            });
            customColorGrid.add(square, customColumnIndex, customRowIndex);
            customColumnIndex++;
            if (customColumnIndex == NUM_OF_COLUMNS) {
                customColumnIndex = 0;
                customRowIndex++;
            }
        }
        for (int i = 0; i < numEmpty; i++) {
            ColorSquare emptySquare = new ColorSquare();
            customColorGrid.add(emptySquare, customColumnIndex, customRowIndex);
            customColumnIndex++;
        }
        customColorRows = customRowIndex + 1;
        requestLayout();

    }

    private void initNavigation() {
        setOnKeyPressed(ke -> {
            switch (ke.getCode()) {
                case SPACE:
                case ENTER:
                    // select the focused color
                    if (focusedSquare != null) {
                        focusedSquare.selectColor(ke);
                    }
                    ke.consume();
                    break;
                default: // no-op
            }
        });
        
        setImpl_traversalEngine(new ParentTraversalEngine(this, new Algorithm() {
            @Override
            public Node select(Node owner, Direction dir, TraversalContext context) {
                final Node subsequentNode = context.selectInSubtree(context.getRoot(), owner, dir);
                switch (dir) {
                    case NEXT:
                    case NEXT_IN_LINE:
                    case PREVIOUS:
                        return subsequentNode;
                    // Here, we need to intercept the standard algorithm in a few cases to get the desired traversal
                    // For right or left direction we want to continue on the next or previous row respectively
                    // For up and down, the custom color panel might be skipped by the standard algorithm (if not wide enough
                    // to be between the current color and custom color button), so we need to include it in the path explicitly.
                    case LEFT:
                    case RIGHT:
                    case UP:
                    case DOWN:
                        if (owner instanceof ColorSquare) {
                            Node result =  processArrow((ColorSquare)owner, dir);
                            return result != null ? result : subsequentNode;
                        } else {
                            return subsequentNode;
                        }
                }
                return null;
            }

            private Node processArrow(ColorSquare owner, Direction dir) {
                final int row = owner.index / NUM_OF_COLUMNS;
                final int column = owner.index % NUM_OF_COLUMNS;

                // Adjust the direction according to color picker orientation
                dir = dir.getDirectionForNodeOrientation(paintPicker.getEffectiveNodeOrientation());
                // This returns true for all the cases which we need to override
                if (isAtBorder(dir, row, column, owner.isCustom)) {
                    // There's no other node in the direction from the square, so we need to continue on some other row
                    // or cycle
                    int subsequentRow = row;
                    int subsequentColumn = column;
                    boolean subSequentSquareCustom = owner.isCustom;
                    switch (dir) {
                        case LEFT:
                        case RIGHT:
                            // The next row is either the first or the last, except when cycling in custom colors, the last row
                            // might have different number of columns
                            if (owner.isCustom) {
                                subsequentRow = Math.floorMod(dir == Direction.LEFT ? row - 1 : row + 1, customColorRows);
                                subsequentColumn = dir == Direction.LEFT ? subsequentRow == customColorRows - 1 ?
                                        customColorLastRowLength - 1 : NUM_OF_COLUMNS - 1 : 0;
                            } else {
                                subsequentRow = Math.floorMod(dir == Direction.LEFT ? row - 1 : row + 1, NUM_OF_ROWS);
                                subsequentColumn = dir == Direction.LEFT ? NUM_OF_COLUMNS - 1 : 0;
                            }
                            break;
                        case UP: // custom color are not handled here
                            subsequentRow = NUM_OF_ROWS - 1;
                            break;
                        case DOWN: // custom color are not handled here
                            if (customColorNumber > 0) {
                                subSequentSquareCustom = true;
                                subsequentRow = 0;
                                subsequentColumn = customColorRows > 1 ? column : Math.min(customColorLastRowLength - 1, column);
                                break;
                            } else {
                                return null; // Let the default algorith handle this
                            }

                    }
                    if (subSequentSquareCustom) {
                        return customColorGrid.getChildren().get(subsequentRow * NUM_OF_COLUMNS + subsequentColumn);
                    } else {
                        return colorPickerGrid.getChildren().get(subsequentRow * NUM_OF_COLUMNS + subsequentColumn);
                    }
                }
                return null;
            }

            private boolean isAtBorder(Direction dir, int row, int column, boolean custom) {
                switch (dir) {
                    case LEFT:
                        return column == 0;
                    case RIGHT:
                        return custom && row == customColorRows - 1 ?
                                column == customColorLastRowLength - 1 : column == NUM_OF_COLUMNS - 1;
                    case UP:
                        return !custom && row == 0;
                    case DOWN:
                        return !custom && row == NUM_OF_ROWS - 1;
                }
                return false;
            }

            @Override
            public Node selectFirst(TraversalContext context) {
                return colorPickerGrid.getChildren().get(0);
            }

            @Override
            public Node selectLast(TraversalContext context) {
                return customColorLink;
            }
        }));
    }

    private void processSelectKey(KeyEvent ke) {
        if (focusedSquare != null) focusedSquare.selectColor(ke);
    }

    public void setPopupControl(PopupControl pc) {
        this.popupControl = pc;
    }

    private ColorPickerGrid getColorGrid() {
        return colorPickerGrid;
    }

    public boolean isCustomColorDialogShowing() {
        if (customPaintControl != null) return customPaintControl.isShowing();
        return false;
    }

    class ColorSquare extends StackPane {
        Rectangle rectangle;
        int index;
        boolean isEmpty;
        boolean isCustom;

        public ColorSquare() {
            this(null, -1, false);
        }

        public ColorSquare(Paint color, int index) {
            this(color, index, false);
        }

        public ColorSquare(Paint color, int index, boolean isCustom) {
            // Add style class to handle selected color square
            getStyleClass().add("color-square");
            if (color != null) {
                setFocusTraversable(true);

                focusedProperty().addListener((s, ov, nv) -> {
                    setFocusedSquare(nv ? this : null);
                });

                addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
                    setFocusedSquare(ColorSquare.this);
                });
                addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
                    setFocusedSquare(null);
                });

                addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
                    if (!dragDetected && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                        if (!isEmpty) {
                            Color fill = (Color) rectangle.getFill();
                            paintPicker.setValue(fill);
                            paintPicker.fireEvent(new ActionEvent());
                            updateSelection(fill);
                            event.consume();
                        }
                        paintPicker.hide();
                    } else if (event.getButton() == MouseButton.SECONDARY ||
                            event.getButton() == MouseButton.MIDDLE) {
                        if (isCustom && contextMenu != null) {
                            if (!contextMenu.isShowing()) {
                                contextMenu.show(ColorSquare.this, Side.RIGHT, 0, 0);
                           //     Utils.addMnemonics(contextMenu, ColorSquare.this.getScene(), paintPicker.impl_isShowMnemonics());
                            } else {
                                contextMenu.hide();
                               // Utils.removeMnemonics(contextMenu, ColorSquare.this.getScene());
                            }
                        }
                    }
                });
            }
            this.index = index;
            this.isCustom = isCustom;
            rectangle = new Rectangle(SQUARE_SIZE, SQUARE_SIZE);
            if (color == null) {
                rectangle.setFill(Color.WHITE);
                isEmpty = true;
            } else {
                rectangle.setFill(color);
            }

            rectangle.setStrokeType(StrokeType.INSIDE);

            String tooltipStr = PaintPickerSkin.tooltipString(color);
            Tooltip.install(this, new Tooltip((tooltipStr == null) ? "" : tooltipStr));

            rectangle.getStyleClass().add("color-rect");

            getChildren().add(rectangle);
        }

        public void selectColor(KeyEvent event) {
            if (rectangle.getFill() != null) {
                if (rectangle.getFill() instanceof Color) {
                    paintPicker.setValue(rectangle.getFill());
                    paintPicker.fireEvent(new ActionEvent());
                }
                event.consume();
            }
            paintPicker.hide();
        }
    }

    // The skin can update selection if colorpicker value changes..
    public void updateSelection(Paint color) {
        setFocusedSquare(null);

        for (ColorSquare c : colorPickerGrid.getSquares()) {
            if (c.rectangle.getFill().equals(color)) {
                setFocusedSquare(c);
                return;
            }
        }
        // check custom colors
        for (Node n : customColorGrid.getChildren()) {
            ColorSquare c = (ColorSquare) n;
            if (c.rectangle.getFill().equals(color)) {
                setFocusedSquare(c);
                return;
            }
        }
    }

    class ColorPickerGrid extends GridPane {

        private final List<ColorSquare> squares;

        public ColorPickerGrid() {
            getStyleClass().add("color-picker-grid");
            setId("ColorCustomizerColorGrid");
            int columnIndex = 0, rowIndex = 0;
            squares = FXCollections.observableArrayList();
            final int numColors = RAW_VALUES.length / 3;
            Color[] colors = new Color[numColors];
            for (int i = 0; i < numColors; i++) {
                colors[i] = new Color(RAW_VALUES[(i * 3)] / 255,
                        RAW_VALUES[(i * 3) + 1] / 255, RAW_VALUES[(i * 3) + 2] / 255,
                        1.0);
                ColorSquare cs = new ColorSquare(colors[i], i);
                squares.add(cs);
            }

            for (ColorSquare square : squares) {
                add(square, columnIndex, rowIndex);
                columnIndex++;
                if (columnIndex == NUM_OF_COLUMNS) {
                    columnIndex = 0;
                    rowIndex++;
                }
            }
            setOnMouseDragged(t -> {
                if (!dragDetected) {
                    dragDetected = true;
                    mouseDragColor = (Color) paintPicker.getValue();
                }
                int xIndex = clamp(0,
                        (int)t.getX()/(SQUARE_SIZE + 1), NUM_OF_COLUMNS - 1);
                int yIndex = clamp(0,
                        (int)t.getY()/(SQUARE_SIZE + 1), NUM_OF_ROWS - 1);
                int index = xIndex + yIndex*NUM_OF_COLUMNS;
                paintPicker.setValue(squares.get(index).rectangle.getFill());
                updateSelection(paintPicker.getValue());
            });
            addEventHandler(MouseEvent.MOUSE_RELEASED, t -> {
                if(colorPickerGrid.getBoundsInLocal().contains(t.getX(), t.getY())) {
                    updateSelection(paintPicker.getValue());
                    paintPicker.fireEvent(new ActionEvent());
                    paintPicker.hide();
                } else {
                    // restore color as mouse release happened outside the grid.
                    if (mouseDragColor != null) {
                        paintPicker.setValue(mouseDragColor);
                        updateSelection(mouseDragColor);
                    }
                }
                dragDetected = false;
            });
        }

        public List<ColorSquare> getSquares() {
            return squares;
        }

        @Override protected double computePrefWidth(double height) {
            return (SQUARE_SIZE + 1)*NUM_OF_COLUMNS;
        }

        @Override protected double computePrefHeight(double width) {
            return (SQUARE_SIZE + 1)*NUM_OF_ROWS;
        }
    }

    private static final int NUM_OF_COLUMNS = 12;
    private static double[] RAW_VALUES = {
            // WARNING: always make sure the number of colors is a divisable by NUM_OF_COLUMNS
            255, 255, 255, // first row
            242, 242, 242,
            230, 230, 230,
            204, 204, 204,
            179, 179, 179,
            153, 153, 153,
            128, 128, 128,
            102, 102, 102,
            77, 77, 77,
            51, 51, 51,
            26, 26, 26,
            0, 0, 0,
            0, 51, 51, // second row
            0, 26, 128,
            26, 0, 104,
            51, 0, 51,
            77, 0, 26,
            153, 0, 0,
            153, 51, 0,
            153, 77, 0,
            153, 102, 0,
            153, 153, 0,
            102, 102, 0,
            0, 51, 0,
            26, 77, 77, // third row
            26, 51, 153,
            51, 26, 128,
            77, 26, 77,
            102, 26, 51,
            179, 26, 26,
            179, 77, 26,
            179, 102, 26,
            179, 128, 26,
            179, 179, 26,
            128, 128, 26,
            26, 77, 26,
            51, 102, 102, // fourth row
            51, 77, 179,
            77, 51, 153,
            102, 51, 102,
            128, 51, 77,
            204, 51, 51,
            204, 102, 51,
            204, 128, 51,
            204, 153, 51,
            204, 204, 51,
            153, 153, 51,
            51, 102, 51,
            77, 128, 128, // fifth row
            77, 102, 204,
            102, 77, 179,
            128, 77, 128,
            153, 77, 102,
            230, 77, 77,
            230, 128, 77,
            230, 153, 77,
            230, 179, 77,
            230, 230, 77,
            179, 179, 77,
            77, 128, 77,
            102, 153, 153, // sixth row
            102, 128, 230,
            128, 102, 204,
            153, 102, 153,
            179, 102, 128,
            255, 102, 102,
            255, 153, 102,
            255, 179, 102,
            255, 204, 102,
            255, 255, 77,
            204, 204, 102,
            102, 153, 102,
            128, 179, 179, // seventh row
            128, 153, 255,
            153, 128, 230,
            179, 128, 179,
            204, 128, 153,
            255, 128, 128,
            255, 153, 128,
            255, 204, 128,
            255, 230, 102,
            255, 255, 102,
            230, 230, 128,
            128, 179, 128,
            153, 204, 204, // eigth row
            153, 179, 255,
            179, 153, 255,
            204, 153, 204,
            230, 153, 179,
            255, 153, 153,
            255, 179, 128,
            255, 204, 153,
            255, 230, 128,
            255, 255, 128,
            230, 230, 153,
            153, 204, 153,
            179, 230, 230, // ninth row
            179, 204, 255,
            204, 179, 255,
            230, 179, 230,
            230, 179, 204,
            255, 179, 179,
            255, 179, 153,
            255, 230, 179,
            255, 230, 153,
            255, 255, 153,
            230, 230, 179,
            179, 230, 179,
            204, 255, 255, // tenth row
            204, 230, 255,
            230, 204, 255,
            255, 204, 255,
            255, 204, 230,
            255, 204, 204,
            255, 204, 179,
            255, 230, 204,
            255, 255, 179,
            255, 255, 204,
            230, 230, 204,
            204, 255, 204
    };

    private static final int NUM_OF_COLORS = RAW_VALUES.length / 3;
    private static final int NUM_OF_ROWS = NUM_OF_COLORS / NUM_OF_COLUMNS;

    private static int clamp(int min, int value, int max) {
        return value < min ? min : value > max ? max : value;
    }
}
