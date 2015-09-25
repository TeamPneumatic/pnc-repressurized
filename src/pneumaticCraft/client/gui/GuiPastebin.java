package pneumaticCraft.client.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;

import pneumaticCraft.client.gui.widget.IGuiWidget;
import pneumaticCraft.client.gui.widget.WidgetTextField;
import pneumaticCraft.common.util.JsonToNBTConverter;
import pneumaticCraft.common.util.NBTToJsonConverter;
import pneumaticCraft.common.util.PastebinHandler;
import pneumaticCraft.lib.Textures;

public class GuiPastebin extends GuiPneumaticScreenBase{

    private WidgetTextField usernameBox, passwordBox;
    private WidgetTextField pastebinBox;
    private final String pastingString;
    public NBTTagCompound outputTag;
    private final GuiScreen parentScreen;
    public String errorMessage;
    private EnumState state = EnumState.NONE;

    private enum EnumState{
        NONE, GETTING, PUTTING, LOGIN, LOGOUT;
    }

    public GuiPastebin(GuiScreen parentScreen, String pastingString){
        xSize = 183;
        ySize = 202;
        this.pastingString = pastingString;
        this.parentScreen = parentScreen;
        Keyboard.enableRepeatEvents(true);
    }

    public GuiPastebin(GuiScreen parentScreen, NBTTagCompound tag){
        this(parentScreen, new NBTToJsonConverter(tag).convert());
    }

    @Override
    public void initGui(){
        super.initGui();
        if(!PastebinHandler.isLoggedIn()) {
            usernameBox = new WidgetTextField(fontRendererObj, guiLeft + 10, guiTop + 30, 80, 10);
            addWidget(usernameBox);

            passwordBox = new WidgetTextField(fontRendererObj, guiLeft + 10, guiTop + 56, 80, 10).setAsPasswordBox();
            addWidget(passwordBox);

            GuiButtonSpecial loginButton = new GuiButtonSpecial(0, guiLeft + 100, guiTop + 30, 60, 20, I18n.format("gui.pastebin.button.login"));
            addWidget(loginButton);

            addLabel(I18n.format("gui.pastebin.username"), guiLeft + 10, guiTop + 20);
            addLabel(I18n.format("gui.pastebin.password"), guiLeft + 10, guiTop + 46);

        } else {
            GuiButtonSpecial logoutButton = new GuiButtonSpecial(3, guiLeft + 60, guiTop + 30, 60, 20, I18n.format("gui.pastebin.button.logout"));
            addWidget(logoutButton);
        }

        pastebinBox = new WidgetTextField(fontRendererObj, guiLeft + 10, guiTop + 130, 160, 10){
            @Override
            public void onMouseClicked(int mouseX, int mouseY, int button){
                boolean wasFocused = isFocused();
                super.onMouseClicked(mouseX, mouseY, button);
                if(isFocused()) {
                    if(!wasFocused) { //setText("");
                        setCursorPositionEnd();
                        setSelectionPos(0);
                    }
                }
            }

        };
        addWidget(pastebinBox);

        GuiButtonSpecial pasteButton = new GuiButtonSpecial(1, guiLeft + 31, guiTop + 78, 120, 20, I18n.format("gui.pastebin.button.upload"));
        addWidget(pasteButton);
        GuiButtonSpecial getButton = new GuiButtonSpecial(2, guiLeft + 31, guiTop + 167, 120, 20, I18n.format("gui.pastebin.button.get"));
        addWidget(getButton);

        GuiButtonSpecial putInClipBoard = new GuiButtonSpecial(4, guiLeft + 8, guiTop + 78, 20, 20, "");
        putInClipBoard.setRenderedIcon(Textures.GUI_COPY_ICON_LOCATION);
        putInClipBoard.setTooltipText(I18n.format("gui.pastebin.button.copyToClipboard"));
        addWidget(putInClipBoard);
        GuiButtonSpecial retrieveFromClipboard = new GuiButtonSpecial(5, guiLeft + 8, guiTop + 167, 20, 20, "");
        retrieveFromClipboard.setRenderedIcon(Textures.GUI_PASTE_ICON_LOCATION);
        retrieveFromClipboard.setTooltipText(I18n.format("gui.pastebin.button.loadFromClipboard"));
        addWidget(retrieveFromClipboard);

        addLabel(I18n.format("gui.pastebin.pastebinLink"), guiLeft + 10, guiTop + 120);

    }

