package me.desht.pneumaticcraft.common.thirdparty.computercraft;

import li.cil.oc.api.Driver;
import me.desht.pneumaticcraft.api.event.PuzzleRegistryEvent;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.progwidgets.WidgetRegistrator;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class OpenComputers implements IThirdParty {
    @GameRegistry.ObjectHolder("pneumaticcraft:drone_interface")
    public static final Block DRONE_INTERFACE = null;

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        Blockss.registerBlock(event.getRegistry(), new BlockDroneInterface());
    }

    @Override
    public void preInit() {
        MinecraftForge.EVENT_BUS.register(this);
        GameRegistry.registerTileEntity(TileEntityDroneInterface.class, "droneInterface");
    }

    @Override
    public void init() {
        if (Loader.isModLoaded(ModIds.OPEN_COMPUTERS)) {
            initializeDrivers();
        }
    }

    @SubscribeEvent
    public void onPuzzleRegister(PuzzleRegistryEvent event) {
        WidgetRegistrator.register(new ProgWidgetCC());
    }

    @Optional.Method(modid = ModIds.OPEN_COMPUTERS)
    private void initializeDrivers() {
        Driver.add(new DriverPneumaticCraft());
    }

}
