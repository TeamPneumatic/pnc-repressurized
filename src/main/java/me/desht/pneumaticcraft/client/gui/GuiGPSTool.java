package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketChangeGPSToolCoordinate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

public class GuiGPSTool extends GuiPneumaticScreenBase {

    private final WidgetTextFieldNumber[] textFields = new WidgetTextFieldNumber[3];
    private WidgetTextField variableField;
    private static final int TEXTFIELD_WIDTH = 40;
    protected final Hand hand;
    private final BlockPos oldGPSLoc;
    private String oldVarName;
    private final int metadata;

    public GuiGPSTool(Hand hand, BlockPos gpsLoc, String oldVarName, int metadata) {
        super(makeTitle(metadata));

        this.hand = hand;
        this.oldGPSLoc = gpsLoc;
        this.oldVarName = oldVarName;
        this.metadata = metadata;
    }

    private GuiGPSTool(Hand hand, BlockPos gpsLoc, String oldVarName) {
        this(hand, gpsLoc, oldVarName, -1);
    }

    private static ITextComponent makeTitle(int index) {
        ITextComponent text = new ItemStack(ModItems.GPS_TOOL.get()).getDisplayName();
        return index < 0 ? text : text.appendText(" (P" + (index + 1) + ")");
    }

    public static void showGUI(ItemStack stack, Hand handIn, BlockPos pos) {
        Minecraft.getInstance().displayGuiScreen(new GuiGPSTool(handIn, pos != null ? pos : BlockPos.ZERO, ItemGPSTool.getVariable(stack)));
    }

    @Override
    public void init() {
        super.init();
        int[] oldText = new int[3];
        if (textFields[0] == null) {
            oldText[0] = oldGPSLoc.getX();
            oldText[1] = oldGPSLoc.getY();
            oldText[2] = oldGPSLoc.getZ();
        } else {
            for (int i = 0; i < 3; i++)
                oldText[i] = textFields[i].getValue();
        }
        int xMiddle = width / 2;
        int yMiddle = height / 2;
        for (int i = 0; i < 3; i++) {
            textFields[i] = new WidgetTextFieldNumber(font, xMiddle - TEXTFIELD_WIDTH / 2, yMiddle - 27 + i * 22, TEXTFIELD_WIDTH, font.FONT_HEIGHT).setValue(oldText[i]);
            if (i == 1) { // Y
                textFields[i].minValue = 0;
                textFields[i].maxValue = 255;
            }
            addButton(textFields[i]);
        }

        for (int i = 0; i < 3; i++) {
            final int idx = i;
            addButton(new Button(xMiddle - 49 - TEXTFIELD_WIDTH / 2, yMiddle - 32 + i * 22, 22, 20,
                    "-10", b -> updateTextField(idx, -10)));
            addButton(new Button(xMiddle - 25 - TEXTFIELD_WIDTH / 2, yMiddle - 32 + i * 22, 22, 20,
                    "-1", b -> updateTextField(idx, -1)));
            addButton(new Button(xMiddle + 3 + TEXTFIELD_WIDTH / 2, yMiddle - 32 + i * 22, 22, 20,
                    "+1", b -> updateTextField(idx, 1)));
            addButton(new Button(xMiddle + 27 + TEXTFIELD_WIDTH / 2, yMiddle - 32 + i * 22, 22, 20,
                    "+10", b -> updateTextField(idx, 10)));
        }

        if (variableField != null) oldVarName = variableField.getText();
        variableField = new WidgetTextField(font, xMiddle - 50, yMiddle + 60, 100, font.FONT_HEIGHT);
        variableField.setText(oldVarName);
        addButton(variableField);

        String var = I18n.format("gui.progWidget.coordinate.variable") + " #";
        addButton(new WidgetLabel(xMiddle - 52 - font.getStringWidth(var), yMiddle + 61, var, 0xc0c0c0));
    }

    private void updateTextField(int idx, int amount) {
        textFields[idx].setValue(textFields[idx].getValue() + amount);
    }

    @Override
    public void render(int par1, int par2, float par3) {
        renderBackground();
        super.render(par1, par2, par3);

        int xMiddle = width / 2;
        int yMiddle = height / 2;
        int stringX = xMiddle - 60 - TEXTFIELD_WIDTH / 2;
        drawCenteredString(font, getTitle().getFormattedText(), xMiddle, yMiddle - 44, 0xFFFFFFFF);
        drawString(font, "X:", stringX, yMiddle - 22 - font.FONT_HEIGHT / 2, 0xFFFFFFFF);
        drawString(font, "Y:", stringX, yMiddle - font.FONT_HEIGHT / 2, 0xFFFFFFFF);
        drawString(font, "Z:", stringX, yMiddle + 22 - font.FONT_HEIGHT / 2, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        BlockPos newPos = new BlockPos(textFields[0].getValue(), textFields[1].getValue(), textFields[2].getValue());
        NetworkHandler.sendToServer(new PacketChangeGPSToolCoordinate(
                newPos.equals(oldGPSLoc) ? new BlockPos(-1, -1, -1) : newPos,
                hand, variableField.getText(), metadata)
        );
        super.onClose();
    }

    @Override
    protected ResourceLocation getTexture() {
        return null;
    }
}
