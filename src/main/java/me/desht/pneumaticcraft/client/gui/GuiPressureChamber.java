package me.desht.pneumaticcraft.client.gui;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.ContainerPressureChamberValve;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberValve;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class GuiPressureChamber extends GuiPneumaticContainerBase<ContainerPressureChamberValve,TileEntityPressureChamberValve> {
    public GuiPressureChamber(ContainerPressureChamberValve container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        addAnimatedStat("Pressure Chamber Status", new ItemStack(ModBlocks.PRESSURE_CHAMBER_WALL.get()), 0xFFFFAA00, false)
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
        font.drawString(containerName, xSize / 2f - font.getStringWidth(containerName) / 2f, 6, 4210752);
        font.drawString("Upgr.", 53, 19, 4210752);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_4UPGRADE_SLOTS;
    }

    @Override
    protected PointXY getInvNameOffset() {
        return null;
    }

    @Override
    protected void addWarnings(List<String> curInfo) {
        super.addWarnings(curInfo);
        if (!te.isValidRecipeInChamber) {
            curInfo.add(I18n.format("gui.tab.problems.pressure_chamber.no_recipe"));
        }
    }

    @Override
    protected void addProblems(List<String> curInfo) {
        if (te.isValidRecipeInChamber && !te.isSufficientPressureInChamber) {
            if (te.recipePressure > 0F) {
                curInfo.add(I18n.format("gui.tab.problems.pressure_chamber.not_enough_pressure"));
            } else {
                curInfo.add(I18n.format("gui.tab.problems.pressure_chamber.too_much_pressure"));
            }
            curInfo.add(I18n.format("gui.tab.problems.pressure_chamber.required_pressure", te.recipePressure));
        }
    }
}
