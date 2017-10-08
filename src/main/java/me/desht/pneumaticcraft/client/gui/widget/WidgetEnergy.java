package me.desht.pneumaticcraft.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class WidgetEnergy extends WidgetBase {
    private static final ResourceLocation DEFAULT_TEXTURE = RL("textures/gui/widget/energy.png");
    private static final int DEFAULT_SCALE = 42;

    private final IEnergyStorage storage;

    public WidgetEnergy(int x, int y, IEnergyStorage storage) {
        super(-1, x, y, 16, DEFAULT_SCALE);
        this.storage = storage;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick){
        int amount = getScaled();

        Minecraft.getMinecraft().getTextureManager().bindTexture(DEFAULT_TEXTURE);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, getBounds().width, getBounds().height, 32, 64);
        Gui.drawModalRectWithCustomSizedTexture(x, y + DEFAULT_SCALE - amount, 16, DEFAULT_SCALE - amount, getBounds().width, amount, 32, 64);
    }

    @Override
    public void addTooltip(int mouseX, int mouseY, List<String> list, boolean shiftPressed){
        list.add(storage.getEnergyStored() + " / " + storage.getMaxEnergyStored() + " RF");
    }

    private int getScaled(){
        if(storage.getMaxEnergyStored() <= 0) {
            return getBounds().height;
        }
        return storage.getEnergyStored() * getBounds().height / storage.getMaxEnergyStored();
    }
}
