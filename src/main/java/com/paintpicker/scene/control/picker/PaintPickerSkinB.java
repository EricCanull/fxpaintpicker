package com.paintpicker.scene.control.picker;

import com.sun.javafx.scene.control.behavior.ComboBoxBaseBehavior;
import com.sun.javafx.scene.control.skin.ComboBoxPopupControl;

import javafx.scene.Node;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.TextField;
import javafx.scene.paint.Paint;
import javafx.util.StringConverter;

@SuppressWarnings("restriction")
public class PaintPickerSkinB extends ComboBoxPopupControl<Paint> {

	public PaintPickerSkinB(ComboBoxBase<Paint> comboBoxBase, ComboBoxBaseBehavior<Paint> behavior) {
		super(comboBoxBase, behavior);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Node getPopupContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected TextField getEditor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected StringConverter<Paint> getConverter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getDisplayNode() {
		// TODO Auto-generated method stub
		return null;
	} 

}
