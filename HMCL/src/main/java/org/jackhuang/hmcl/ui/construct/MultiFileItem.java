/*
 * Hello Minecraft! Launcher.
 * Copyright (C) 2017  huangyuhui <huanghongxun2008@126.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see {http://www.gnu.org/licenses/}.
 */
package org.jackhuang.hmcl.ui.construct;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import org.jackhuang.hmcl.Main;
import org.jackhuang.hmcl.ui.Controllers;
import org.jackhuang.hmcl.ui.FXUtils;
import org.jackhuang.hmcl.ui.SVG;

import java.io.File;
import java.util.Collection;

public class MultiFileItem extends ComponentList {
    private final StringProperty customText = new SimpleStringProperty(this, "customText", "Custom");
    private final StringProperty chooserTitle = new SimpleStringProperty(this, "chooserTitle", "Select a file");

    private final ToggleGroup group = new ToggleGroup();
    private final JFXTextField txtCustom = new JFXTextField();
    private final JFXButton btnSelect = new JFXButton();
    private final JFXRadioButton radioCustom = new JFXRadioButton();
    private final BorderPane custom = new BorderPane();
    private final VBox pane = new VBox();

    {
        BorderPane.setAlignment(txtCustom, Pos.CENTER_RIGHT);

        btnSelect.setGraphic(SVG.folderOpen("black", 15, 15));
        btnSelect.setOnMouseClicked(e -> {
            // TODO
        });

        radioCustom.textProperty().bind(customTextProperty());
        radioCustom.setToggleGroup(group);
        txtCustom.disableProperty().bind(radioCustom.selectedProperty().not());
        btnSelect.disableProperty().bind(radioCustom.selectedProperty().not());

        custom.setLeft(radioCustom);
        custom.setStyle("-fx-padding: 3;");
        HBox right = new HBox();
        right.setSpacing(3);
        right.getChildren().addAll(txtCustom, btnSelect);
        custom.setRight(right);
        FXUtils.limitHeight(custom, 20);

        pane.setStyle("-fx-padding: 0 0 10 0;");
        pane.setSpacing(8);
        pane.getChildren().add(custom);
        addChildren(pane);
    }

    public Node createChildren(String title) {
        return createChildren(title, null);
    }

    public Node createChildren(String title, Object userData) {
        return createChildren(title, "", userData);
    }

    public Node createChildren(String title, String subtitle, Object userData) {
        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-padding: 3;");
        FXUtils.limitHeight(pane, 20);

        JFXRadioButton left = new JFXRadioButton(title);
        left.setToggleGroup(group);
        left.setUserData(userData);
        pane.setLeft(left);

        Label right = new Label(subtitle);
        right.getStyleClass().add("subtitle-label");
        right.setStyle("-fx-font-size: 10;");
        pane.setRight(right);

        return pane;
    }

    public void loadChildren(Collection<Node> list) {
        pane.getChildren().setAll(list);
        pane.getChildren().add(custom);
    }

    public void onExploreJavaDir() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(Main.i18n(getChooserTitle()));
        File selectedDir = chooser.showDialog(Controllers.getStage());
        if (selectedDir != null)
            txtCustom.setText(selectedDir.getAbsolutePath());
    }

    public ToggleGroup getGroup() {
        return group;
    }

    public String getCustomText() {
        return customText.get();
    }

    public StringProperty customTextProperty() {
        return customText;
    }

    public void setCustomText(String customText) {
        this.customText.set(customText);
    }

    public String getChooserTitle() {
        return chooserTitle.get();
    }

    public StringProperty chooserTitleProperty() {
        return chooserTitle;
    }

    public void setChooserTitle(String chooserTitle) {
        this.chooserTitle.set(chooserTitle);
    }

    public void setCustomUserData(Object userData) {
        radioCustom.setUserData(userData);
    }

    public JFXRadioButton getRadioCustom() {
        return radioCustom;
    }

    public JFXTextField getTxtCustom() {
        return txtCustom;
    }
}
