package me.desht.pneumaticcraft.client.gui.tubemodule;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.TubeModuleClientRegistry;
import me.desht.pneumaticcraft.client.gui.GuiPneumaticScreenBase;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public abstract class GuiTubeModule<M extends TubeModule> extends GuiPneumaticScreenBase {
    protected final M module;

    GuiTubeModule(M module) {
        super(new ItemStack(module.getItem()).getDisplayName());

        this.module = module;
        this.xSize = 183;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static void openGuiForModule(TubeModule module) {
        Minecraft.getInstance().displayGuiScreen(TubeModuleClientRegistry.createGUI(module));
    }
}
