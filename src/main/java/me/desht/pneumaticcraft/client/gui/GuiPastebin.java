package me.desht.pneumaticcraft.client.gui;

import com.google.common.base.CaseFormat;
import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.common.progwidgets.area.AreaType;
import me.desht.pneumaticcraft.common.util.*;
import me.desht.pneumaticcraft.common.util.LegacyAreaWidgetConverter.EnumOldAreaType;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.util.Constants;

public class GuiPastebin extends GuiPneumaticScreenBase {

    private WidgetTextField usernameBox, passwordBox;
    private WidgetTextField pastebinBox;
    private WidgetCheckBox prettyCB;
//    private final String pastingString;
    private final CompoundNBT pastingNBT;
    private final Screen parentScreen;
    private String statusMessage = "";
    private String lastMessage = "";
    private int messageTimer;
    private EnumState state = EnumState.NONE;
    CompoundNBT outputTag;
    boolean shouldMerge;

    private enum EnumState {
        NONE, GETTING, PUTTING, LOGIN, LOGOUT
    }

//    private GuiPastebin(Screen parentScreen, Comnp pastingString) {
//        super(new StringTextComponent("Pastebin"));
//
//        xSize = 183;
//        ySize = 202;
////        this.pastingString = pastingString;
//        this.pastingNBT =
//        this.parentScreen = parentScreen;
//        Minecraft.getInstance().keyboardListener.enableRepeatEvents(true);
//    }

    GuiPastebin(Screen parentScreen, CompoundNBT tag) {
        super(new StringTextComponent("Pastebin"));
        xSize = 183;
        ySize = 202;
        this.pastingNBT = tag;
        this.parentScreen = parentScreen;
        Minecraft.getInstance().keyboardListener.enableRepeatEvents(true);
    }

    @Override
    public void init() {
        super.init();

        if (!PastebinHandler.isLoggedIn()) {
            usernameBox = new WidgetTextField(font, guiLeft + 10, guiTop + 30, 80, 10);
            addButton(usernameBox);

            passwordBox = new WidgetTextField(font, guiLeft + 10, guiTop + 56, 80, 10).setAsPasswordBox();
            addButton(passwordBox);

            WidgetButtonExtended loginButton = new WidgetButtonExtended(guiLeft + 100, guiTop + 30, 60, 20, I18n.format("pneumaticcraft.gui.pastebin.button.login"), b -> login());
            loginButton.setTooltipText("Pastebin login is optional");
            addButton(loginButton);

            addLabel(I18n.format("pneumaticcraft.gui.pastebin.username"), guiLeft + 10, guiTop + 20);
            addLabel(I18n.format("pneumaticcraft.gui.pastebin.password"), guiLeft + 10, guiTop + 46);
        } else {
            WidgetButtonExtended logoutButton = new WidgetButtonExtended(guiLeft + 60, guiTop + 30, 60, 20, I18n.format("pneumaticcraft.gui.pastebin.button.logout"), b -> logout());
            addButton(logoutButton);
        }

        pastebinBox = new WidgetTextField(font, guiLeft + 10, guiTop + 130, 160, 10) {
            @Override
            protected void onFocusedChanged(boolean focused) {
                if (focused) {
                    setCursorPositionEnd();
                    setSelectionPos(0);
                }
                super.onFocusedChanged(focused);
            }
        };
        addButton(pastebinBox);

        WidgetButtonExtended pasteButton = new WidgetButtonExtended(guiLeft + 31, guiTop + 78, 120, 20, I18n.format("pneumaticcraft.gui.pastebin.button.upload"), b -> sendToPastebin());
        addButton(pasteButton);
        WidgetButtonExtended getButton = new WidgetButtonExtended(guiLeft + 31, guiTop + 167, 120, 20, I18n.format("pneumaticcraft.gui.pastebin.button.get"), b -> getFromPastebin());
        addButton(getButton);

        WidgetButtonExtended putInClipBoard = new WidgetButtonExtended(guiLeft + 8, guiTop + 78, 20, 20, "", b -> putToClipboard());
        putInClipBoard.setRenderedIcon(Textures.GUI_COPY_ICON_LOCATION);
        putInClipBoard.setTooltipText(I18n.format("pneumaticcraft.gui.pastebin.button.copyToClipboard"));
        addButton(putInClipBoard);
        WidgetButtonExtended retrieveFromClipboard = new WidgetButtonExtended(guiLeft + 8, guiTop + 167, 20, 20, "", b -> getFromClipboard());
        retrieveFromClipboard.setRenderedIcon(Textures.GUI_PASTE_ICON_LOCATION);
        retrieveFromClipboard.setTooltipText(I18n.format("pneumaticcraft.gui.pastebin.button.loadFromClipboard"));
        addButton(retrieveFromClipboard);

        prettyCB = new WidgetCheckBox(0, guiTop + 102, 0xFF404040, I18n.format("pneumaticcraft.gui.pastebin.pretty"),
                b -> shouldMerge = b.checked);
        prettyCB.x = guiLeft + (170 - prettyCB.getWidth());
        prettyCB.setTooltip(PneumaticCraftUtils.splitString(I18n.format("pneumaticcraft.gui.pastebin.pretty.tooltip")));
        addButton(prettyCB);

        WidgetCheckBox mergeCB = new WidgetCheckBox(0, guiTop + 155, 0xFF404040, I18n.format("pneumaticcraft.gui.pastebin.merge"),
                b -> shouldMerge = b.checked);
        mergeCB.x = guiLeft + (170 - mergeCB.getWidth());
        mergeCB.setTooltip(PneumaticCraftUtils.splitString(I18n.format("pneumaticcraft.gui.pastebin.merge.tooltip")));
        addButton(mergeCB);

        addLabel(I18n.format("pneumaticcraft.gui.pastebin.pastebinLink"), guiLeft + 10, guiTop + 120);
    }

