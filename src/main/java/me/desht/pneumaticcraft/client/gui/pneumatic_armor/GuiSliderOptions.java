package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateArmorExtraData;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.client.config.GuiSlider;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;

public abstract class GuiSliderOptions extends IOptionPage.SimpleToggleableOptions implements GuiSlider.ISlider {
    private GuiSlider slider;
    private Integer pendingVal = null;

    GuiSliderOptions(IUpgradeRenderHandler handler) {
        super(handler);
    }

    protected Point getSliderPos() {
        return new Point(30, 60);
    }

    protected Pair<Integer, Integer> getRange() {
        return Pair.of(0, 100);
    }

    protected abstract String getTagName();

    protected abstract EquipmentSlotType getSlot();

    protected abstract String getPrefix();

    protected abstract String getSuffix();

    public void initGui(IGuiScreen gui) {
        Pair<Integer,Integer> range = getRange();
        int initVal = range.getRight();
        if (Minecraft.getInstance().player != null) {
            ItemStack leggings = Minecraft.getInstance().player.getItemStackFromSlot(getSlot());
            initVal = ItemPneumaticArmor.getIntData(leggings, getTagName(), range.getRight());
        }
        Point pos = getSliderPos();
        slider = new GuiSlider(pos.x, pos.y, 150, 20,  getPrefix(), getSuffix(),
                range.getLeft(), range.getRight(), initVal, false, true, b -> { }, this);
        gui.getWidgetList().add(slider);
    }

    @Override
    public void onChangeSliderValue(GuiSlider slider) {
        pendingVal = slider.getValueInt();
    }

    public void tick() {
        if (pendingVal != null && !slider.dragging) {
            // avoid sending a stream of update packets if player is dragging slider
            CompoundNBT tag = new CompoundNBT();
            tag.putInt(getTagName(), pendingVal);
            NetworkHandler.sendToServer(new PacketUpdateArmorExtraData(getSlot(), tag));
            // also update the clientside handler
            CommonArmorHandler.getHandlerForPlayer().onDataFieldUpdated(getSlot(), getTagName(), tag.get(getTagName()));
            pendingVal = null;
        }
    }
}
