package me.desht.pneumaticcraft.common.entity.living;

import me.desht.pneumaticcraft.common.core.ModEntityTypes;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.progwidgets.IBlockOrdered.EnumOrder;
import me.desht.pneumaticcraft.common.progwidgets.*;
import me.desht.pneumaticcraft.common.util.DroneProgramBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.List;

public class EntityHarvestingDrone extends EntityBasicDrone {
    public static EntityHarvestingDrone create(EntityType<Entity> entityEntityType, World world) {
        return new EntityHarvestingDrone(world);
    }

    public static Entity createClient(FMLPlayMessages.SpawnEntity spawnEntity, World world) {
        return new EntityHarvestingDrone(world);
    }

    private EntityHarvestingDrone(World world) {
        super(ModEntityTypes.HARVESTING_DRONE, world);
    }

    public EntityHarvestingDrone(World world, PlayerEntity player) {
        super(ModEntityTypes.LOGISTIC_DRONE, world, player);
    }

    @Override
    protected Item getDroneItem(){
        return ModItems.HARVESTING_DRONE;
    }

    @Override
    public void addProgram(BlockPos clickPos, Direction facing, BlockPos pos, List<IProgWidget> widgets) {
        TileEntity te = world.getTileEntity(clickPos);
        ProgWidgetHarvest harvestPiece = new ProgWidgetHarvest();
        harvestPiece.setRequiresTool(te != null && te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing).isPresent());
        harvestPiece.setOrder(EnumOrder.HIGH_TO_LOW);
        
        DroneProgramBuilder builder = new DroneProgramBuilder();
        builder.add(new ProgWidgetStart());
        builder.add(new ProgWidgetInventoryImport(), ProgWidgetArea.fromPosition(clickPos)); //No filter, because we cannot guarantee we won't filter away modded hoes...
        builder.add(harvestPiece, ProgWidgetArea.fromPosAndExpansions(clickPos, 16, 16, 16));
        builder.add(new ProgWidgetWait(), ProgWidgetString.withText("10s")); //Wait 10 seconds for performance reasons.
        widgets.addAll(builder.build());
    }
    
}
