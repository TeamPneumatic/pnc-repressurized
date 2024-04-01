/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.gui;

import com.google.common.base.CaseFormat;
import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.common.drone.progwidgets.area.AreaType;
import me.desht.pneumaticcraft.common.util.JsonToNBTConverter;
import me.desht.pneumaticcraft.common.util.LegacyAreaWidgetConverter;
import me.desht.pneumaticcraft.common.util.LegacyAreaWidgetConverter.EnumOldAreaType;
import me.desht.pneumaticcraft.common.util.NBTToJsonConverter;
import me.desht.pneumaticcraft.common.util.PastebinHandler;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class PastebinScreen extends AbstractPneumaticCraftScreen {
    private WidgetTextField usernameBox, passwordBox;
    private WidgetTextField pastebinBox;
    private WidgetCheckBox prettyCB;
    private final CompoundTag pastingNBT;
    private final Screen parentScreen;
    private Component statusMessage = Component.empty();
    private Component lastMessage = Component.empty();
    private int messageTimer;
    private EnumState state = EnumState.NONE;
    CompoundTag outputTag;
    boolean shouldMerge;

    private enum EnumState {
        NONE, GETTING, PUTTING, LOGIN, LOGOUT
    }

    PastebinScreen(Screen parentScreen, CompoundTag tag) {
        super(Component.literal("Pastebin"));
        xSize = 183;
        ySize = 202;
        this.pastingNBT = tag;
        this.parentScreen = parentScreen;
    }

    @Override
    public void init() {
        super.init();

        if (!PastebinHandler.isLoggedIn()) {
            usernameBox = new WidgetTextField(font, guiLeft + 10, guiTop + 30, 80, 10);
            addRenderableWidget(usernameBox);

            passwordBox = new WidgetTextField(font, guiLeft + 10, guiTop + 56, 80, 10).setAsPasswordBox();
            addRenderableWidget(passwordBox);

            WidgetButtonExtended loginButton = new WidgetButtonExtended(guiLeft + 100, guiTop + 30, 60, 20, xlate("pneumaticcraft.gui.pastebin.button.login"), b -> login());
            loginButton.setTooltipText(xlate("pneumaticcraft.gui.pastebin.loginOptional"));
            addRenderableWidget(loginButton);

            addLabel(xlate("pneumaticcraft.gui.pastebin.username"), guiLeft + 10, guiTop + 20);
            addLabel(xlate("pneumaticcraft.gui.pastebin.password"), guiLeft + 10, guiTop + 46);
        } else {
            WidgetButtonExtended logoutButton = new WidgetButtonExtended(guiLeft + 60, guiTop + 30, 60, 20, xlate("pneumaticcraft.gui.pastebin.button.logout"), b -> logout());
            addRenderableWidget(logoutButton);
        }

        pastebinBox = new WidgetTextField(font, guiLeft + 10, guiTop + 130, 160, 10) {
            @Override
            public void setFocused(boolean focused) {
                final boolean previousFocus = isFocused();
                super.setFocused(focused);

                if (previousFocus != focused && focused) {
                    moveCursorToEnd(true);
                    setHighlightPos(0);
                }
            }
        };
        addRenderableWidget(pastebinBox);

        WidgetButtonExtended pasteButton = new WidgetButtonExtended(guiLeft + 31, guiTop + 78, 120, 20, xlate("pneumaticcraft.gui.pastebin.button.upload"), b -> sendToPastebin());
        addRenderableWidget(pasteButton);
        WidgetButtonExtended getButton = new WidgetButtonExtended(guiLeft + 31, guiTop + 167, 120, 20, xlate("pneumaticcraft.gui.pastebin.button.get"), b -> getFromPastebin());
        addRenderableWidget(getButton);

        WidgetButtonExtended putInClipBoard = new WidgetButtonExtended(guiLeft + 8, guiTop + 78, 20, 20, Component.empty(), b -> putToClipboard());
        putInClipBoard.setRenderedIcon(Textures.GUI_COPY_ICON_LOCATION);
        putInClipBoard.setTooltipText(xlate("pneumaticcraft.gui.pastebin.button.copyToClipboard"));
        addRenderableWidget(putInClipBoard);
        WidgetButtonExtended retrieveFromClipboard = new WidgetButtonExtended(guiLeft + 8, guiTop + 167, 20, 20, Component.empty(), b -> getFromClipboard());
        retrieveFromClipboard.setRenderedIcon(Textures.GUI_PASTE_ICON_LOCATION);
        retrieveFromClipboard.setTooltipText(xlate("pneumaticcraft.gui.pastebin.button.loadFromClipboard"));
        addRenderableWidget(retrieveFromClipboard);

        prettyCB = new WidgetCheckBox(0, guiTop + 102, 0xFF404040, xlate("pneumaticcraft.gui.pastebin.pretty"),
                b -> shouldMerge = b.checked);
        prettyCB.setX(guiLeft + (170 - prettyCB.getWidth()));
        prettyCB.setTooltip(Tooltip.create(xlate("pneumaticcraft.gui.pastebin.pretty.tooltip")));
        addRenderableWidget(prettyCB);

        if (parentScreen instanceof ProgrammerScreen) {
            WidgetCheckBox mergeCB = new WidgetCheckBox(0, guiTop + 155, 0xFF404040, xlate("pneumaticcraft.gui.pastebin.merge"),
                    b -> shouldMerge = b.checked);
            mergeCB.setX(guiLeft + (170 - mergeCB.getWidth()));
            mergeCB.setTooltip(Tooltip.create(xlate("pneumaticcraft.gui.pastebin.merge.tooltip")));
            addRenderableWidget(mergeCB);
        }

        addLabel(xlate("pneumaticcraft.gui.pastebin.pastebinLink"), guiLeft + 10, guiTop + 120);
    }

    private void login() {
        PastebinHandler.login(usernameBox.getValue(), passwordBox.getValue());
        state = EnumState.LOGIN;
        statusMessage = xlate("pneumaticcraft.gui.pastebin.loggingIn");
    }

    private void logout() {
        PastebinHandler.logout();
        state = EnumState.LOGOUT;
    }

    private void sendToPastebin() {
        PastebinHandler.put(new NBTToJsonConverter(pastingNBT).convert(prettyCB.checked));
        state = EnumState.PUTTING;
        statusMessage = xlate("pneumaticcraft.gui.pastebin.uploadingToPastebin");
    }

    private void getFromPastebin() {
        PastebinHandler.get(pastebinBox.getValue());
        state = EnumState.GETTING;
        statusMessage = xlate("pneumaticcraft.gui.pastebin.retrievingFromPastebin");
    }

    private void putToClipboard() {
        minecraft.keyboardHandler.setClipboard(new NBTToJsonConverter(pastingNBT).convert(prettyCB.checked));
        statusMessage = xlate("pneumaticcraft.gui.pastebin.clipboardSetToContents");
    }

    private void getFromClipboard() {
        readFromString(minecraft.keyboardHandler.getClipboard());
//        statusMessage = xlate("pneumaticcraft.gui.pastebin.retrievedFromClipboard");
    }

    @Override
    public void tick() {
        super.tick();

        if (state == EnumState.LOGOUT) {
            state = EnumState.NONE;
            init(minecraft, this.width, this.height);
        }
        if (state != EnumState.NONE && PastebinHandler.isDone()) {
            statusMessage = Component.empty();
            String pastebinText;
            switch (state) {
                case GETTING -> {
                    pastebinText = PastebinHandler.getHandler().contents;
                    if (pastebinText != null) {
                        readFromString(pastebinText);
                    } else {
                        statusMessage = xlate("pneumaticcraft.gui.pastebin.invalidPastebin");
                    }
                }
                case PUTTING -> {
                    if (PastebinHandler.getException() != null) {
                        statusMessage = Component.literal(PastebinHandler.getException().getMessage());
                    } else {
                        pastebinText = PastebinHandler.getHandler().getLink;
                        if (pastebinText == null) pastebinText = "<ERROR>";
                        if (pastebinText.contains("pastebin.com")) {
                            pastebinBox.setValue(pastebinText);
                            setTempMessage(xlate("pneumaticcraft.gui.pastebin.uploadedToPastebin"));
                        } else {
                            statusMessage = Component.literal(pastebinText);
                        }
                    }
                }
                case LOGIN -> {
                    if (!PastebinHandler.isLoggedIn()) {
                        statusMessage = xlate("pneumaticcraft.gui.pastebin.invalidLogin");
                    }
                    init();
                }
            }
            state = EnumState.NONE;
        }
        if (messageTimer > 0 && --messageTimer <= 0) {
            lastMessage = Component.empty();
        }
    }

    private void setTempMessage(Component msg) {
        lastMessage = msg;
        messageTimer = 60;
    }

    private void readFromString(String string) {
        try {
            outputTag = new JsonToNBTConverter(string).convert();
            if (outputTag.contains("widgets")) {
                doLegacyConversion(outputTag);
            }
            setTempMessage(xlate("pneumaticcraft.gui.pastebin.retrievedFromPastebin"));
        } catch (Exception e) {
            e.printStackTrace();
            setTempMessage(xlate("pneumaticcraft.gui.pastebin.invalidFormattedPastebin").withStyle(ChatFormatting.GOLD));
        }
    }

    /**
     * Handle legacy conversion: PNC 1.12.2 and older used a simple (mixed case) widget string
     * but now ProgWidgets are registry entries and use a ResourceLocation.  Also, convert any
     * Area widgets from the old-style format if necessary.
     *
     * @param nbt the legacy data to convert
     */
    private void doLegacyConversion(CompoundTag nbt) {
        ListTag l = nbt.getList("widgets", Tag.TAG_COMPOUND);
        int areaConversions = 0;
        for (int i = 0; i < l.size(); i++) {
            CompoundTag subTag = l.getCompound(i);
            String newName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, subTag.getString("name"));
            subTag.putString("name", Names.MOD_ID + ":" + newName);
            if (newName.equals("area")) {
                EnumOldAreaType oldType = EnumOldAreaType.values()[subTag.getInt("type")];
                AreaType newType = LegacyAreaWidgetConverter.convertFromLegacyFormat(oldType, subTag.getInt("typeInfo"));
                subTag.putString("type", newType.getName());
                newType.writeToNBT(subTag);
                areaConversions++;
            }
        }
        nbt.put(IProgrammable.NBT_WIDGETS, l);
        nbt.remove("widgets");
        if (areaConversions > 0) {
            Log.info("Pastebin import: converted {} legacy area widgets", areaConversions);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float partialTicks) {
        renderBackground(graphics, x, y, partialTicks);

        super.render(graphics, x, y, partialTicks);

        if (!statusMessage.getString().isEmpty()) {
            graphics.drawString(font, statusMessage, guiLeft + 5, guiTop + 5, 0xFFFFFF00, false);
        } else if (!lastMessage.getString().isEmpty()) {
            graphics.drawString(font, lastMessage, guiLeft + 5, guiTop + 5, 0xFF00FF00, false);
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parentScreen);
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.GUI_PASTEBIN;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
