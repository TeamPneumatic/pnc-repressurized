package me.desht.pneumaticcraft.common.thirdparty.ic2;

import ic2.api.item.IC2Items;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

@GameRegistry.ObjectHolder(Names.MOD_ID)
public class IC2 implements IThirdParty, IGuiHandler {

    @GameRegistry.ObjectHolder("pneumatic_generator")
    public static final Block PNEUMATIC_GENERATOR = null;
    @GameRegistry.ObjectHolder("electric_compressor")
    public static final Block ELECTRIC_COMPRESSOR = null;

    public static ItemStack glassFibreCable;
    public static ItemStack overclockerUpgrade;
    public static ItemStack transformerUpgrade;
    public static ItemStack energyStorageUpgrade;

    @Override
    public void preInit() {
        MinecraftForge.EVENT_BUS.register(this);

        GameRegistry.registerTileEntity(TileEntityPneumaticGenerator.class, RL("pneumaticGenerator"));
        GameRegistry.registerTileEntity(TileEntityElectricCompressor.class, RL("electricCompressor"));

        PneumaticRegistry.getInstance().getHelmetRegistry().registerBlockTrackEntry(new BlockTrackEntryIC2());
    }

    @Override
    public void init() {
        glassFibreCable = IC2Items.getItem("cable", "type:glass");
        overclockerUpgrade = IC2Items.getItem("upgrade", "overclocker");
        transformerUpgrade = IC2Items.getItem("upgrade", "transformer");
        energyStorageUpgrade = IC2Items.getItem("upgrade", "energy_storage");
    }

    @SubscribeEvent
    public void onBlockRegister(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new BlockElectricCompressor());
        event.getRegistry().register(new BlockPneumaticGenerator());
    }

    @SubscribeEvent
    public void onItemRegister(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new ItemBlock(ELECTRIC_COMPRESSOR).setRegistryName(ELECTRIC_COMPRESSOR.getRegistryName()));
        event.getRegistry().register(new ItemBlock(PNEUMATIC_GENERATOR).setRegistryName(PNEUMATIC_GENERATOR.getRegistryName()));
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onModelRegister(ModelRegistryEvent event) {
        registerModel(PNEUMATIC_GENERATOR);
        registerModel(ELECTRIC_COMPRESSOR);
    }

    @SideOnly(Side.CLIENT)
    private void registerModel(Block block) {
        Item item = ItemBlock.getItemFromBlock(block);
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.isBlockLoaded(pos) ? world.getTileEntity(pos) : null;
        switch (EnumGuiId.values()[ID]) {
            case PNEUMATIC_GENERATOR:
                return new ContainerPneumaticGenerator(player.inventory, (TileEntityPneumaticGenerator) te);
            case ELECTRIC_COMPRESSOR:
                return new ContainerElectricCompressor(player.inventory, (TileEntityElectricCompressor) te);
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.isBlockLoaded(pos) ? world.getTileEntity(pos) : null;
        switch (EnumGuiId.values()[ID]) {
            case PNEUMATIC_GENERATOR:
                return new GuiPneumaticGenerator(player.inventory, (TileEntityPneumaticGenerator) te);
            case ELECTRIC_COMPRESSOR:
                return new GuiElectricCompressor(player.inventory, (TileEntityElectricCompressor) te);
        }
        return null;
    }
}
