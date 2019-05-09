package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.client.gui.widget.GuiKeybindCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.IGuiWidget;
import me.desht.pneumaticcraft.client.gui.widget.IWidgetListener;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateArmorExtraData;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.nbt.NBTTagCompound;

public class GuiJetBootsOptions extends IOptionPage.SimpleToggleableOptions implements IWidgetListener {
    public static final String NBT_BUILDER_MODE = "JetBootsBuilderMode";

    private GuiKeybindCheckBox checkBox;

    public GuiJetBootsOptions(IUpgradeRenderHandler handler) {
        super(handler);
    }

    @Override
    public void initGui(IGuiScreen gui) {
        super.initGui(gui);

        checkBox = new GuiKeybindCheckBox(0, 5, 45, 0xFFFFFFFF, "jetboots.module.builderMode");
        ((GuiHelmetMainScreen) gui).addWidget(checkBox);
        checkBox.setListener(this);
    }

    @Override
    public void updateScreen() {
        CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer();
        checkBox.enabled = handler.getUpgradeCount(EntityEquipmentSlot.FEET, IItemRegistry.EnumUpgrade.JET_BOOTS) >= 8;
    }

    @Override
    public void actionPerformed(IGuiWidget widget) {
        if (widget == GuiKeybindCheckBox.fromKeyBindingName("jetboots.module.builderMode")) {
            CommonArmorHandler commonArmorHandler = CommonArmorHandler.getHandlerForPlayer();
            if (commonArmorHandler.getUpgradeCount(EntityEquipmentSlot.FEET, IItemRegistry.EnumUpgrade.JET_BOOTS) >= 8) {
                boolean checked = ((GuiKeybindCheckBox) widget).checked;
                NBTTagCompound tag = new NBTTagCompound();
                tag.setBoolean(NBT_BUILDER_MODE, checked);
                NetworkHandler.sendToServer(new PacketUpdateArmorExtraData(EntityEquipmentSlot.FEET, tag));
                CommonArmorHandler.getHandlerForPlayer().onDataFieldUpdated(EntityEquipmentSlot.FEET, NBT_BUILDER_MODE, tag.getTag(NBT_BUILDER_MODE));
                HUDHandler.instance().addFeatureToggleMessage(getRenderHandler(), checkBox.text, checked);
            }
        }
    }

    @Override
    public void onKeyTyped(IGuiWidget widget) {
    }
}
