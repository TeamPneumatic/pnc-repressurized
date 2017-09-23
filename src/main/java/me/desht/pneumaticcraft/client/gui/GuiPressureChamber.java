package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.inventory.ContainerPressureChamber;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberValve;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiPressureChamber extends GuiPneumaticContainerBase<TileEntityPressureChamberValve> {

    private GuiAnimatedStat statusStat;

    public GuiPressureChamber(InventoryPlayer player, TileEntityPressureChamberValve te) {

        super(new ContainerPressureChamber(player, te), te, Textures.GUI_4UPGRADE_SLOTS);
    }

    @Override
    public void initGui() {
        super.initGui();
        statusStat = addAnimatedStat("Pressure Chamber Status", new ItemStack(Blockss.PRESSURE_CHAMBER_WALL), 0xFFFFAA00, false);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
        String containerName = I18n.format("gui.pressureChamberTitle", te.multiBlockSize + "x" + te.multiBlockSize + "x" + te.multiBlockSize);
        fontRenderer.drawString(containerName, xSize / 2 - fontRenderer.getStringWidth(containerName) / 2, 6, 4210752);
        fontRenderer.drawString("Upgr.", 53, 19, 4210752);
    }

    @Override
    protected Point getInvNameOffset() {
        return null;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        statusStat.setText(getStatusText());
    }

    private List<String> getStatusText() {
        List<String> text = new ArrayList<String>();

        text.add("\u00a77Chamber Size:");
        text.add("\u00a70" + te.multiBlockSize + "x" + te.multiBlockSize + "x" + te.multiBlockSize + " (outside)");
        text.add("\u00a70" + (te.multiBlockSize - 2) + "x" + (te.multiBlockSize - 2) + "x" + (te.multiBlockSize - 2) + " (inside)");
        text.add("\u00a77Recipe list:");
        if (PneumaticCraftRepressurized.isNEIInstalled) {
            text.add("\u00a70Click on the Pressure gauge to view all the recipes of this machine. Powered by ChickenBones' NEI.");
        } else {
            text.add("\u00a70Install NEI (an other (client) mod by ChickenBones) to be able to see all the recipes of this machine.");
        }
        return text;
    }

    @Override
    protected void addProblems(List<String> textList) {
        if (!te.isValidRecipeInChamber) {
            textList.add("\u00a77No (valid) items in the chamber");
            textList.add("\u00a70Insert (valid) items");
            textList.add("\u00a70in the chamber");
        } else if (!te.isSufficientPressureInChamber) {
            if (te.recipePressure > 0F) {
                textList.add("\u00a77Not enough pressure");
                textList.add("\u00a70Add air to the input");
            } else {
                textList.add("\u00a77Too much pressure");
                textList.add("\u00a70Remove air from the input");
            }
            textList.add("\u00a70Pressure required: " + te.recipePressure + " bar");
        } else if (!te.areEntitiesDoneMoving) {
            textList.add("\u00a77Items are too far away from eachother");
            textList.add("\u00a70Wait until the items are blown to the middle.");
        }
    }
}
