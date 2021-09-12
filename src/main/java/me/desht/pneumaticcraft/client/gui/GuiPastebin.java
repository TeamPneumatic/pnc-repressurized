package me.desht.pneumaticcraft.client.gui;

import com.google.common.base.CaseFormat;
import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.common.progwidgets.area.AreaType;
import me.desht.pneumaticcraft.common.util.JsonToNBTConverter;
import me.desht.pneumaticcraft.common.util.LegacyAreaWidgetConverter;
import me.desht.pneumaticcraft.common.util.LegacyAreaWidgetConverter.EnumOldAreaType;
import me.desht.pneumaticcraft.common.util.NBTToJsonConverter;
import me.desht.pneumaticcraft.common.util.PastebinHandler;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiPastebin extends GuiPneumaticScreenBase {
    private WidgetTextField usernameBox, passwordBox;
    private WidgetTextField pastebinBox;
    private WidgetCheckBox prettyCB;
    private final CompoundNBT pastingNBT;
    private final Screen parentScreen;
    private ITextComponent statusMessage = StringTextComponent.EMPTY;
    private ITextComponent lastMessage = StringTextComponent.EMPTY;
    private int messageTimer;
    private EnumState state = EnumState.NONE;
    CompoundNBT outputTag;
    boolean shouldMerge;

    private enum EnumState {
        NONE, GETTING, PUTTING, LOGIN, LOGOUT
    }

    GuiPastebin(Screen parentScreen, CompoundNBT tag) {
        super(new StringTextComponent("Pastebin"));
        xSize = 183;
        ySize = 202;
        this.pastingNBT = tag;
        this.parentScreen = parentScreen;
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);
    }

    @Override
    public void init() {
        super.init();

        if (!PastebinHandler.isLoggedIn()) {
            usernameBox = new WidgetTextField(font, guiLeft + 10, guiTop + 30, 80, 10);
            addButton(usernameBox);

            passwordBox = new WidgetTextField(font, guiLeft + 10, guiTop + 56, 80, 10).setAsPasswordBox();
            addButton(passwordBox);

            WidgetButtonExtended loginButton = new WidgetButtonExtended(guiLeft + 100, guiTop + 30, 60, 20, xlate("pneumaticcraft.gui.pastebin.button.login"), b -> login());
            loginButton.setTooltipText(xlate("pneumaticcraft.gui.pastebin.loginOptional"));
            addButton(loginButton);

            addLabel(xlate("pneumaticcraft.gui.pastebin.username"), guiLeft + 10, guiTop + 20);
            addLabel(xlate("pneumaticcraft.gui.pastebin.password"), guiLeft + 10, guiTop + 46);
        } else {
            WidgetButtonExtended logoutButton = new WidgetButtonExtended(guiLeft + 60, guiTop + 30, 60, 20, xlate("pneumaticcraft.gui.pastebin.button.logout"), b -> logout());
            addButton(logoutButton);
        }

        pastebinBox = new WidgetTextField(font, guiLeft + 10, guiTop + 130, 160, 10) {
            @Override
            protected void onFocusedChanged(boolean focused) {
                if (focused) {
                    moveCursorToEnd();
                    setHighlightPos(0);
                }
                super.onFocusedChanged(focused);
            }
        };
        addButton(pastebinBox);

        WidgetButtonExtended pasteButton = new WidgetButtonExtended(guiLeft + 31, guiTop + 78, 120, 20, xlate("pneumaticcraft.gui.pastebin.button.upload"), b -> sendToPastebin());
        addButton(pasteButton);
        WidgetButtonExtended getButton = new WidgetButtonExtended(guiLeft + 31, guiTop + 167, 120, 20, xlate("pneumaticcraft.gui.pastebin.button.get"), b -> getFromPastebin());
        addButton(getButton);

        WidgetButtonExtended putInClipBoard = new WidgetButtonExtended(guiLeft + 8, guiTop + 78, 20, 20, StringTextComponent.EMPTY, b -> putToClipboard());
        putInClipBoard.setRenderedIcon(Textures.GUI_COPY_ICON_LOCATION);
        putInClipBoard.setTooltipText(xlate("pneumaticcraft.gui.pastebin.button.copyToClipboard"));
        addButton(putInClipBoard);
        WidgetButtonExtended retrieveFromClipboard = new WidgetButtonExtended(guiLeft + 8, guiTop + 167, 20, 20, StringTextComponent.EMPTY, b -> getFromClipboard());
        retrieveFromClipboard.setRenderedIcon(Textures.GUI_PASTE_ICON_LOCATION);
        retrieveFromClipboard.setTooltipText(xlate("pneumaticcraft.gui.pastebin.button.loadFromClipboard"));
        addButton(retrieveFromClipboard);

        prettyCB = new WidgetCheckBox(0, guiTop + 102, 0xFF404040, xlate("pneumaticcraft.gui.pastebin.pretty"),
                b -> shouldMerge = b.checked);
        prettyCB.x = guiLeft + (170 - prettyCB.getWidth());
        prettyCB.setTooltipKey("pneumaticcraft.gui.pastebin.pretty.tooltip");
        addButton(prettyCB);

        if (parentScreen instanceof GuiProgrammer) {
            WidgetCheckBox mergeCB = new WidgetCheckBox(0, guiTop + 155, 0xFF404040, xlate("pneumaticcraft.gui.pastebin.merge"),
                    b -> shouldMerge = b.checked);
            mergeCB.x = guiLeft + (170 - mergeCB.getWidth());
            mergeCB.setTooltipKey("pneumaticcraft.gui.pastebin.merge.tooltip");
            addButton(mergeCB);
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
            statusMessage = StringTextComponent.EMPTY;
            String pastebinText;
            switch (state) {
                case GETTING:
                    pastebinText = PastebinHandler.getHandler().contents;
                    if (pastebinText != null) {
                        readFromString(pastebinText);
                    } else {
                        statusMessage = xlate("pneumaticcraft.gui.pastebin.invalidPastebin");
                    }
                    break;
                case PUTTING:
                    if (PastebinHandler.getException() != null) {
                        statusMessage = new StringTextComponent(PastebinHandler.getException().getMessage());
                    } else {
                        pastebinText = PastebinHandler.getHandler().getLink;
                        if (pastebinText == null) pastebinText = "<ERROR>";
                        if (pastebinText.contains("pastebin.com")) {
                            pastebinBox.setValue(pastebinText);
                            setTempMessage(xlate("pneumaticcraft.gui.pastebin.uploadedToPastebin"));
                        } else {
                            statusMessage = new StringTextComponent(pastebinText);
                        }
                    }
                    break;
                case LOGIN:
                    if (!PastebinHandler.isLoggedIn()) {
                        statusMessage = xlate("pneumaticcraft.gui.pastebin.invalidLogin");
                    }
                    init();
            }
            state = EnumState.NONE;
        }
        if (messageTimer > 0 && --messageTimer <= 0) {
            lastMessage = StringTextComponent.EMPTY;
        }
    }

    private void setTempMessage(ITextComponent msg) {
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
            setTempMessage(xlate("pneumaticcraft.gui.pastebin.invalidFormattedPastebin").withStyle(TextFormatting.GOLD));
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
                subTag.putString("type", newType.getName());
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
    public void render(MatrixStack matrixStack, int x, int y, float partialTicks) {
        renderBackground(matrixStack);

        super.render(matrixStack, x, y, partialTicks);

        if (statusMessage != StringTextComponent.EMPTY) {
            drawString(matrixStack, font, statusMessage, guiLeft + 5, guiTop + 5, 0xFFFFFF00);
        } else if (lastMessage != StringTextComponent.EMPTY) {
            drawString(matrixStack, font, lastMessage, guiLeft + 5, guiTop + 5, 0xFF00FF00);
        }
    }

    @Override
    public void removed() {
        minecraft.keyboardHandler.setSendRepeatsToGui(false);
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
