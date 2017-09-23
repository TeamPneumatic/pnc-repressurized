package me.desht.pneumaticcraft.client.gui.tubemodule;

import me.desht.pneumaticcraft.client.gui.GuiPneumaticScreenBase;
import me.desht.pneumaticcraft.common.block.BlockPressureTube;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public abstract class GuiTubeModule extends GuiPneumaticScreenBase {
    protected final TubeModule module;

    public GuiTubeModule(EntityPlayer player, int x, int y, int z) {
        this(BlockPressureTube.getLookedModule(player.world, new BlockPos(x, y, z), player));
    }

    public GuiTubeModule(TubeModule module) {
        this.module = module;
        xSize = 183;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
