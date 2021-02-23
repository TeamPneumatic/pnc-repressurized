package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetRangeToggleButton;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.inventory.ContainerVacuumTrap;
import me.desht.pneumaticcraft.common.tileentity.TileEntityVacuumTrap;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class GuiVacuumTrap extends GuiPneumaticContainerBase<ContainerVacuumTrap, TileEntityVacuumTrap> {
    WidgetButtonExtended rangeButton;

    public GuiVacuumTrap(ContainerVacuumTrap container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        addButton(new WidgetTank(guiLeft + 98, guiTop + 14, te.getFluidTank()));

        addButton(rangeButton = new WidgetRangeToggleButton(guiLeft + 152, guiTop + 66, te));
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_VACUUM_TRAP;
    }

    @Override
    protected PointXY getGaugeLocation() {
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        return new PointXY(xStart + (int)(xSize * 0.82), yStart + ySize / 4 - 2);
    }

    @Override
    protected void addProblems(List<ITextComponent> curInfo) {
        super.addProblems(curInfo);
        if (te.problem == TileEntityVacuumTrap.Problems.NO_CORE || te.problem == TileEntityVacuumTrap.Problems.CORE_FULL) {
            curInfo.addAll(GuiUtils.xlateAndSplit(te.problem.getTranslationKey()));
        }
    }

    @Override
    protected void addWarnings(List<ITextComponent> curInfo) {
        super.addWarnings(curInfo);
        if (te.problem == TileEntityVacuumTrap.Problems.TRAP_CLOSED) {
            curInfo.addAll(GuiUtils.xlateAndSplit(te.problem.getTranslationKey()));
        }
        if (te.getFluidTank().getFluidAmount() < TileEntityVacuumTrap.MEMORY_ESSENCE_AMOUNT) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.vacuum_trap.no_memory_essence"));
        }
    }
}
