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

package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.client.gui.tubemodule.GuiTubeModule;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemTubeModule;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;
import java.util.Objects;

import static me.desht.pneumaticcraft.common.block.BlockPressureTube.CORE_MAX;
import static me.desht.pneumaticcraft.common.block.BlockPressureTube.CORE_MIN;

public abstract class TubeModule {
    public static final float MAX_VALUE = 30;
    private final ItemTubeModule item;

    protected TileEntityPressureTube pressureTube;
    protected Direction dir = Direction.UP;
    private final VoxelShape[] boundingBoxes;
    protected boolean upgraded;
    public float lowerBound = 4.9F, higherBound = 0;
    private boolean fake;
    public boolean advancedConfig;
    public boolean shouldDrop;

    public TubeModule(ItemTubeModule item) {
        this.item = item;

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

    public void setTube(TileEntityPressureTube pressureTube) {
        this.pressureTube = pressureTube;
    }

    public TileEntityPressureTube getTube() {
        return pressureTube;
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
            if (upgraded) drops.add(new ItemStack(ModItems.ADVANCED_PCB.get()));
        }
        return drops;
    }

    public Item getItem() {
        return item;
    }

    public void setDirection(Direction dir) {
        this.dir = dir;
    }

    public Direction getDirection() {
        return dir;
    }

    public void readFromNBT(CompoundTag nbt) {
        dir = Direction.from3DDataValue(nbt.getInt("dir"));
        upgraded = nbt.getBoolean("upgraded");
        lowerBound = nbt.getFloat("lowerBound");
        higherBound = nbt.getFloat("higherBound");
        advancedConfig = !nbt.contains("advancedConfig") || nbt.getBoolean("advancedConfig");
    }

    public CompoundTag writeToNBT(CompoundTag nbt) {
        nbt.putInt("dir", dir.get3DDataValue());
        nbt.putBoolean("upgraded", upgraded);
        nbt.putFloat("lowerBound", lowerBound);
        nbt.putFloat("higherBound", higherBound);
        nbt.putBoolean("advancedConfig", advancedConfig);
        return nbt;
    }

    protected void tickCommon() {
    }

    public void tickClient() {
        tickCommon();
    }

    public void tickServer() {
        tickCommon();
    }

    public void onNeighborTileUpdate() {
    }

    public void onNeighborBlockUpdate() {
    }

    public final ResourceLocation getType() {
        return item.getRegistryName();
    }

    public int getRedstoneLevel() {
        return 0;
    }

    public void updateNeighbors() {
        pressureTube.getLevel().updateNeighborsAt(pressureTube.getBlockPos(), pressureTube.getLevel().getBlockState(pressureTube.getBlockPos()).getBlock());
    }

    public boolean isInline() {
        return false;
    }

    public void sendDescriptionPacket() {
        pressureTube.sendDescriptionPacket();
    }

    public void addInfo(List<Component> curInfo) {
        if (upgraded) {
            ItemStack stack = new ItemStack(ModItems.ADVANCED_PCB.get());
            curInfo.add(stack.getHoverName().copy().append(" installed").withStyle(ChatFormatting.GREEN));
        }
        if (this instanceof INetworkedModule) {
            int colorChannel = ((INetworkedModule) this).getColorChannel();
            String key = "color.minecraft." + DyeColor.byId(colorChannel);
            curInfo.add(new TranslatableComponent("pneumaticcraft.waila.logisticsModule.channel").append(" ")
                    .append(new TranslatableComponent(key).withStyle(ChatFormatting.YELLOW)));
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

    public boolean onActivated(Player player, InteractionHand hand) {
        if (player.level.isClientSide && hasGui()) {
            GuiTubeModule.openGuiForModule(this);
        }
        return true;
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
        if (!(o instanceof TubeModule)) return false;
        TubeModule that = (TubeModule) o;
        return Objects.equals(pressureTube.getBlockPos(), that.pressureTube.getBlockPos()) && dir == that.dir;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pressureTube.getBlockPos(), dir);
    }

    @SuppressWarnings("EmptyMethod")
    public void onPlaced() {
    }

    public void onRemoved() {
    }
}
