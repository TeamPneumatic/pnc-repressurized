package me.desht.pneumaticcraft.common.thirdparty.computercraft;

import li.cil.oc.api.Driver;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.item.ItemRegistry;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.progwidgets.WidgetRegistrator;
import me.desht.pneumaticcraft.common.recipes.CraftingRegistrator;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.lib.ModIds;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber
public class OpenComputers implements IThirdParty {
    public Block droneInterface;

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        droneInterface = new BlockDroneInterface(Material.IRON);
        Blockss.registerBlock(event.getRegistry(), droneInterface);
    }

    @Override
    public void preInit() {
        GameRegistry.registerTileEntity(TileEntityDroneInterface.class, "droneInterface");
        WidgetRegistrator.register(new ProgWidgetCC());
    }

    @Override
    public void init() {
        if (!Loader.isModLoaded(ModIds.COMPUTERCRAFT)) {
            CraftingRegistrator.addRecipe(new ItemStack(droneInterface),
                    true, " u ", "mp ", "iii",
                    'u', ItemRegistry.getInstance().getUpgrade(EnumUpgrade.RANGE),
                    'm', Items.ENDER_PEARL, 'p', Itemss.PRINTED_CIRCUIT_BOARD, 'i', Names.INGOT_IRON_COMPRESSED);
        }
        if (Loader.isModLoaded(ModIds.OPEN_COMPUTERS)) {
            initializeDrivers();
        }
    }

    @Optional.Method(modid = ModIds.OPEN_COMPUTERS)
    private void initializeDrivers() {
        Driver.add(new DriverPneumaticCraft());
    }

    @Override
    public void postInit() {

    }

    @Override
    public void clientSide() {

    }

    @Override
    public void clientInit() {
    }

}
