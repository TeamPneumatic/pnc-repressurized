package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.inventory.ContainerVacuumTrap;
import me.desht.pneumaticcraft.common.tileentity.TileEntityVacuumTrap;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class GuiVacuumTrap extends GuiPneumaticContainerBase<ContainerVacuumTrap, TileEntityVacuumTrap> {
    public GuiVacuumTrap(ContainerVacuumTrap container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_VACUUM_TRAP;
    }

    @Override
    protected PointXY getGaugeLocation() {
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        return new PointXY(xStart + (int)(xSize * 0.82), yStart + ySize / 4 + 4);
    }

    @Override
    protected void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);
        if (te.problem == TileEntityVacuumTrap.Problems.NO_CORE || te.problem == TileEntityVacuumTrap.Problems.CORE_FULL) {
            curInfo.add(I18n.format(te.problem.getTranslationKey()));
        }
    }

    @Override
    protected void addWarnings(List<String> curInfo) {
        super.addWarnings(curInfo);
        if (te.problem == TileEntityVacuumTrap.Problems.TRAP_CLOSED) {
            curInfo.add(I18n.format(te.problem.getTranslationKey()));
        }
    }
}
