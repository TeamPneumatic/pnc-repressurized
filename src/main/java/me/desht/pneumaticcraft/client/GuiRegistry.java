package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.api.client.IClientRegistry;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.assemblymachine.IAssemblyRenderOverriding;
import me.desht.pneumaticcraft.client.gui.GuiUtils;
import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.HashMap;

public class GuiRegistry implements IClientRegistry {

    private static final GuiRegistry INSTANCE = new GuiRegistry();
    public final HashMap<Integer, IAssemblyRenderOverriding> renderOverrides = new HashMap<Integer, IAssemblyRenderOverriding>();

    public static GuiRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public IGuiAnimatedStat getAnimatedStat(GuiScreen gui, int backgroundColor) {
        return new GuiAnimatedStat(gui, backgroundColor);
    }

    @Override
    public IGuiAnimatedStat getAnimatedStat(GuiScreen gui, ItemStack iconStack, int backgroundColor) {
        return new GuiAnimatedStat(gui, backgroundColor, iconStack);
    }

    @Override
    public IGuiAnimatedStat getAnimatedStat(GuiScreen gui, String iconTexture, int backgroundColor) {
        return new GuiAnimatedStat(gui, backgroundColor, iconTexture);
    }

    @Override
    public void drawPressureGauge(FontRenderer fontRenderer, float minPressure, float maxPressure, float dangerPressure, float minWorkingPressure, float currentPressure, int xPos, int yPos, float zLevel) {
        GuiUtils.drawPressureGauge(fontRenderer, minPressure, maxPressure, dangerPressure, minWorkingPressure, currentPressure, xPos, yPos, zLevel);
    }

    public void registerRenderOverride(Block block, IAssemblyRenderOverriding renderOverride) {
        if (block == null) throw new NullPointerException("Block is null!");
        if (renderOverride == null) throw new NullPointerException("Render override is null!");
        renderOverrides.put(Block.getIdFromBlock(block), renderOverride);
    }

    public void registerRenderOverride(Item item, IAssemblyRenderOverriding renderOverride) {
        if (item == null) throw new NullPointerException("Item is null!");
        if (renderOverride == null) throw new NullPointerException("Render override is null!");
        renderOverrides.put(Item.getIdFromItem(item), renderOverride);
    }

}
