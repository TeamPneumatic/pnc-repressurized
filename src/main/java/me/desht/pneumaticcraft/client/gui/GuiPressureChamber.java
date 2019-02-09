package me.desht.pneumaticcraft.client.gui;

import com.google.common.collect.ImmutableList;
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
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiPressureChamber extends GuiPneumaticContainerBase<TileEntityPressureChamberValve> {
    private List<String> statusText;

    public GuiPressureChamber(InventoryPlayer player, TileEntityPressureChamberValve te) {

        super(new ContainerPressureChamber(player, te), te, Textures.GUI_4UPGRADE_SLOTS);
    }

    @Override
    public void initGui() {
        super.initGui();

        addAnimatedStat("Pressure Chamber Status", new ItemStack(Blockss.PRESSURE_CHAMBER_WALL), 0xFFFFAA00, false)
                .setText(ImmutableList.of(
                        "\u00a7fChamber Size:",
                        "\u00a70" + te.multiBlockSize + "x" + te.multiBlockSize + "x" + te.multiBlockSize + " (outside)",
                        "\u00a70" + (te.multiBlockSize - 2) + "x" + (te.multiBlockSize - 2) + "x" + (te.multiBlockSize - 2) + " (inside)"
                ));
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
    protected void addWarnings(List<String> curInfo) {
        super.addWarnings(curInfo);
        if (!te.isValidRecipeInChamber) {
            curInfo.add("\u00a7fNo (valid) items in the chamber");
            curInfo.add("\u00a70Insert valid items in");
            curInfo.add("\u00a70the chamber to be compressed.");
        }
    }

    @Override
    protected void addProblems(List<String> curInfo) {
        if (te.isValidRecipeInChamber && !te.isSufficientPressureInChamber) {
            if (te.recipePressure > 0F) {
                curInfo.add("\u00a7fNot enough pressure");
                curInfo.add("\u00a70Add air to the input");
            } else {
                curInfo.add("\u00a7fToo much pressure");
                curInfo.add("\u00a70Remove air from the input");
            }
            curInfo.add("\u00a70Pressure required: " + te.recipePressure + " bar");
        }
    }
}
