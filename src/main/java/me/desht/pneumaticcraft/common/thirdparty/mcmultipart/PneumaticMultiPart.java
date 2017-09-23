package me.desht.pneumaticcraft.common.thirdparty.mcmultipart;

import mcmultipart.api.addon.IMCMPAddon;
import mcmultipart.api.addon.MCMPAddon;
import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipartRegistry;
import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.ref.MCMPCapabilities;
import mcmultipart.api.slot.EnumCenterSlot;
import mcmultipart.block.BlockMultipartContainer;
import mcmultipart.block.TileMultipartContainer;
import me.desht.pneumaticcraft.common.block.BlockPressureTube;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

@MCMPAddon
@Mod.EventBusSubscriber
public class PneumaticMultiPart implements IMCMPAddon, IThirdParty {
    private static boolean enabled = false;

    @Override
    public void registerParts(IMultipartRegistry registry) {
        if (!enabled) return;

        MinecraftForge.EVENT_BUS.register(this);

        registry.registerPartWrapper(Blockss.PRESSURE_TUBE, new PartPressureTube((BlockPressureTube) Blockss.PRESSURE_TUBE));
        registry.registerStackWrapper(Item.getItemFromBlock(Blockss.PRESSURE_TUBE), s -> true, Blockss.PRESSURE_TUBE);

        registry.registerPartWrapper(Blockss.ADVANCED_PRESSURE_TUBE, new PartPressureTube((BlockPressureTube) Blockss.ADVANCED_PRESSURE_TUBE));
        registry.registerStackWrapper(Item.getItemFromBlock(Blockss.ADVANCED_PRESSURE_TUBE), s -> true, Blockss.ADVANCED_PRESSURE_TUBE);
    }

    @SubscribeEvent
    public static void onAttachCapability(AttachCapabilitiesEvent<TileEntity> e) {
        if (!enabled) return;

        TileEntity tile = e.getObject();
        if (tile instanceof IMultipartTE) {
            register(e, ((IMultipartTE) tile).getMultipartId());
        }
    }

    private static void register(AttachCapabilitiesEvent<TileEntity> e, String multipartId) {
        e.addCapability(RL(multipartId), new ICapabilityProvider() {
            private PartPressureTubeTile tile;

            @Override
            public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
                return capability == MCMPCapabilities.MULTIPART_TILE;
            }

            @Nullable
            @Override
            public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
                if (capability == MCMPCapabilities.MULTIPART_TILE) {
                    if (tile == null) {
                        tile = new PartPressureTubeTile(e.getObject());
                    }
                    return MCMPCapabilities.MULTIPART_TILE.cast(tile);
                }

                return null;
            }
        });
    }

    @Nullable
    public static TileEntity unwrapTile(IBlockAccess world, BlockPos pos) {
        TileEntity tile = PneumaticCraftUtils.getTileEntitySafely(world, pos);

        if (tile instanceof TileMultipartContainer) {
            Optional<IMultipartTile> multipartTile = ((TileMultipartContainer) tile).getPartTile(EnumCenterSlot.CENTER);

            if (multipartTile.isPresent()) {
                return multipartTile.get().getTileEntity();
            }
        }

        return tile;
    }

    public static Block unwrapBlock(IBlockAccess world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);

        if (state.getBlock() instanceof BlockMultipartContainer) {
            Optional<TileMultipartContainer> multipartContainer = BlockMultipartContainer.getTile(world, pos);

            if (multipartContainer.isPresent()) {
                Optional<IPartInfo> info = multipartContainer.get().get(EnumCenterSlot.CENTER);

                if (info.isPresent()) {
                    return info.get().getPart().getBlock();
                }
            }
        }

        return state.getBlock();
    }

    @Override
    public void preInit() {
        enabled = true;
    }

    @Override
    public void init() {

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