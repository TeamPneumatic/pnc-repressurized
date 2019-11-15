package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.client.gui.GuiSecurityStationHacking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.util.List;

public class ItemNetworkComponent extends ItemPneumatic implements IProgrammable {
    private final NetworkComponentType type;

    public enum NetworkComponentType {
        DIAGNOSTIC_SUBROUTINE("diagnostic_subroutine"),
        NETWORK_API("network_api"),
        NETWORK_DATA_STORAGE("network_data_storage"),
        NETWORK_IO_PORT("network_io_port"),
        NETWORK_REGISTRY("network_registry"),
        NETWORK_NODE("network_node");

        private final String name;

        NetworkComponentType(String name) {
            this.name = name;
        }

        public String getRegistryName() {
            return name;
        }
    }

    public ItemNetworkComponent(NetworkComponentType type) {
        super(type.getRegistryName());
        this.type = type;
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> curInfo, ITooltipFlag extraInfo) {
        super.addInformation(stack, worldIn, curInfo, extraInfo);

        if (PneumaticCraftRepressurized.proxy.getClientWorld() != null) {
            if (Minecraft.getInstance().currentScreen instanceof GuiSecurityStationHacking) {
                ((GuiSecurityStationHacking) Minecraft.getInstance().currentScreen).addExtraHackInfo(curInfo);
            }
        }
    }

    @Override
    public boolean canProgram(ItemStack stack) {
        return type == NetworkComponentType.NETWORK_API || type == NetworkComponentType.NETWORK_DATA_STORAGE;
    }

    @Override
    public boolean usesPieces(ItemStack stack) {
        return type == NetworkComponentType.NETWORK_API;
    }

    @Override
    public boolean showProgramTooltip() {
        return true;
    }

    public static NetworkComponentType getType(ItemStack stack) {
        return stack.getItem() instanceof ItemNetworkComponent ? ((ItemNetworkComponent) stack.getItem()).type : null;
    }
}
