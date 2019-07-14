package me.desht.pneumaticcraft.common.block;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberInterface;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberInterface.InterfaceDirection;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberValve;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

public class BlockPressureChamberInterface extends BlockPneumaticCraft implements IBlockPressureChamber {

    public BlockPressureChamberInterface() {
        super(DEFAULT_PROPS.hardnessAndResistance(3f, 2000f), "pressure_chamber_interface");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityPressureChamberInterface.class;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    protected boolean canRotateToTopOrBottom() {
        return true;
    }

    /**
     * Called when the block is placed in the world.
     */
    @Override
    public void onBlockPlacedBy(World par1World, BlockPos pos, BlockState state, LivingEntity par5EntityLiving, ItemStack iStack) {
        super.onBlockPlacedBy(par1World, pos, state, par5EntityLiving, iStack);
        if (TileEntityPressureChamberValve.checkIfProperlyFormed(par1World, pos) && par5EntityLiving instanceof ServerPlayerEntity) {
            AdvancementTriggers.PRESSURE_CHAMBER.trigger((ServerPlayerEntity) par5EntityLiving);
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, BlockState state) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityPressureChamberInterface && !world.isRemote) {
            ((TileEntityPressureChamberInterface) te).onBlockBreak();
        }
        super.breakBlock(world, pos, state);

    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);

        TileEntity te = world.getTileEntity(data.getPos());
        if (te instanceof TileEntityPressureChamberInterface) {
            InterfaceDirection interfaceMode = ((TileEntityPressureChamberInterface) te).interfaceMode;
            String text = TextFormatting.GRAY + "Interface mode: " + TextFormatting.WHITE
                    + PneumaticCraftUtils.xlate("waila.interface.mode." + interfaceMode.toString().toLowerCase());
            probeInfo.text(text);
        }
    }
}
