/*
 * JFoenix
 * Copyright (c) 2015, JFoenix and/or its affiliates., All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */

package com.jfoenix.controls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.SimpleStyleableObjectProperty;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import com.jfoenix.controls.events.JFXDialogEvent;
import com.jfoenix.converters.DialogTransitionConverter;
import com.jfoenix.effects.JFXDepthManager;
import com.jfoenix.transitions.CachedTransition;

/**
 * @author sshahine
 *
 */
@DefaultProperty(value="content")
public class JFXDialog extends StackPane {

	//	public static enum JFXDialogLayout{PLAIN, HEADING, ACTIONS, BACKDROP};
	public static enum DialogTransition{CENTER, TOP, RIGHT, BOTTOM, LEFT};

	private StackPane contentHolder;
	private StackPane overlayPane;

	private double offsetX = 0;
	private double offsetY = 0;

	private Pane dialogContainer;
	private Region content;
	private Transition animation;

	private BooleanProperty overlayClose = new SimpleBooleanProperty(true);
	EventHandler<? super MouseEvent> closeHandler = (e)->close();
	
	public JFXDialog(){
		this(null,null,DialogTransition.CENTER);
	}

	/**
	 * creates JFX Dialog control
	 * @param dialogContainer
	 * @param content
	 * @param transitionType
	 */
	
	public JFXDialog(Pane dialogContainer, Region content, DialogTransition transitionType) {		
		initialize();
		setContent(content);
		setDialogContainer(dialogContainer);
		this.transitionType.set(transitionType);
		// init change listeners
		initChangeListeners();
	}
	
	/**
	 * creates JFX Dialog control
	 * @param dialogContainer
	 * @param content
	 * @param transitionType
	 * @param overlayClose
	 */
	public JFXDialog(Pane dialogContainer, Region content, DialogTransition transitionType, boolean overlayClose) {		
		initialize();
		setOverlayClose(overlayClose);
		setContent(content);
		setDialogContainer(dialogContainer);
		this.transitionType.set(transitionType);
		// init change listeners
		initChangeListeners();
	}

	private void initChangeListeners(){
		overlayCloseProperty().addListener((o,oldVal,newVal)->{
			if(overlayPane!=null){
				if(newVal) overlayPane.addEventHandler(MouseEvent.MOUSE_PRESSED, closeHandler);
				else overlayPane.removeEventHandler(MouseEvent.MOUSE_PRESSED, closeHandler);		
			}
		});
	}
	
	private void initialize() {
		this.setVisible(false);
		this.getStyleClass().add(DEFAULT_STYLE_CLASS);        
	}

	/***************************************************************************
	 *                                                                         *
	 * Setters / Getters                                                       *
	 *                                                                         *
	 **************************************************************************/

	public Pane getDialogContainer() {
		return dialogContainer;
	}

	public void setDialogContainer(Pane dialogContainer) {
		if(dialogContainer!=null){
			this.dialogContainer = dialogContainer;
			this.getChildren().clear();
			this.getChildren().add(overlayPane);
			this.visibleProperty().unbind();
			this.visibleProperty().bind(overlayPane.visibleProperty());
			this.dialogContainer.getChildren().remove(this);
			this.dialogContainer.getChildren().add(this);
			// FIXME: need to be improved to consider only the parent boundary
			offsetX = (this.getParent().getBoundsInLocal().getWidth());
			offsetY = (this.getParent().getBoundsInLocal().getHeight());
			animation = getShowAnimation(transitionType.get());
		}
	}

	public Region getContent() {
		return content;
	}

