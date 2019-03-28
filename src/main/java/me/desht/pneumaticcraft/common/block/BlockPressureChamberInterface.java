package me.desht.pneumaticcraft.common.block;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberInterface;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberInterface.EnumInterfaceMode;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberValve;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

public class BlockPressureChamberInterface extends BlockPneumaticCraftModeled implements IBlockPressureChamber {

    BlockPressureChamberInterface() {
        super(Material.IRON, "pressure_chamber_interface");
        setResistance(2000.f);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityPressureChamberInterface.class;
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.PRESSURE_CHAMBER_INTERFACE;
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
    public void onBlockPlacedBy(World par1World, BlockPos pos, IBlockState state, EntityLivingBase par5EntityLiving, ItemStack iStack) {
        super.onBlockPlacedBy(par1World, pos, state, par5EntityLiving, iStack);
        if (TileEntityPressureChamberValve.checkIfProperlyFormed(par1World, pos) && par5EntityLiving instanceof EntityPlayerMP) {
            AdvancementTriggers.PRESSURE_CHAMBER.trigger((EntityPlayerMP) par5EntityLiving);
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityPressureChamberInterface && !world.isRemote) {
            ((TileEntityPressureChamberInterface) te).onBlockBreak();
        }
        super.breakBlock(world, pos, state);

    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);

        TileEntity te = world.getTileEntity(data.getPos());
        if (te instanceof TileEntityPressureChamberInterface) {
            EnumInterfaceMode interfaceMode = ((TileEntityPressureChamberInterface) te).interfaceMode;
            String text = TextFormatting.GRAY + "Interface mode: " + TextFormatting.WHITE
                    + PneumaticCraftUtils.xlate("waila.interface.mode." + interfaceMode.toString().toLowerCase());
            probeInfo.text(text);
        }
    }
}
