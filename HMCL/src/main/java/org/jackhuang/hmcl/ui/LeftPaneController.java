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
package org.jackhuang.hmcl.ui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import org.jackhuang.hmcl.Main;
import org.jackhuang.hmcl.auth.yggdrasil.YggdrasilAccount;
import org.jackhuang.hmcl.event.EventBus;
import org.jackhuang.hmcl.event.ProfileChangedEvent;
import org.jackhuang.hmcl.event.ProfileLoadingEvent;
import org.jackhuang.hmcl.game.AccountHelper;
import org.jackhuang.hmcl.setting.Profile;
import org.jackhuang.hmcl.setting.Settings;
import org.jackhuang.hmcl.ui.construct.IconedItem;
import org.jackhuang.hmcl.ui.construct.RipplerContainer;
import org.jackhuang.hmcl.util.Lang;
import org.jackhuang.hmcl.util.Pair;

import java.util.LinkedList;
import java.util.Objects;

public final class LeftPaneController {
    private final AdvancedListBox leftPane;
    private final VBox profilePane = new VBox();
    private final VersionListItem accountItem = new VersionListItem("No Account", "unknown");

    public LeftPaneController(AdvancedListBox leftPane) {
        this.leftPane = leftPane;

        leftPane.startCategory(Main.i18n("account").toUpperCase())
                .add(Lang.apply(new RipplerContainer(accountItem), rippler -> {
                    rippler.setOnMouseClicked(e -> Controllers.navigate(new AccountsPage()));
                    accountItem.setOnSettingsButtonClicked(() -> Controllers.navigate(new AccountsPage()));
                }))
                .startCategory(Main.i18n("launcher").toUpperCase())
                .add(Lang.apply(new IconedItem(SVG.gear("black", 20, 20), Main.i18n("launcher_settings")), iconedItem -> {
                    iconedItem.prefWidthProperty().bind(leftPane.widthProperty());
                    iconedItem.setOnMouseClicked(e -> Controllers.navigate(Controllers.getSettingsPage()));
                }))
                .startCategory(Main.i18n("profile"))
                .add(profilePane);

        EventBus.EVENT_BUS.channel(ProfileLoadingEvent.class).register(this::onProfilesLoading);
        EventBus.EVENT_BUS.channel(ProfileChangedEvent.class).register(this::onProfileChanged);

        Controllers.getDecorator().getAddMenuButton().setOnMouseClicked(e ->
                Controllers.getDecorator().showPage(new ProfilePage(null))
        );

        FXUtils.onChangeAndOperate(Settings.INSTANCE.selectedAccountProperty(), it -> {
            if (it == null) {
                accountItem.setVersionName("mojang@mojang.com");
                accountItem.setGameVersion("Yggdrasil");
            } else {
                accountItem.setVersionName(it.getUsername());
                accountItem.setGameVersion(AccountsPage.accountType(it));
            }

            if (it instanceof YggdrasilAccount)
                accountItem.setImage(AccountHelper.getSkin((YggdrasilAccount) it, 4), AccountHelper.getViewport(4));
            else
                accountItem.setImage(FXUtils.DEFAULT_ICON, null);
        });

        if (Settings.INSTANCE.getAccounts().isEmpty())
            Controllers.navigate(new AccountsPage());
    }

    public void onProfileChanged(ProfileChangedEvent event) {
        Profile profile = event.getProfile();

        for (Node node : profilePane.getChildren()) {
            if (node instanceof RipplerContainer && node.getProperties().get("profile") instanceof Pair<?, ?>) {
                ((RipplerContainer) node).setSelected(Objects.equals(((Pair) node.getProperties().get("profile")).getKey(), profile.getName()));
            }
        }
    }

    public void onProfilesLoading() {
        LinkedList<RipplerContainer> list = new LinkedList<>();
        for (Profile profile : Settings.INSTANCE.getProfiles()) {
            VersionListItem item = new VersionListItem(profile.getName());
            RipplerContainer ripplerContainer = new RipplerContainer(item);
            item.setOnSettingsButtonClicked(() -> Controllers.getDecorator().showPage(new ProfilePage(profile)));
            ripplerContainer.setRipplerFill(Paint.valueOf("#89E1F9"));
            ripplerContainer.setOnMouseClicked(e -> {
                // clean selected property
                for (Node node : profilePane.getChildren())
                    if (node instanceof RipplerContainer)
                        ((RipplerContainer) node).setSelected(false);
                ripplerContainer.setSelected(true);
                Settings.INSTANCE.setSelectedProfile(profile);
            });
            ripplerContainer.getProperties().put("profile", new Pair<>(profile.getName(), item));
            ripplerContainer.maxWidthProperty().bind(leftPane.widthProperty());
            list.add(ripplerContainer);
        }
        Platform.runLater(() -> profilePane.getChildren().setAll(list));
    }
}
