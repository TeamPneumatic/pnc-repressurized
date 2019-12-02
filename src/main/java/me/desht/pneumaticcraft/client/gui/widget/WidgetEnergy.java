package me.desht.pneumaticcraft.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class WidgetEnergy extends Widget implements ITooltipSupplier {
    private static final ResourceLocation DEFAULT_TEXTURE = RL("textures/gui/widget/energy.png");
    private static final int DEFAULT_SCALE = 42;

    private final IEnergyStorage storage;

    public WidgetEnergy(int x, int y, IEnergyStorage storage) {
        super(x, y, 16, DEFAULT_SCALE, "");
        this.storage = storage;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTick){
        int amount = getScaled();

        Minecraft.getInstance().getTextureManager().bindTexture(DEFAULT_TEXTURE);
        AbstractGui.blit(x, y, 0, 0, width, height, 32, 64);
        AbstractGui.blit(x, y + DEFAULT_SCALE - amount, 16, DEFAULT_SCALE - amount, width, amount, 32, 64);
    }

    @Override
    public void addTooltip(int mouseX, int mouseY, List<String> list, boolean shiftPressed){
        list.add(storage.getEnergyStored() + " / " + storage.getMaxEnergyStored() + " RF");
    }

    private int getScaled(){
        if (storage.getMaxEnergyStored() <= 0) {
            return height;
        }
        return storage.getEnergyStored() * height / storage.getMaxEnergyStored();
    }
}
