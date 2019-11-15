package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.client.model.module.ModelModuleBase;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketOpenTubeModuleGui;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;

import static me.desht.pneumaticcraft.lib.BBConstants.PRESSURE_PIPE_MAX_POS;
import static me.desht.pneumaticcraft.lib.BBConstants.PRESSURE_PIPE_MIN_POS;

public abstract class TubeModule {
    public static final float MAX_VALUE = 30;

    protected IPneumaticPosProvider pressureTube;
    protected Direction dir = Direction.UP;
    public final AxisAlignedBB[] boundingBoxes = new AxisAlignedBB[6];
    protected boolean upgraded;
    public float lowerBound = 7.5F, higherBound = 0;
    private boolean fake;
    public boolean advancedConfig;
    public boolean shouldDrop;
    // FIXME - move to external renderer
    @OnlyIn(Dist.CLIENT)
    private ModelModuleBase model;

    public TubeModule() {
        double width = getWidth() / 2;
        double height = getHeight();

        // 0..6 = D,U,N,S,W,E
        boundingBoxes[0] = new AxisAlignedBB(0.5 - width, PRESSURE_PIPE_MIN_POS - height, 0.5 - width, 0.5 + width, PRESSURE_PIPE_MIN_POS, 0.5 + width);
        boundingBoxes[1] = new AxisAlignedBB(0.5 - width, PRESSURE_PIPE_MAX_POS, 0.5 - width, 0.5 + width, PRESSURE_PIPE_MAX_POS + height, 0.5 + width);
        boundingBoxes[2] = new AxisAlignedBB(0.5 - width, 0.5 - width, PRESSURE_PIPE_MIN_POS - height, 0.5 + width, 0.5 + width, PRESSURE_PIPE_MIN_POS);
        boundingBoxes[3] = new AxisAlignedBB(0.5 - width, 0.5 - width, PRESSURE_PIPE_MAX_POS, 0.5 + width, 0.5 + width, PRESSURE_PIPE_MAX_POS + height);
        boundingBoxes[4] = new AxisAlignedBB(PRESSURE_PIPE_MIN_POS - height, 0.5 - width, 0.5 - width, PRESSURE_PIPE_MIN_POS, 0.5 + width, 0.5 + width);
        boundingBoxes[5] = new AxisAlignedBB(PRESSURE_PIPE_MAX_POS, 0.5 - width, 0.5 - width, PRESSURE_PIPE_MAX_POS + height, 0.5 + width, 0.5 + width);
    }

    public void markFake() {
        fake = true;
    }

    public boolean isFake() {
        return fake;
    }

    public void setTube(IPneumaticPosProvider pressureTube) {
        this.pressureTube = pressureTube;
    }

    public IPneumaticPosProvider getTube() {
        return pressureTube;
    }

    public double getWidth() {
        return PRESSURE_PIPE_MAX_POS - PRESSURE_PIPE_MIN_POS;
    }

    protected double getHeight() {
        return PRESSURE_PIPE_MIN_POS;
    }

    public float getThreshold(int redstone) {
        double slope = (higherBound - lowerBound) / 15;
        double threshold = lowerBound + slope * redstone;
        return (float) threshold;
    }

    /**
     * Returns the item(s) that this part drops.
     *
     * @return the module item and possibly an Advanced PCB too
     */
    public NonNullList<ItemStack> getDrops() {
        NonNullList<ItemStack> drops = NonNullList.create();
        if (shouldDrop) {
            drops.add(new ItemStack(ModuleRegistrator.getModuleItem(getType())));
            if (upgraded) drops.add(new ItemStack(ModItems.ADVANCED_PCB));
        }
        return drops;
    }

    public void setDirection(Direction dir) {
        this.dir = dir;
    }

    public Direction getDirection() {
        return dir;
    }

    public void readFromNBT(CompoundNBT nbt) {
        dir = Direction.byIndex(nbt.getInt("dir"));
        upgraded = nbt.getBoolean("upgraded");
        lowerBound = nbt.getFloat("lowerBound");
        higherBound = nbt.getFloat("higherBound");
        advancedConfig = !nbt.contains("advancedConfig") || nbt.getBoolean("advancedConfig");
    }

    public void writeToNBT(CompoundNBT nbt) {
        nbt.putInt("dir", dir.ordinal());
        nbt.putBoolean("upgraded", upgraded);
        nbt.putFloat("lowerBound", lowerBound);
        nbt.putFloat("higherBound", higherBound);
        nbt.putBoolean("advancedConfig", advancedConfig);
    }

    public void update() {
    }

    public void onNeighborTileUpdate() {
    }

    public void onNeighborBlockUpdate() {
    }

    /**
     * Get a unique string identifier for this module type.
     *
     * @return the module ID
     */
    public abstract String getType();

    public int getRedstoneLevel() {
        return 0;
    }

    void updateNeighbors() {
        pressureTube.world().notifyNeighborsOfStateChange(pressureTube.pos(), pressureTube.world().getBlockState(pressureTube.pos()).getBlock());
    }

    public boolean isInline() {
        return false;
    }

    public void sendDescriptionPacket() {
        if (pressureTube instanceof TileEntityPressureTube) ((TileEntityPressureTube) pressureTube).sendDescriptionPacket();
    }

    public void addInfo(List<ITextComponent> curInfo) {
        if (upgraded) {
            ItemStack stack = new ItemStack(ModItems.ADVANCED_PCB);
            curInfo.add(stack.getDisplayName().appendText(" installed").applyTextStyle(TextFormatting.GREEN));
        }
        if (this instanceof INetworkedModule) {
            int colorChannel = ((INetworkedModule) this).getColorChannel();
            String key = "color.minecraft." + DyeColor.byId(colorChannel);
            curInfo.add(new StringTextComponent("waila.logisticsModule.channel").appendText(" ")
                    .appendSibling(new StringTextComponent(key).applyTextStyle(TextFormatting.YELLOW)));
        }
    }

    public boolean canUpgrade() {
        return true;
    }

    public void upgrade() {
        upgraded = true;
    }

    public boolean isUpgraded() {
        return upgraded;
    }

    public boolean onActivated(PlayerEntity player, Hand hand) {
        if (!player.world.isRemote && upgraded && hasGui()) {
            NetworkHandler.sendToPlayer(new PacketOpenTubeModuleGui(getType(), pressureTube.pos()), (ServerPlayerEntity) player);
            return true;
        }
        return false;
    }

    public boolean hasGui() {
        return false;
    }

    // FIXME move this out of here to external tube->model registry
    @OnlyIn(Dist.CLIENT)
    public abstract Class<? extends ModelModuleBase> getModelClass();

    @OnlyIn(Dist.CLIENT)
    public final ModelModuleBase getModel() {
        if (model == null) {
            try {
                Constructor<? extends ModelModuleBase> ctor = getModelClass().getDeclaredConstructor(this.getClass());
                model = ctor.newInstance(this);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
                model = new ModelModuleBase.MissingModel();
            }
        }
        return model;
    }

    @OnlyIn(Dist.CLIENT)
    public void doExtraRendering() {
    }

    public AxisAlignedBB getRenderBoundingBox() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TubeModule)) return false;
        TubeModule that = (TubeModule) o;
        return Objects.equals(pressureTube.pos(), that.pressureTube.pos()) && dir == that.dir;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pressureTube.pos(), dir);
    }
}