    private void login() {
        PastebinHandler.login(usernameBox.getText(), passwordBox.getText());
        state = EnumState.LOGIN;
        statusMessage = I18n.format("pneumaticcraft.gui.pastebin.loggingIn");
    }

    private void logout() {
        PastebinHandler.logout();
        state = EnumState.LOGOUT;
    }

    private void sendToPastebin() {
        PastebinHandler.put(new NBTToJsonConverter(pastingNBT).convert(prettyCB.checked));
        state = EnumState.PUTTING;
        statusMessage = I18n.format("pneumaticcraft.gui.pastebin.uploadingToPastebin");
    }

    private void getFromPastebin() {
        PastebinHandler.get(pastebinBox.getText());
        state = EnumState.GETTING;
        statusMessage = I18n.format("pneumaticcraft.gui.pastebin.retrievingFromPastebin");
    }

    private void putToClipboard() {
        minecraft.keyboardListener.setClipboardString(new NBTToJsonConverter(pastingNBT).convert(prettyCB.checked));
        statusMessage = I18n.format("pneumaticcraft.gui.pastebin.clipboardSetToContents");
    }

    private void getFromClipboard() {
        readFromString(minecraft.keyboardListener.getClipboardString());
        statusMessage = I18n.format("pneumaticcraft.gui.pastebin.retrievedFromClipboard");
    }

    @Override
    public void tick() {
        super.tick();

        if (state == EnumState.LOGOUT) {
            state = EnumState.NONE;
            init(minecraft, this.width, this.height);
        }
        if (state != EnumState.NONE && PastebinHandler.isDone()) {
            statusMessage = "";
            String pastebinText;
            switch (state) {
                case GETTING:
                    pastebinText = PastebinHandler.getHandler().contents;
                    if (pastebinText != null) {
                        readFromString(pastebinText);
                    } else {
                        statusMessage = I18n.format("pneumaticcraft.gui.pastebin.invalidPastebin");
                    }
                    break;
                case PUTTING:
                    if (PastebinHandler.getException() != null) {
                        statusMessage = PastebinHandler.getException().getMessage();
                    } else {
                        pastebinText = PastebinHandler.getHandler().getLink;
                        if (pastebinText == null) pastebinText = "<ERROR>";
                        if (pastebinText.contains("pastebin.com")) {
                            pastebinBox.setText(pastebinText);
                            setTempMessage(I18n.format("pneumaticcraft.gui.pastebin.uploadedToPastebin"));
                        } else {
                            statusMessage = pastebinText;
                        }
                    }
                    break;
                case LOGIN:
                    if (!PastebinHandler.isLoggedIn()) {
                        statusMessage = I18n.format("pneumaticcraft.gui.pastebin.invalidLogin");
                    }
                    init();
            }
            state = EnumState.NONE;
        }
        if (messageTimer > 0 && --messageTimer <= 0) {
            lastMessage = "";
        }
    }

    private void setTempMessage(String msg) {
        lastMessage = msg;
        messageTimer = 60;
    }

    private void readFromString(String string) {
        try {
            outputTag = new JsonToNBTConverter(string).convert();
            if (outputTag.contains("widgets")) {
                doLegacyConversion(outputTag);
            }
            setTempMessage(I18n.format("pneumaticcraft.gui.pastebin.retrievedFromPastebin"));
        } catch (Exception e) {
            e.printStackTrace();
            statusMessage = I18n.format("pneumaticcraft.gui.pastebin.invalidFormattedPastebin");
        }
    }

    /**
     * Handle legacy conversion: PNC 1.12.2 and older used a simple (mixed case) widget string
     * but now ProgWidgets are registry entries and use a ResourceLocation.  Also, convert any
     * Area widgets from the old-style format if necessary.
     *
     * @param nbt the legacy data to convert
     */
    private void doLegacyConversion(CompoundNBT nbt) {
        ListNBT l = nbt.getList("widgets", Constants.NBT.TAG_COMPOUND);
        int areaConversions = 0;
        for (int i = 0; i < l.size(); i++) {
            CompoundNBT subTag = l.getCompound(i);
            String newName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, subTag.getString("name"));
            subTag.putString("name", Names.MOD_ID + ":" + newName);
            if (newName.equals("area")) {
                EnumOldAreaType oldType = EnumOldAreaType.values()[subTag.getInt("type")];
                AreaType newType = LegacyAreaWidgetConverter.convertFromLegacyFormat(oldType, subTag.getInt("typeInfo"));
                subTag.putString("type", newType.getName().toLowerCase());
                newType.writeToNBT(subTag);
                areaConversions++;
            }
        }
        nbt.put(IProgrammable.NBT_WIDGETS, l);
        nbt.remove("widgets");
        if (areaConversions > 0) {
            Log.info("Pastebin import: converted %d legacy area widgets", areaConversions);
        }
    }

    @Override
    public void render(int x, int y, float partialTicks) {
        renderBackground();

        super.render(x, y, partialTicks);

        if (!statusMessage.isEmpty()) {
            font.drawStringWithShadow(statusMessage, guiLeft + 5, guiTop + 5, 0xFFFFFF00);
        } else if (!lastMessage.isEmpty()) {
            font.drawStringWithShadow(lastMessage, guiLeft + 5, guiTop + 5, 0xFF00FF00);
        }
    }

    @Override
    public void onClose() {
        minecraft.keyboardListener.enableRepeatEvents(false);
        minecraft.displayGuiScreen(parentScreen);
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
