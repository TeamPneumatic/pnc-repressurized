/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.tubemodules;

import me.desht.pneumaticcraft.client.gui.tubemodule.AbstractTubeModuleScreen;
import me.desht.pneumaticcraft.common.block.PressureTubeBlock;
import me.desht.pneumaticcraft.common.block.entity.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Objects;

import static me.desht.pneumaticcraft.common.block.PressureTubeBlock.CORE_MAX;
import static me.desht.pneumaticcraft.common.block.PressureTubeBlock.CORE_MIN;

public abstract class AbstractTubeModule {
    public static final float MAX_VALUE = 30;

    protected final PressureTubeBlockEntity pressureTube;
    private final VoxelShape[] boundingBoxes;
    protected final Direction dir;

    private boolean fake;
    private boolean shouldDrop;
    protected boolean upgraded;
    public float lowerBound = 4.9F, higherBound = 0;
    public boolean advancedConfig;
    private ResourceLocation regName;

    public AbstractTubeModule(Direction dir, PressureTubeBlockEntity pressureTube) {
        this.dir = dir;
        this.pressureTube = pressureTube;

        double w = getWidth() / 2;
        double h = getHeight();

        // 0..6 = D,U,N,S,W,E
        boundingBoxes = new VoxelShape[] {
                Block.box(8 - w, CORE_MIN - h, 8 - w, 8 + w, CORE_MIN, 8 + w),
                Block.box(8 - w, CORE_MAX, 8 - w, 8 + w, CORE_MAX + h, 8 + w),
                Block.box(8 - w, 8 - w, CORE_MIN - h, 8 + w, 8 + w, CORE_MIN),
                Block.box(8 - w, 8 - w, CORE_MAX, 8 + w, 8 + w, CORE_MAX + h),
                Block.box(CORE_MIN - h, 8 - w, 8 - w, CORE_MIN, 8 + w, 8 + w),
                Block.box(CORE_MAX, 8 - w, 8 - w, CORE_MAX + h, 8 + w, 8 + w),
        };
    }

    public void markFake() {
        fake = true;
    }

    public boolean isFake() {
        return fake;
    }

    public PressureTubeBlockEntity getTube() {
        return pressureTube;
    }

    public boolean isValid() {
        return !pressureTube.isRemoved();
    }

    protected final void setChanged() {
        if (pressureTube != null && pressureTube.getLevel() != null && !pressureTube.getLevel().isClientSide) {
            pressureTube.setChanged();
        }
    }

    /**
     * Get the module's width (in range 0..16 as passed to {@link Block#box(double, double, double, double, double, double)}
     *
     * @return the width
     */
    public double getWidth() {
        return CORE_MAX - CORE_MIN;
    }

    /**
     * Get the module's height (in range 0..16 as passed to {@link Block#box(double, double, double, double, double, double)}
     *
     * @return the height
     */
    protected double getHeight() {
        return CORE_MIN;
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
            drops.add(new ItemStack(getItem()));
            if (upgraded) drops.add(new ItemStack(ModItems.MODULE_EXPANSION_CARD.get()));
        }
        return drops;
    }

    public abstract Item getItem();

    public Direction getDirection() {
        return dir;
    }

    public void readFromNBT(CompoundTag nbt) {
        lowerBound = nbt.getFloat("lowerBound");
        higherBound = nbt.getFloat("higherBound");
        upgraded = nbt.getBoolean("upgraded");
        advancedConfig = nbt.getBoolean("advancedConfig");
    }

    public CompoundTag writeToNBT(CompoundTag nbt) {
        // important to write dir here, even though it's not read above; see PressureTubeBlockEntity#readFromPacket
        nbt.putInt("dir", dir.get3DDataValue());
        nbt.putFloat("lowerBound", lowerBound);
        nbt.putFloat("higherBound", higherBound);
        if (upgraded) nbt.putBoolean("upgraded", true);
        if (advancedConfig) nbt.putBoolean("advancedConfig", true);
        return nbt;
    }

    protected void tickCommon() {
    }

    public void tickClient() {
        tickCommon();
    }

    public void tickServer() {
        shouldDrop = true;
        tickCommon();
    }

    public void onNeighborTileUpdate() {
    }

    public void onNeighborBlockUpdate() {
    }

    public final ResourceLocation getType() {
        if (regName == null) {
            regName = PneumaticCraftUtils.getRegistryName(getItem()).orElseThrow();
        }
        return regName;
    }

    public int getRedstoneLevel() {
        return 0;
    }

    public void updateNeighbors() {
        Level level = pressureTube.nonNullLevel();
        BlockPos pos = pressureTube.getBlockPos();
        level.updateNeighborsAt(pos, level.getBlockState(pos).getBlock());
    }

    public boolean isInline() {
        return false;
    }

    public void sendDescriptionPacket() {
        pressureTube.sendDescriptionPacket();
    }

    public void addInfo(List<Component> curInfo) {
        if (upgraded) {
            ItemStack stack = new ItemStack(ModItems.MODULE_EXPANSION_CARD.get());
            curInfo.add(stack.getHoverName().copy().append(" installed").withStyle(ChatFormatting.GREEN));
        }
        if (this instanceof INetworkedModule) {
            int colorChannel = ((INetworkedModule) this).getColorChannel();
            String key = "color.minecraft." + DyeColor.byId(colorChannel);
            curInfo.add(Component.translatable("pneumaticcraft.waila.logisticsModule.channel").append(" ")
                    .append(Component.translatable(key).withStyle(ChatFormatting.YELLOW)));
        }
    }

    public boolean canUpgrade() {
        return true;
    }

    public void upgrade() {
        if (!upgraded) {
            setChanged();
            upgraded = true;
        }
    }

    public boolean isUpgraded() {
        return upgraded;
    }

    public boolean onActivated(Player player, InteractionHand hand) {
        if (player.level().isClientSide && hasGui()) {
            AbstractTubeModuleScreen.openGuiForModule(this);
            return true;
        }
        return false;
    }

    /**
     * Does this module have a gui?  Server also needs to know about this, since module GUI's are opened in response
     * to a packet from the server.
     *
     * @return true if the module has a gui
     */
    public boolean hasGui() {
        return false;
    }

    public VoxelShape getShape() {
        return boundingBoxes[getDirection().get3DDataValue()];
    }

    public AABB getRenderBoundingBox() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractTubeModule)) return false;
        AbstractTubeModule that = (AbstractTubeModule) o;
        return Objects.equals(pressureTube.getBlockPos(), that.pressureTube.getBlockPos()) && dir == that.dir;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pressureTube.getBlockPos(), dir);
    }

    @SuppressWarnings("EmptyMethod")
    public void onPlaced() {
    }

    /**
     * Called just before a module is removed from a pressure tube, either by a player wrenching it, or when data
     * that is read from NBT causes the module to be removed.
     */
    public void onRemoved() {
    }

    /**
     * Only relevant to inline modules, where the inline module may cover more of the tube than just
     * the side it's installed on.
     */
    public boolean isInlineAndFocused(PressureTubeBlock.TubeHitInfo hitInfo) {
        return false;
    }
}
