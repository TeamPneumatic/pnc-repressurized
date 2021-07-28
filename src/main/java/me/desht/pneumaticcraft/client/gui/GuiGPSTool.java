package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketChangeGPSToolCoordinate;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class GuiGPSTool extends GuiPneumaticScreenBase {
    private static final int TEXTFIELD_WIDTH = 60;

    protected final WidgetTextFieldNumber[] textFields = new WidgetTextFieldNumber[3];
    protected WidgetTextField variableField;
    protected WidgetButtonExtended varTypeButton;
    protected final Hand hand;
    private final BlockPos oldGPSLoc;
    private String oldVarName;
    protected boolean playerGlobal;

    protected GuiGPSTool(ITextComponent title, Hand hand, BlockPos gpsLoc, String oldVarName) {
        super(title);

        this.hand = hand;
        this.oldGPSLoc = gpsLoc;
        this.oldVarName = oldVarName;
    }

    public static void showGUI(ItemStack stack, Hand handIn, BlockPos pos) {
        Minecraft.getInstance().displayGuiScreen(
                new GuiGPSTool(stack.getDisplayName(), handIn, pos != null ? pos : BlockPos.ZERO, ItemGPSTool.getVariable(stack))
        );
    }

    @Override
    public void init() {
        super.init();

        xSize = width;

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
            int min = i == 1 ? PneumaticCraftUtils.getMinHeight(ClientUtils.getClientWorld()) : Integer.MIN_VALUE;
            int max = i == 1 ? ClientUtils.getClientWorld().getHeight() : Integer.MAX_VALUE;
            textFields[i] = new WidgetTextFieldNumber(font, xMiddle - TEXTFIELD_WIDTH / 2, yMiddle - 15 + i * 22, TEXTFIELD_WIDTH, font.FONT_HEIGHT)
                    .setValue(oldText[i])
                    .setRange(min, max)
                    .setAdjustments(1, 10);
            addButton(textFields[i]);
        }

        for (int i = 0; i < 3; i++) {
            final int idx = i;
            addButton(new Button(xMiddle - 49 - TEXTFIELD_WIDTH / 2, yMiddle - 20 + i * 22, 22, 20,
                    new StringTextComponent("-10"), b -> updateTextField(idx, -10)));
            addButton(new Button(xMiddle - 25 - TEXTFIELD_WIDTH / 2, yMiddle - 20 + i * 22, 22, 20,
                    new StringTextComponent("-1"), b -> updateTextField(idx, -1)));
            addButton(new Button(xMiddle + 3 + TEXTFIELD_WIDTH / 2, yMiddle - 20 + i * 22, 22, 20,
                    new StringTextComponent("+1"), b -> updateTextField(idx, 1)));
            addButton(new Button(xMiddle + 27 + TEXTFIELD_WIDTH / 2, yMiddle - 20 + i * 22, 22, 20,
                    new StringTextComponent("+10"), b -> updateTextField(idx, 10)));
        }

        if (variableField != null) oldVarName = variableField.getText();
        variableField = new WidgetTextField(font, xMiddle - 50, yMiddle + 60, 100, font.FONT_HEIGHT + 1);
        playerGlobal = !oldVarName.startsWith("%");
        oldVarName = GlobalVariableHelper.stripVarPrefix(oldVarName);
        variableField.setText(oldVarName);
        addButton(variableField);

        varTypeButton = new WidgetButtonExtended(variableField.x - 13, yMiddle + 58, 12, 14, playerGlobal ? "#" : "%",
                b -> toggleVarType())
                .setTooltipKey("pneumaticcraft.gui.remote.varType.tooltip");
        addButton(varTypeButton);
    }

    protected void toggleVarType() {
        playerGlobal = !playerGlobal;
        varTypeButton.setMessage(new StringTextComponent(GlobalVariableHelper.getVarPrefix(playerGlobal)));
    }

    private void updateTextField(int idx, int amount) {
        textFields[idx].setValue(textFields[idx].getValue() + amount);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        int xMiddle = width / 2;
        int yMiddle = height / 2;
        int stringX = xMiddle - 60 - TEXTFIELD_WIDTH / 2;
        drawCenteredString(matrixStack, font, getTitle(), xMiddle, yMiddle - 58, 0xFFFFFFFF);
        drawString(matrixStack, font, "X:", stringX, yMiddle - 10 - font.FONT_HEIGHT / 2, 0xFFFFFFFF);
        drawString(matrixStack, font, "Y:", stringX, yMiddle + 4 + font.FONT_HEIGHT / 2, 0xFFFFFFFF);
        drawString(matrixStack, font, "Z:", stringX, yMiddle + 34 - font.FONT_HEIGHT / 2, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        syncToServer();
        super.onClose();
    }

    protected void syncToServer() {
        BlockPos newPos = new BlockPos(textFields[0].getValue(), textFields[1].getValue(), textFields[2].getValue());
        String varName = GlobalVariableHelper.getPrefixedVar(variableField.getText(), playerGlobal);
        NetworkHandler.sendToServer(new PacketChangeGPSToolCoordinate(
                /*newPos.equals(oldGPSLoc) ? new BlockPos(-1, -1, -1) :*/ newPos,
                hand, varName, getIndex())
        );

    }

    protected int getIndex() {
        return 0;
    }

    @Override
    protected ResourceLocation getTexture() {
        return null;
    }
}
