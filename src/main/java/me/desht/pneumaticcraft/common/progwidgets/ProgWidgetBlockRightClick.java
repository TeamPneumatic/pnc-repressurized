package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.programmer.GuiProgWidgetBlockRightClick;
import me.desht.pneumaticcraft.common.ai.DroneAIBlockInteract;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ProgWidgetBlockRightClick extends ProgWidgetPlace implements IBlockRightClicker {

    private boolean sneaking;

    @Override
    public String getWidgetString() {
        return "blockRightClick";
    }

    @Override
    public int getCraftingColorIndex() {
        return ItemPlastic.YELLOW;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_BLOCK_RIGHT_CLICK;
    }

    @Override
    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return setupMaxActions(new DroneAIBlockInteract(drone, (ProgWidgetAreaItemBase) widget), (IMaxActions) widget);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Screen getOptionWindow(GuiProgrammer guiProgrammer) {
        return new GuiProgWidgetBlockRightClick(this, guiProgrammer);
    }

    @Override
    public boolean isSneaking() {
        return sneaking;
    }

    public void setSneaking(boolean sneaking) {
        this.sneaking = sneaking;
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        tag.putBoolean("sneaking", sneaking);
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        sneaking = tag.getBoolean("sneaking");
    }

}
