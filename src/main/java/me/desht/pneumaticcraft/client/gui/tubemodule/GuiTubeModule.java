package me.desht.pneumaticcraft.client.gui.tubemodule;

import me.desht.pneumaticcraft.client.TubeModuleClientRegistry;
import me.desht.pneumaticcraft.client.gui.GuiPneumaticScreenBase;
import me.desht.pneumaticcraft.common.block.BlockPressureTube;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public abstract class GuiTubeModule extends GuiPneumaticScreenBase {
    protected final TubeModule module;

    GuiTubeModule(BlockPos modulePos) {
        this(BlockPressureTube.getFocusedModule(Minecraft.getInstance().world, modulePos, Minecraft.getInstance().player));
    }

    GuiTubeModule(TubeModule module) {
        super(new ItemStack(module.getItem()).getDisplayName());

        this.module = module;
        this.xSize = 183;
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

    public static void openGuiForType(ResourceLocation moduleType, BlockPos modulePos) {
        Minecraft.getInstance().displayGuiScreen(TubeModuleClientRegistry.createGUI(moduleType, modulePos));
    }
}
