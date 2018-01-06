/*
 */
package com.paintpicker.scene.control.picker;

import com.paintpicker.scene.control.picker.comboboxmode.Mode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 */
public class PaintPicker extends ComboBoxBase<Paint> {

    /**
     * The custom colors added to the Color Palette by the user.
     */
    private final ObservableList<Paint> customColors = FXCollections.observableArrayList();

    /**
     * Gets the list of custom colors added to the Color Palette by the user.
     *
     * @return
     */
    public final ObservableList<Paint> getCustomColors() {
        return customColors;
    }

     private final Mode mode;

    /**
     * Creates a default ColorPicker instance with a selected color set to white.
     */
    public PaintPicker() {
        this(Color.WHITE, Mode.SINGLE);
    }

    /**
     * Creates a ColorPicker instance and sets the selected paint to the given paint.
     *
     * @param paint to be set as the currently selected paint of the ColorPicker.
     * @param mode
     */
    public PaintPicker(Paint paint, Mode mode) {
        setValue(paint);
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        this.mode = mode;
    }

    /***************************************************************************
     *                                                                         *
     * Methods                                                                 *
     *                                                                         *
     * @return 
     **************************************************************************/
    public Mode getMode() {
        return mode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new PaintPickerSkin(this);
    }

    /***************************************************************************
     *                                                                         *
     * Stylesheet Handling                                                     *
     *                                                                         *
     * @return
     **************************************************************************/
    private static final String DEFAULT_STYLE_CLASS = "color-picker";

    /**
     * The style class to specify a Button like appearance of ColorPicker control.
     */
    public static final String STYLE_CLASS_BUTTON = "button";

    /**
     * The style class to specify a SplitMenuButton like appearance of ColorPicker control.
     */
    public static final String STYLE_CLASS_SPLIT_BUTTON = "split-button";

    /**
     * The style class to specify a SplitMenuButton like appearance of ColorPicker control.
     */
    public static final String SINGLE_MODE = "default-mode";

    /**
     * The style class to specify a SplitMenuButton like appearance of ColorPicker control.
     */
    public static final String GRADIENT_MODE = "gradient-mode";


}

