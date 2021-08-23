package me.desht.pneumaticcraft.client.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.ContainerPressureChamberValve;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberValve;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiPressureChamber extends GuiPneumaticContainerBase<ContainerPressureChamberValve,TileEntityPressureChamberValve> {
    public GuiPressureChamber(ContainerPressureChamberValve container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        int sOut = te.multiBlockSize;
        int sIn = te.multiBlockSize - 2;
        addAnimatedStat(xlate("pneumaticcraft.gui.tab.status"), new ItemStack(ModBlocks.PRESSURE_CHAMBER_WALL.get()), 0xFFFFAA00, false)
                .setText(ImmutableList.of(
                        xlate("pneumaticcraft.gui.tab.pressureChamber.chamberSize").withStyle(TextFormatting.WHITE),
                        new StringTextComponent( sOut + "x" + sOut + "x" + sOut + " (outside)").withStyle(TextFormatting.BLACK),
                        new StringTextComponent( sIn + "x" + sIn + "x" + sIn + " (inside)").withStyle(TextFormatting.BLACK)
                ));
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int x, int y) {
        super.renderLabels(matrixStack, x, y);

        ITextComponent title = xlate("pneumaticcraft.gui.pressureChamberTitle", te.multiBlockSize + "x" + te.multiBlockSize + "x" + te.multiBlockSize);
        font.draw(matrixStack, title.getVisualOrderText(), (imageWidth - font.width(title)) / 2f, 6, 0x404040);
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
    protected void addWarnings(List<ITextComponent> curInfo) {
        super.addWarnings(curInfo);
        if (!te.isValidRecipeInChamber) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.pressure_chamber.no_recipe"));
        }
    }

    @Override
    protected void addProblems(List<ITextComponent> curInfo) {
        if (te.isValidRecipeInChamber && !te.isSufficientPressureInChamber) {
            if (te.recipePressure > 0F) {
                curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.pressure_chamber.not_enough_pressure"));
            } else {
                curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.pressure_chamber.too_much_pressure"));
            }
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.pressure_chamber.required_pressure", te.recipePressure));
        }
    }
}
