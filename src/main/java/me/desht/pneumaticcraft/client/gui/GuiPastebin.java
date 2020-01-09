package me.desht.pneumaticcraft.client.gui;

import com.google.common.base.CaseFormat;
import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.common.util.JsonToNBTConverter;
import me.desht.pneumaticcraft.common.util.NBTToJsonConverter;
import me.desht.pneumaticcraft.common.util.PastebinHandler;
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
    private final String pastingString;
    CompoundNBT outputTag;
    private final Screen parentScreen;
    private String statusMessage;
    private EnumState state = EnumState.NONE;

    private enum EnumState {
        NONE, GETTING, PUTTING, LOGIN, LOGOUT
    }

    private GuiPastebin(Screen parentScreen, String pastingString) {
        super(new StringTextComponent("Pastebin"));

        xSize = 183;
        ySize = 202;
        this.pastingString = pastingString;
        this.parentScreen = parentScreen;
        Minecraft.getInstance().keyboardListener.enableRepeatEvents(true);
    }

    GuiPastebin(Screen parentScreen, CompoundNBT tag) {
        this(parentScreen, new NBTToJsonConverter(tag).convert(true));
    }

    @Override
    public void init() {
        super.init();

        if (!PastebinHandler.isLoggedIn()) {
            usernameBox = new WidgetTextField(font, guiLeft + 10, guiTop + 30, 80, 10);
            addButton(usernameBox);

            passwordBox = new WidgetTextField(font, guiLeft + 10, guiTop + 56, 80, 10).setAsPasswordBox();
            addButton(passwordBox);

            WidgetButtonExtended loginButton = new WidgetButtonExtended(guiLeft + 100, guiTop + 30, 60, 20, I18n.format("gui.pastebin.button.login"), b -> login());
            loginButton.setTooltipText("Pastebin login is optional");
            addButton(loginButton);

            addLabel(I18n.format("gui.pastebin.username"), guiLeft + 10, guiTop + 20);
            addLabel(I18n.format("gui.pastebin.password"), guiLeft + 10, guiTop + 46);
        } else {
            WidgetButtonExtended logoutButton = new WidgetButtonExtended(guiLeft + 60, guiTop + 30, 60, 20, I18n.format("gui.pastebin.button.logout"), b -> logout());
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

        WidgetButtonExtended pasteButton = new WidgetButtonExtended(guiLeft + 31, guiTop + 78, 120, 20, I18n.format("gui.pastebin.button.upload"), b -> sendToPastebin());
        addButton(pasteButton);
        WidgetButtonExtended getButton = new WidgetButtonExtended(guiLeft + 31, guiTop + 167, 120, 20, I18n.format("gui.pastebin.button.get"), b -> getFromPastebin());
        addButton(getButton);

        WidgetButtonExtended putInClipBoard = new WidgetButtonExtended(guiLeft + 8, guiTop + 78, 20, 20, "", b -> putToClipboard());
        putInClipBoard.setRenderedIcon(Textures.GUI_COPY_ICON_LOCATION);
        putInClipBoard.setTooltipText(I18n.format("gui.pastebin.button.copyToClipboard"));
        addButton(putInClipBoard);
        WidgetButtonExtended retrieveFromClipboard = new WidgetButtonExtended(guiLeft + 8, guiTop + 167, 20, 20, "", b -> getFromClipboard());
        retrieveFromClipboard.setRenderedIcon(Textures.GUI_PASTE_ICON_LOCATION);
        retrieveFromClipboard.setTooltipText(I18n.format("gui.pastebin.button.loadFromClipboard"));
        addButton(retrieveFromClipboard);

        addLabel(I18n.format("gui.pastebin.pastebinLink"), guiLeft + 10, guiTop + 120);
    }

    private void login() {
        PastebinHandler.login(usernameBox.getText(), passwordBox.getText());
        state = EnumState.LOGIN;
        statusMessage = I18n.format("gui.pastebin.loggingIn");
    }

    private void logout() {
        PastebinHandler.logout();
        state = EnumState.LOGOUT;
    }

    private void sendToPastebin() {
        PastebinHandler.put(pastingString);
        state = EnumState.PUTTING;
        statusMessage = I18n.format("gui.pastebin.uploadingToPastebin");
    }

    private void getFromPastebin() {
        PastebinHandler.get(pastebinBox.getText());
        state = EnumState.GETTING;
        statusMessage = I18n.format("gui.pastebin.retrievingFromPastebin");
    }

    private void putToClipboard() {
        minecraft.keyboardListener.setClipboardString(pastingString);
        statusMessage = I18n.format("gui.pastebin.clipboardSetToContents");
    }

    private void getFromClipboard() {
        readFromString(minecraft.keyboardListener.getClipboardString());
        statusMessage = I18n.format("gui.pastebin.retrievedFromClipboard");
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
                        statusMessage = I18n.format("gui.pastebin.invalidPastebin");
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
                        } else {
                            statusMessage = pastebinText;
                        }
                    }
                    break;
                case LOGIN:
                    if (!PastebinHandler.isLoggedIn()) {
                        statusMessage = I18n.format("gui.pastebin.invalidLogin");
                    }
                    init();
            }
            state = EnumState.NONE;
        }
    }

    private void readFromString(String string) {
        try {
            outputTag = new JsonToNBTConverter(string).convert();
            if (outputTag.contains("widgets")) {
                doLegacyConversion(outputTag);
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusMessage = I18n.format("gui.pastebin.invalidFormattedPastebin");
        }
    }

    /**
     * Handle legacy conversion: PNC 1.12.2 and older used a simple (mixed case) widget string
     * but now ProgWidgets are registry entries and use a ResourceLocation
     * @param outputTag the legacy data to convert
     */
    private void doLegacyConversion(CompoundNBT outputTag) {
        ListNBT l = outputTag.getList("widgets", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < l.size(); i++) {
            CompoundNBT tag = l.getCompound(i);
            String newName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, tag.getString("name"));
            tag.putString("name", Names.MOD_ID + ":" + newName);
        }
        outputTag.put(IProgrammable.NBT_WIDGETS, l);
        outputTag.remove("widgets");
    }

    @Override
    public void render(int x, int y, float partialTicks) {
        renderBackground();

        super.render(x, y, partialTicks);

        if (statusMessage != null && !statusMessage.isEmpty()) {
            font.drawString(statusMessage, guiLeft + 5, guiTop + 5, 0xFFFF0000);
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
