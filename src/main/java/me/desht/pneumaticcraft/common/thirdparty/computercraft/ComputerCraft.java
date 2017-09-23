package me.desht.pneumaticcraft.common.thirdparty.computercraft;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.item.ItemRegistry;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.recipes.CraftingRegistrator;
import me.desht.pneumaticcraft.common.thirdparty.IRegistryListener;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.ModIds;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ComputerCraft extends OpenComputers implements IRegistryListener {
    @GameRegistry.ObjectHolder(ModIds.COMPUTERCRAFT + ":CC-Peripheral")
    public static final Block MODEM = null;

    @Override
    public void preInit() {
        ThirdPartyManager.computerCraftLoaded = true;
        PneumaticRegistry.getInstance().getHelmetRegistry().registerBlockTrackEntry(new BlockTrackEntryPeripheral());
        super.preInit();
    }

    @Override
    public void init() {
        if (Loader.isModLoaded(ModIds.OPEN_COMPUTERS)) super.init();
        if (MODEM != null) {
            CraftingRegistrator.addRecipe(new ItemStack(droneInterface),
                    true, " u ", "mp ", "iii",
                    'u', ItemRegistry.getInstance().getUpgrade(EnumUpgrade.RANGE),
                    'm', new ItemStack(MODEM, 1, 1), 'p', Itemss.PRINTED_CIRCUIT_BOARD, 'i', Names.INGOT_IRON_COMPRESSED);
        } else {
            Log.error("Wireless Modem block not found! Using the backup recipe");
            CraftingRegistrator.addRecipe(new ItemStack(droneInterface),
                    " u ", "mp ", "iii",
                    'u', ItemRegistry.getInstance().getUpgrade(EnumUpgrade.RANGE),
                    'm', Items.ENDER_PEARL, 'p', Itemss.PRINTED_CIRCUIT_BOARD, 'i', Names.INGOT_IRON_COMPRESSED);
        }
    }

    @Override
    public void onItemRegistry(Item item) {
    }

    @Override
    public void onBlockRegistry(Block block) {
        if (block instanceof IPeripheralProvider) {
            ComputerCraftAPI.registerPeripheralProvider((IPeripheralProvider) block);
        }
    }

}
