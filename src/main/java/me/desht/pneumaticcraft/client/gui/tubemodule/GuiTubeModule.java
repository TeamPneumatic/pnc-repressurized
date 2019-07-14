package me.desht.pneumaticcraft.client.gui.tubemodule;

import me.desht.pneumaticcraft.client.gui.GuiPneumaticScreenBase;
import me.desht.pneumaticcraft.common.block.BlockPressureTube;
import me.desht.pneumaticcraft.common.block.tubes.ModuleRegistrator;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public abstract class GuiTubeModule extends GuiPneumaticScreenBase {
    protected final TubeModule module;

    GuiTubeModule(BlockPos modulePos) {
        this(BlockPressureTube.getFocusedModule(Minecraft.getInstance().world, modulePos, Minecraft.getInstance().player));
    }

    GuiTubeModule(TubeModule module) {
        super(new ItemStack(ModuleRegistrator.getModuleItem(module.getType())).getDisplayName());

        this.module = module;
        this.xSize = 183;
    }

    public static void openGuiForType(String moduleType, BlockPos modulePos) {
        Minecraft mc = Minecraft.getInstance();
        switch (moduleType) {
            case Names.MODULE_AIR_GRATE:
                mc.displayGuiScreen(new GuiAirGrateModule(modulePos));
            case Names.MODULE_REDSTONE:
                mc.displayGuiScreen(new GuiRedstoneModule(modulePos));
                break;
            case Names.MODULE_REGULATOR:
            case Names.MODULE_GAUGE:
            case Names.MODULE_SAFETY_VALVE:
                mc.displayGuiScreen(new GuiPressureModule(modulePos));
                break;
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        renderBackground();
        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
