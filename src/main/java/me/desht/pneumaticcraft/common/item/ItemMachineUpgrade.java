package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemMachineUpgrade extends ItemPneumatic {
    private final int index;

    public ItemMachineUpgrade(String registryName, int index) {
        super(registryName);
        this.index = index;
    }

    public IItemRegistry.EnumUpgrade getUpgradeType() {
        return IItemRegistry.EnumUpgrade.values()[index];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> infoList, ITooltipFlag par4) {
        if (PneumaticCraftRepressurized.proxy.isSneakingInGui()) {
            infoList.add(I18n.format("gui.tooltip.item.upgrade.usedIn"));
            PneumaticRegistry.getInstance().getItemRegistry().addTooltip(this, infoList);
        } else {
            infoList.add(I18n.format("gui.tooltip.item.upgrade.shiftMessage"));
        }
        super.addInformation(stack, world, infoList, par4);
    }

}
