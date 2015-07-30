package pneumaticCraft.client.gui.widget;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.MathHelper;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.api.IHeatExchangerLogic;
import pneumaticCraft.lib.Textures;

public class WidgetTemperature extends WidgetBase{

    private int[] scales;
    private final IHeatExchangerLogic logic;
    private final int minTemp, maxTemp;

    public WidgetTemperature(int id, int x, int y, int minTemp, int maxTemp, IHeatExchangerLogic logic, int... scales){
        super(id, x, y, 13, 50);
        this.scales = scales;
        this.logic = logic;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp - 273;
    }

    public void setScales(int... scales){
        this.scales = scales;
    }

    public int[] getScales(){
        return scales;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick){
        GL11.glDisable(GL11.GL_LIGHTING);
        Minecraft.getMinecraft().getTextureManager().bindTexture(Textures.WIDGET_TEMPERATURE);
        GL11.glColor4d(1, 1, 1, 1);
        Gui.func_146110_a(x + 6, y, 6, 0, 7, 50, 18, 50);

        int barLength = ((int)logic.getTemperature() - minTemp) * 48 / maxTemp;
        barLength = MathHelper.clamp_int(barLength, 0, 48);
        Gui.func_146110_a(x + 7, y + 1 + 48 - barLength, 13, 48 - barLength, 5, barLength, 18, 50);

        for(int scale : scales) {
            int scaleY = 48 - (scale - minTemp) * 48 / maxTemp;
            Gui.func_146110_a(x, y - 1 + scaleY, 0, 0, 6, 5, 18, 50);
        }
    }

    @Override
    public void addTooltip(int mouseX, int mouseY, List<String> curTip, boolean shift){
        curTip.add("Temperature: " + ((int)logic.getTemperature() - 273) + "C");
    }
}
