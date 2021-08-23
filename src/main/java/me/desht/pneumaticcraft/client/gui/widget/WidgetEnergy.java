package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.List;

public class WidgetEnergy extends Widget implements ITooltipProvider {
    private static final int DEFAULT_SCALE = 42;

    private final IEnergyStorage storage;

    public WidgetEnergy(int x, int y, IEnergyStorage storage) {
        super(x, y, 16, DEFAULT_SCALE, StringTextComponent.EMPTY);
        this.storage = storage;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick){
        int amount = getScaled();

        Minecraft.getInstance().getTextureManager().bind(Textures.WIDGET_ENERGY);
        AbstractGui.blit(matrixStack, x + 1, y, 1, 0, width - 2, height, 32, 64);
        AbstractGui.blit(matrixStack, x + 1, y + DEFAULT_SCALE - amount, 17, DEFAULT_SCALE - amount, width - 2, amount, 32, 64);
    }

    @Override
    public void addTooltip(double mouseX, double mouseY, List<ITextComponent> list, boolean shiftPressed){
        list.add(new StringTextComponent(storage.getEnergyStored() + " / " + storage.getMaxEnergyStored() + " FE"));
    }

    private int getScaled(){
        if (storage.getMaxEnergyStored() <= 0) {
            return height;
        }
        return storage.getEnergyStored() * height / storage.getMaxEnergyStored();
    }
}
