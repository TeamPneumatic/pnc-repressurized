package pneumaticCraft.common.thirdparty.cofh;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.client.gui.widget.WidgetBase;
import cofh.api.tileentity.IEnergyInfo;

public class WidgetEnergy extends WidgetBase{

    public static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation("cofh:textures/gui/elements/Energy.png");
    public static final int DEFAULT_SCALE = 42;

    protected IEnergyInfo storage;

    public WidgetEnergy(int x, int y, IEnergyInfo storage){
        super(-1, x, y, 16, DEFAULT_SCALE);
        this.storage = storage;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick){

        int amount = getScaled();

        Minecraft.getMinecraft().getTextureManager().bindTexture(DEFAULT_TEXTURE);
        Gui.func_146110_a(x, y, 0, 0, getBounds().width, getBounds().height, 32, 64);
        Gui.func_146110_a(x, y + DEFAULT_SCALE - amount, 16, DEFAULT_SCALE - amount, getBounds().width, amount, 32, 64);
    }

    @Override
    public void addTooltip(int mouseX, int mouseY, List<String> list, boolean shiftPressed){
        list.add(storage.getInfoEnergyStored() + " / " + storage.getInfoMaxEnergyStored() + " RF");
    }

    protected int getScaled(){

        if(storage.getInfoMaxEnergyStored() <= 0) {
            return getBounds().height;
        }
        return storage.getInfoEnergyStored() * getBounds().height / storage.getInfoMaxEnergyStored();
    }

}
