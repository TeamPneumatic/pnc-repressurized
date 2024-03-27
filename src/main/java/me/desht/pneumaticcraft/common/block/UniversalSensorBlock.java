package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.block.entity.UniversalSensorBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class UniversalSensorBlock extends AbstractPneumaticCraftBlock implements PneumaticCraftEntityBlock {
    private static final VoxelShape SHAPE = Shapes.join(
            Block.box(4, 2, 4, 12, 7, 12),
            Block.box(1, 0, 1, 15, 2, 15),
            BooleanOp.OR);

    public UniversalSensorBlock() {
        super(ModBlocks.defaultProps());

        registerDefaultState(defaultBlockState()
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(EAST, false)
        );
    }

    @Override
    protected boolean isWaterloggable() {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        world.getBlockEntity(pos, ModBlockEntityTypes.UNIVERSAL_SENSOR.get()).ifPresent(teUS -> {
            if (entity instanceof Player && !(entity instanceof FakePlayer)) {
                teUS.setPlayerId(entity.getUUID());
            }
        });

        super.setPlacedBy(world, pos, state, entity, stack);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(AbstractPneumaticCraftBlock.NORTH, AbstractPneumaticCraftBlock.SOUTH, AbstractPneumaticCraftBlock.WEST, AbstractPneumaticCraftBlock.EAST);
    }

    @Override
    public int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return blockAccess.getBlockEntity(pos, ModBlockEntityTypes.UNIVERSAL_SENSOR.get())
                .map(te -> side == Direction.UP ? te.redstoneStrength : 0)
                .orElse(0);
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return blockAccess.getBlockEntity(pos, ModBlockEntityTypes.UNIVERSAL_SENSOR.get())
                .map(te -> te.redstoneStrength).orElse(0);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new UniversalSensorBlockEntity(pPos, pState);
    }

    @Override
    protected void doOpenGui(ServerPlayer player, BlockEntity te) {
        player.openMenu((MenuProvider) te, buf -> {
            buf.writeBlockPos(te.getBlockPos());
            Collection<String> vars = GlobalVariableManager.getInstance().getAllActiveVariableNames(player);
            buf.writeVarInt(vars.size());
            vars.forEach(buf::writeUtf);
        });
    }
}
