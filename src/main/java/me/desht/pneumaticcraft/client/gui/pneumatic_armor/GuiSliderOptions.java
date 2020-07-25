package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateArmorExtraData;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.widget.Slider;
import org.apache.commons.lang3.tuple.Pair;

public abstract class GuiSliderOptions<T extends IUpgradeRenderHandler> extends IOptionPage.SimpleToggleableOptions<T>
        implements Slider.ISlider {
    private Slider slider;
    private Integer pendingVal = null;

    GuiSliderOptions(IGuiScreen screen, T handler) {
        super(screen, handler);
    }

    protected PointXY getSliderPos() {
        return new PointXY(30, 60);
    }

    protected Pair<Integer, Integer> getRange() {
        return Pair.of(0, 100);
    }

    /**
     * The NBT tag on the armor item under which this slider value should be saved.  The item used is determined
     * by {@link #getSlot()}.
     */
    protected abstract String getTagName();

    protected abstract ITextComponent getPrefix();

    protected abstract ITextComponent getSuffix();

    EquipmentSlotType getSlot() {
        return getUpgradeHandler().getEquipmentSlot();
    }

    public void populateGui(IGuiScreen gui) {
        Pair<Integer,Integer> range = getRange();
        int initVal = range.getRight();
        if (Minecraft.getInstance().player != null) {
            ItemStack stack = Minecraft.getInstance().player.getItemStackFromSlot(getSlot());
            initVal = ItemPneumaticArmor.getIntData(stack, getTagName(), range.getRight());
        }
        PointXY pos = getSliderPos();
        slider = new Slider(pos.x, pos.y, 150, 20,  getPrefix(), getSuffix(),
                range.getLeft(), range.getRight(), initVal, false, true, b -> { }, this);
        gui.addWidget(slider);
    }

    @Override
    public void onChangeSliderValue(Slider slider) {
        pendingVal = slider.getValueInt();
    }

    public void tick() {
        if (pendingVal != null && !slider.dragging) {
            // avoid sending a stream of update packets if player is dragging slider
            CompoundNBT tag = new CompoundNBT();
            tag.putInt(getTagName(), pendingVal);
            NetworkHandler.sendToServer(new PacketUpdateArmorExtraData(getSlot(), tag));
            // also update the clientside handler
            CommonArmorHandler.getHandlerForPlayer().onDataFieldUpdated(getTagName(), tag.get(getTagName()));
            pendingVal = null;
        }
    }
}
