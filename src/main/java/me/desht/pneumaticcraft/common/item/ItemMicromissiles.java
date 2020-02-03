package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.client.gui.GuiMicromissile;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.config.aux.MicromissileDefaults;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.entity.projectile.EntityMicromissile;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemMicromissiles extends Item {
    public static final String NBT_TOP_SPEED = "topSpeed";
    public static final String NBT_TURN_SPEED = "turnSpeed";
    public static final String NBT_DAMAGE = "damage";
    public static final String NBT_FILTER = "filter";
    public static final String NBT_PX = "px";
    public static final String NBT_PY = "py";
    public static final String NBT_FIRE_MODE = "fireMode";

    public enum FireMode {
        SMART, DUMB;

        public static FireMode fromString(String mode) {
            try {
                return FireMode.valueOf(mode);
            } catch (IllegalArgumentException e) {
                return SMART;
            }
        }
    }

    public ItemMicromissiles() {
        super(ModItems.defaultProps().maxStackSize(1));
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return PNCConfig.Common.Micromissiles.missilePodSize;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);

        if (playerIn.isSneaking()) {
            if (worldIn.isRemote) {
                GuiMicromissile.openGui(stack.getDisplayName(), handIn);
            }
            return ActionResult.newResult(ActionResultType.SUCCESS, stack);
        }

        EntityMicromissile missile = new EntityMicromissile(worldIn, playerIn, stack);
        Vec3d directionVec = playerIn.getLookVec().normalize();
        missile.posX += directionVec.x;
        missile.posY += directionVec.y + 0.1;
        missile.posZ += directionVec.z;
        missile.shoot(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, getInitialVelocity(stack), 0.0F);

        playerIn.getCooldownTracker().setCooldown(this, PNCConfig.Common.Micromissiles.launchCooldown);

        if (!worldIn.isRemote) {
            RayTraceResult res = PneumaticCraftUtils.getMouseOverServer(playerIn, 100);
            if (res instanceof EntityRayTraceResult) {
                EntityRayTraceResult ertr = (EntityRayTraceResult) res;
                if (missile.isValidTarget(ertr.getEntity())) {
                    missile.setTarget(ertr.getEntity());
                }
            }
            worldIn.addEntity(missile);
        }

        if (!playerIn.isCreative()) {
            stack.damageItem(1, playerIn, playerEntity -> { });
        }
        return ActionResult.newResult(ActionResultType.SUCCESS, stack);
    }

    private float getInitialVelocity(ItemStack stack) {
        if (stack.hasTag()) {
            CompoundNBT tag = stack.getTag();
            FireMode fireMode = FireMode.fromString(tag.getString(NBT_FIRE_MODE));
            if (fireMode == FireMode.SMART) {
                return Math.max(0.2f, tag.getFloat(NBT_TOP_SPEED) / 2f);
            } else {
                return 1/3f;
            }
        } else {
            return 1/3f;
        }
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> curInfo, ITooltipFlag extraInfo) {
        super.addInformation(stack, worldIn, curInfo, extraInfo);

        curInfo.add(xlate("gui.micromissile.remaining", stack.getMaxDamage() - stack.getDamage() + 1));
        if (stack.hasTag()) {
            CompoundNBT tag = stack.getTag();
            // padding for ClientEventHandler#renderTooltipEvent() to draw in
            curInfo.add(new StringTextComponent(" "));
            curInfo.add(new StringTextComponent(" "));
            curInfo.add(new StringTextComponent(" "));
            String filter = tag.getString(NBT_FILTER);
            if (!filter.isEmpty()) {
                curInfo.add(xlate("gui.sentryTurret.targetFilter", filter));
            }
            curInfo.add(xlate("gui.micromissile.firingMode")
                    .appendText(": " + TextFormatting.AQUA)
                    .appendSibling(xlate("gui.micromissile.mode." + tag.getString(NBT_FIRE_MODE))));
            if (PNCConfig.Common.Micromissiles.damageTerrain) {
                curInfo.add(xlate("gui.tooltip.terrainWarning"));
            } else {
                curInfo.add(xlate("gui.tooltip.terrainSafe"));
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (!stack.hasTag() && entityIn instanceof PlayerEntity) {
            MicromissileDefaults.Entry def = MicromissileDefaults.INSTANCE.getDefaults((PlayerEntity) entityIn);
            if (def != null) {
                stack.setTag(def.toNBT());
            }
        }
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
    }
}
