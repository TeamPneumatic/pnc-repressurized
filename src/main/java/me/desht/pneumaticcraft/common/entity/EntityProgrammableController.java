package me.desht.pneumaticcraft.common.entity;

import me.desht.pneumaticcraft.common.entity.living.EntityDroneBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammableController;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

/**
 * Special client-only entity used for rendering the programmable controller's "minidrone".
 */
public class EntityProgrammableController extends EntityDroneBase {
    private TileEntityProgrammableController controller;
    private float propSpeed = 0f;

    public EntityProgrammableController(EntityType<EntityProgrammableController> type, World world) {
        super(type, world);

        this.blocksBuilding = false;
    }

    public void setController(TileEntityProgrammableController controller) {
        this.controller = controller;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void tick() {
        if (level.isClientSide && controller != null) {
            TileEntity te = level.getBlockEntity(controller.getBlockPos());
            if (te != controller) {
                // expire stale minidrones
                remove();
            } else {
                if (controller.isIdle) {
                    propSpeed = Math.max(0, propSpeed - 0.04F);
                } else {
                    propSpeed = Math.min(1, propSpeed + 0.04F);
                }
                oldPropRotation = propRotation;
                propRotation += propSpeed;
            }
        }
    }

    @Override
    public double getLaserOffsetY() {
        return 0.45;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public BlockPos getDugBlock() {
        return controller == null ? null : controller.getDugPosition();
    }

    @Override
    public ItemStack getDroneHeldItem() {
        return controller == null ? ItemStack.EMPTY : controller.getFakePlayer().getMainHandItem();
    }

    @Override
    public BlockPos getTargetedBlock() {
        return controller.getTargetPos();
    }

    @Override
    public ITextComponent getOwnerName() {
        return new StringTextComponent(controller.ownerNameClient);
    }

    @Override
    public String getLabel() {
        return controller.label == null ? "<?>" : controller.label;
    }

    @Override
    public boolean isTeleportRangeLimited() {
        return true;  // not very relevant since the PC minidrone doesn't need to teleport anyway
    }

    public BlockPos getControllerPos() {
        return controller.getBlockPos();
    }

    public TileEntityProgrammableController getController() {
        return controller;
    }
}