    @Override
    public void updateScreen(){
        super.updateScreen();
        if(state == EnumState.LOGOUT) {
            state = EnumState.NONE;
            initGui();
        }
        if(state != EnumState.NONE && PastebinHandler.isDone()) {
            errorMessage = "";
            String pastebinText;
            switch(state){
                case GETTING:
                    pastebinText = PastebinHandler.getHandler().contents;
                    if(pastebinText != null) {
                        readFromString(pastebinText);
                    } else {
                        errorMessage = I18n.format("gui.pastebin.invalidPastebin");
                    }
                    break;
                case PUTTING:
                    if(PastebinHandler.getException() != null) {
                        errorMessage = PastebinHandler.getException().getMessage();
                    } else {
                        pastebinText = PastebinHandler.getHandler().getLink;
                        if(pastebinText == null) pastebinText = "<ERROR>";
                        if(pastebinText.contains("pastebin.com")) {
                            pastebinBox.setText(pastebinText);
                        } else {
                            errorMessage = pastebinText;
                        }
                    }
                    break;
                case LOGIN:
                    if(!PastebinHandler.isLoggedIn()) {
                        errorMessage = I18n.format("gui.pastebin.invalidLogin");
                    }
                    initGui();
            }
            state = EnumState.NONE;
        }
    }

    private void readFromString(String string){
        try {
            outputTag = new JsonToNBTConverter(string).convert();
        } catch(Exception e) {
            e.printStackTrace();
            errorMessage = I18n.format("gui.pastebin.invalidFormattedPastebin");
        }
    }

    @Override
    public void drawScreen(int x, int y, float partialTicks){
        super.drawScreen(x, y, partialTicks);
        if(errorMessage != null) fontRendererObj.drawString(errorMessage, guiLeft + 5, guiTop + 5, 0xFFFF0000);
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    @Override
    protected void keyTyped(char par1, int par2){
        if(par2 == 1) {
            Keyboard.enableRepeatEvents(false);
            mc.displayGuiScreen(parentScreen);
            onGuiClosed();
        } else {
            super.keyTyped(par1, par2);
        }
    }

    @Override
    public void actionPerformed(IGuiWidget widget){
        super.actionPerformed(widget);
        errorMessage = "";
        if(widget.getID() == 0) {
            PastebinHandler.login(usernameBox.getText(), passwordBox.getText());
            state = EnumState.LOGIN;
            errorMessage = I18n.format("gui.pastebin.loggingIn");
        } else if(widget.getID() == 1) {
            PastebinHandler.put(pastingString);
            state = EnumState.PUTTING;
            errorMessage = I18n.format("gui.pastebin.uploadingToPastebin");
        } else if(widget.getID() == 2) {
            PastebinHandler.get(pastebinBox.getText());
            state = EnumState.GETTING;
            errorMessage = I18n.format("gui.pastebin.retrievingFromPastebin");
        } else if(widget.getID() == 3) {
            PastebinHandler.logout();
            state = EnumState.LOGOUT;
        } else if(widget.getID() == 4) {
            GuiScreen.setClipboardString(pastingString);
            errorMessage = I18n.format("gui.pastebin.clipboardSetToContents");
        } else if(widget.getID() == 5) {
            errorMessage = I18n.format("gui.pastebin.retrievedFromClipboard");
            readFromString(GuiScreen.getClipboardString());
        }
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.GUI_PASTEBIN;
    }

    @Override
    public boolean doesGuiPauseGame(){
        return false;
    }
}