	public void setContent(Region content) {
		if(content!=null){
			this.content = content;	
			contentHolder = new StackPane();
			contentHolder.getChildren().add(content);
			contentHolder.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(2), null)));
			JFXDepthManager.setDepth(contentHolder, 4);
			contentHolder.setPickOnBounds(false);
			// ensure stackpane is never resized beyond it's preferred size
			contentHolder.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
			overlayPane = new StackPane();
			overlayPane.getChildren().add(contentHolder);
			overlayPane.getStyleClass().add("jfx-dialog-overlay-pane");
			StackPane.setAlignment(contentHolder, Pos.CENTER);
			overlayPane.setVisible(false);
			overlayPane.setBackground(new Background(new BackgroundFill(Color.rgb(0, 0, 0, 0.1), null, null)));
			// close the dialog if clicked on the overlay pane
			if(overlayClose.get()) overlayPane.addEventHandler(MouseEvent.MOUSE_PRESSED, closeHandler);
			// prevent propagating the events to overlay pane
			contentHolder.addEventHandler(MouseEvent.ANY, (e)->e.consume());
		}
	}

	
	public final BooleanProperty overlayCloseProperty() {
		return this.overlayClose;
	}

	public final boolean isOverlayClose() {
		return this.overlayCloseProperty().get();
	}

	public final void setOverlayClose(final boolean overlayClose) {
		this.overlayCloseProperty().set(overlayClose);
	}
	
	/***************************************************************************
	 *                                                                         *
	 * Public API                                                              *
	 *                                                                         *
	 **************************************************************************/

	public void show(Pane dialogContainer){
		this.setDialogContainer(dialogContainer);
		animation.play();
	}

	public void show(){
		animation.play();		
	}

	public void close(){
		animation.setRate(-1);
		animation.play();
		animation.setOnFinished((e)->{
			resetProperties();
		});
		onDialogClosedProperty.get().handle(new JFXDialogEvent(JFXDialogEvent.CLOSED));
	}

	/***************************************************************************
	 *                                                                         *
	 * Transitions                                                             *
	 *                                                                         *
	 **************************************************************************/

	private Transition getShowAnimation(DialogTransition transitionType){
		Transition animation = null;
		if(contentHolder!=null){
			switch (transitionType) {		
			case LEFT:	
				contentHolder.setTranslateX(-offsetX);
				animation = new LeftTransition();
				break;
			case RIGHT:			
				contentHolder.setTranslateX(offsetX);
				animation = new RightTransition();
				break;
			case TOP:	
				contentHolder.setTranslateY(-offsetY);
				animation = new TopTransition();
				break;
			case BOTTOM:			
				contentHolder.setTranslateY(offsetY);
				animation = new BottomTransition();
				break;
			default:
				contentHolder.setScaleX(0);
				contentHolder.setScaleY(0);
				animation = new CenterTransition();
				break;
			}
		}
		if(animation!=null)animation.setOnFinished((finish)->onDialogOpenedProperty.get().handle(new JFXDialogEvent(JFXDialogEvent.OPENED)));
		return animation;
	}

	private void resetProperties(){
		overlayPane.setVisible(false);	
		contentHolder.setTranslateX(0);
		contentHolder.setTranslateY(0);
		contentHolder.setScaleX(1);
		contentHolder.setScaleY(1);
	}

	private class LeftTransition extends CachedTransition {
		public LeftTransition() {
			super(contentHolder, new Timeline(
					new KeyFrame(Duration.ZERO, 
							new KeyValue(contentHolder.translateXProperty(), -offsetX ,Interpolator.EASE_BOTH),
							new KeyValue(overlayPane.visibleProperty(), false ,Interpolator.EASE_BOTH)
							),
							new KeyFrame(Duration.millis(10), 
									new KeyValue(overlayPane.visibleProperty(), true ,Interpolator.EASE_BOTH),
									new KeyValue(overlayPane.opacityProperty(), 0,Interpolator.EASE_BOTH)
									),
									new KeyFrame(Duration.millis(1000), 
											new KeyValue(contentHolder.translateXProperty(), 0,Interpolator.EASE_BOTH),
											new KeyValue(overlayPane.opacityProperty(), 1,Interpolator.EASE_BOTH)
											))
					);
			// reduce the number to increase the shifting , increase number to reduce shifting
			setCycleDuration(Duration.seconds(0.4));
			setDelay(Duration.seconds(0));
		}
	}

	private class RightTransition extends CachedTransition {
		public RightTransition() {
			super(contentHolder, new Timeline(
					new KeyFrame(Duration.ZERO, 
							new KeyValue(contentHolder.translateXProperty(), offsetX ,Interpolator.EASE_BOTH),
							new KeyValue(overlayPane.visibleProperty(), false ,Interpolator.EASE_BOTH)
							),
							new KeyFrame(Duration.millis(10), 
									new KeyValue(overlayPane.visibleProperty(), true ,Interpolator.EASE_BOTH),
									new KeyValue(overlayPane.opacityProperty(), 0, Interpolator.EASE_BOTH)
									),
									new KeyFrame(Duration.millis(1000), 
											new KeyValue(contentHolder.translateXProperty(), 0,Interpolator.EASE_BOTH),
											new KeyValue(overlayPane.opacityProperty(), 1, Interpolator.EASE_BOTH)))
					);
			// reduce the number to increase the shifting , increase number to reduce shifting
			setCycleDuration(Duration.seconds(0.4));
			setDelay(Duration.seconds(0));
		}
	}

	private class TopTransition extends CachedTransition {
		public TopTransition() {
			super(contentHolder, new Timeline(
					new KeyFrame(Duration.ZERO, 
							new KeyValue(contentHolder.translateYProperty(), -offsetY ,Interpolator.EASE_BOTH),
							new KeyValue(overlayPane.visibleProperty(), false ,Interpolator.EASE_BOTH)
							),
							new KeyFrame(Duration.millis(10), 
									new KeyValue(overlayPane.visibleProperty(), true ,Interpolator.EASE_BOTH),
									new KeyValue(overlayPane.opacityProperty(), 0, Interpolator.EASE_BOTH)
									),
									new KeyFrame(Duration.millis(1000),
											new KeyValue(contentHolder.translateYProperty(), 0,Interpolator.EASE_BOTH),
											new KeyValue(overlayPane.opacityProperty(), 1, Interpolator.EASE_BOTH)))
					);
			// reduce the number to increase the shifting , increase number to reduce shifting
			setCycleDuration(Duration.seconds(0.4));
			setDelay(Duration.seconds(0));
		}
	}

	private class BottomTransition extends CachedTransition {
		public BottomTransition() {
			super(contentHolder, new Timeline(
					new KeyFrame(Duration.ZERO, 
							new KeyValue(contentHolder.translateYProperty(), offsetY ,Interpolator.EASE_BOTH),
							new KeyValue(overlayPane.visibleProperty(), false ,Interpolator.EASE_BOTH)
							),
							new KeyFrame(Duration.millis(10), 
									new KeyValue(overlayPane.visibleProperty(), true ,Interpolator.EASE_BOTH),
									new KeyValue(overlayPane.opacityProperty(), 0, Interpolator.EASE_BOTH)
									),
									new KeyFrame(Duration.millis(1000), 
											new KeyValue(contentHolder.translateYProperty(), 0,Interpolator.EASE_BOTH),
											new KeyValue(overlayPane.opacityProperty(), 1, Interpolator.EASE_BOTH)))
					);
			// reduce the number to increase the shifting , increase number to reduce shifting
			setCycleDuration(Duration.seconds(0.4));
			setDelay(Duration.seconds(0));
		}
	}

	private class CenterTransition extends CachedTransition {
		public CenterTransition() {
			super(contentHolder, new Timeline(
					new KeyFrame(Duration.ZERO, 
							new KeyValue(contentHolder.scaleXProperty(), 0 ,Interpolator.EASE_BOTH),
							new KeyValue(contentHolder.scaleYProperty(), 0 ,Interpolator.EASE_BOTH),
							new KeyValue(overlayPane.visibleProperty(), false ,Interpolator.EASE_BOTH)
							),
							new KeyFrame(Duration.millis(10), 
									new KeyValue(overlayPane.visibleProperty(), true ,Interpolator.EASE_BOTH),
									new KeyValue(overlayPane.opacityProperty(), 0,Interpolator.EASE_BOTH)
									),							
									new KeyFrame(Duration.millis(1000), 							
											new KeyValue(contentHolder.scaleXProperty(), 1 ,Interpolator.EASE_BOTH),
											new KeyValue(contentHolder.scaleYProperty(), 1 ,Interpolator.EASE_BOTH),
											new KeyValue(overlayPane.opacityProperty(), 1, Interpolator.EASE_BOTH)
											))
					);
			// reduce the number to increase the shifting , increase number to reduce shifting
			setCycleDuration(Duration.seconds(0.4));
			setDelay(Duration.seconds(0));
		}
	}


	/***************************************************************************
	 *                                                                         *
	 * Stylesheet Handling                                                     *
	 *                                                                         *
	 **************************************************************************/

	private static final String DEFAULT_STYLE_CLASS = "jfx-dialog";


	private StyleableObjectProperty<DialogTransition> transitionType = new SimpleStyleableObjectProperty<DialogTransition>(StyleableProperties.DIALOG_TRANSITION, JFXDialog.this, "dialogTransition", DialogTransition.CENTER );

	public DialogTransition getTransitionType(){
		return transitionType == null ? DialogTransition.CENTER : transitionType.get();
	}
	public StyleableObjectProperty<DialogTransition> transitionTypeProperty(){		
		return this.transitionType;
	}
	public void setTransitionType(DialogTransition transition){
		this.transitionType.set(transition);
	}


	private static class StyleableProperties {
		private static final CssMetaData< JFXDialog, DialogTransition> DIALOG_TRANSITION =
				new CssMetaData< JFXDialog, DialogTransition>("-fx-dialog-transition",
						DialogTransitionConverter.getInstance(), DialogTransition.CENTER) {
			@Override
			public boolean isSettable(JFXDialog control) {
				return control.transitionType == null || !control.transitionType.isBound();
			}
			@Override
			public StyleableProperty<DialogTransition> getStyleableProperty(JFXDialog control) {
				return control.transitionTypeProperty();
			}
		};

		private static final List<CssMetaData<? extends Styleable, ?>> CHILD_STYLEABLES;
		static {
			final List<CssMetaData<? extends Styleable, ?>> styleables =
					new ArrayList<CssMetaData<? extends Styleable, ?>>(Parent.getClassCssMetaData());
			Collections.addAll(styleables,
					DIALOG_TRANSITION
					);
			CHILD_STYLEABLES = Collections.unmodifiableList(styleables);
		}
	}

	// inherit the styleable properties from parent
	private List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

	@Override
	public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
		if(STYLEABLES == null){
			final List<CssMetaData<? extends Styleable, ?>> styleables =
					new ArrayList<CssMetaData<? extends Styleable, ?>>(Parent.getClassCssMetaData());
			styleables.addAll(getClassCssMetaData());
			styleables.addAll(super.getClassCssMetaData());
			STYLEABLES = Collections.unmodifiableList(styleables);
		}
		return STYLEABLES;
	}
	public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
		return StyleableProperties.CHILD_STYLEABLES;
	}



	/***************************************************************************
	 *                                                                         *
	 * Custom Events                                                           *
	 *                                                                         *
	 **************************************************************************/
	
	private ObjectProperty<EventHandler<? super JFXDialogEvent>> onDialogClosedProperty = new SimpleObjectProperty<>((closed)->{});

	public void setOnDialogClosed(EventHandler<? super JFXDialogEvent> handler){
		onDialogClosedProperty.set(handler);
	}

	public void getOnDialogClosed(EventHandler<? super JFXDialogEvent> handler){
		onDialogClosedProperty.get();
	}


	private ObjectProperty<EventHandler<? super JFXDialogEvent>> onDialogOpenedProperty = new SimpleObjectProperty<>((opened)->{});
	
	public void setOnDialogOpened(EventHandler<? super JFXDialogEvent> handler){
		onDialogOpenedProperty.set(handler);
	}

	public void getOnDialogOpened(EventHandler<? super JFXDialogEvent> handler){
		onDialogOpenedProperty.get();
	}



	
	
}

