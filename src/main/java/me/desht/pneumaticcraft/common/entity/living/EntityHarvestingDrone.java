package me.desht.pneumaticcraft.common.entity.living;

import me.desht.pneumaticcraft.common.core.ModEntities;
import me.desht.pneumaticcraft.common.progwidgets.IBlockOrdered.Ordering;
import me.desht.pneumaticcraft.common.progwidgets.*;
import me.desht.pneumaticcraft.common.util.DroneProgramBuilder;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class EntityHarvestingDrone extends EntityBasicDrone {
    public EntityHarvestingDrone(EntityType<EntityHarvestingDrone> type, World world) {
        super(type, world);
    }

    public EntityHarvestingDrone(World world, PlayerEntity player) {
        super(ModEntities.HARVESTING_DRONE.get(), world, player);
    }

    @Override
    public boolean addProgram(BlockPos clickPos, Direction facing, BlockPos pos, ItemStack droneStack, List<IProgWidget> widgets) {
        TileEntity te = world.getTileEntity(clickPos);
        ProgWidgetHarvest harvestPiece = new ProgWidgetHarvest();
        harvestPiece.setRequiresTool(IOHelper.getInventoryForTE(te, facing).isPresent());
        harvestPiece.setOrder(Ordering.HIGH_TO_LOW);
        
        DroneProgramBuilder builder = new DroneProgramBuilder();
        builder.add(new ProgWidgetStart());
        // No item filter, because we cannot guarantee we won't filter away modded hoes...
        builder.add(new ProgWidgetInventoryImport(), ProgWidgetArea.fromPosition(clickPos));
        builder.add(harvestPiece, ProgWidgetArea.fromPosition(clickPos, 16, 16, 16));
        maybeAddStandbyInstruction(builder, droneStack);
        // Wait 10 seconds for performance reasons.
        builder.add(new ProgWidgetWait(), ProgWidgetText.withText("10s"));
        widgets.addAll(builder.build());

        return true;
    }
}
