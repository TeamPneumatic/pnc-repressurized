package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.client.gui.GuiMicromissile;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.config.subconfig.MicromissileDefaults;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.entity.projectile.EntityMicromissile;
import me.desht.pneumaticcraft.common.util.ITranslatableEnum;
import me.desht.pneumaticcraft.common.util.RayTraceUtils;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Locale;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemMicromissiles extends Item {
    public static final String NBT_TOP_SPEED = "topSpeed";
    public static final String NBT_TURN_SPEED = "turnSpeed";
    public static final String NBT_DAMAGE = "damage";
    public static final String NBT_FILTER = "filter";
    public static final String NBT_PX = "px";
    public static final String NBT_PY = "py";
    public static final String NBT_FIRE_MODE = "fireMode";

    public enum FireMode implements ITranslatableEnum {
        SMART, DUMB;

        public static FireMode fromString(String mode) {
            try {
                return FireMode.valueOf(mode);
            } catch (IllegalArgumentException e) {
                return SMART;
            }
        }

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.gui.micromissile.mode." + this.toString().toLowerCase(Locale.ROOT);
        }
    }

    public ItemMicromissiles() {
        super(ModItems.defaultProps().stacksTo(1).defaultDurability(100));
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return PNCConfig.Common.Micromissiles.missilePodSize;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return toRepair.getItem() == this && repair.getItem() == Blocks.TNT.asItem();
    }

    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);

        if (playerIn.isShiftKeyDown()) {
            if (worldIn.isClientSide) {
                GuiMicromissile.openGui(stack.getHoverName(), handIn);
            }
            return ActionResult.success(stack);
        }

        EntityMicromissile missile = new EntityMicromissile(worldIn, playerIn, stack);
        Vector3d newPos = missile.position().add(playerIn.getLookAngle().normalize());
        missile.setPos(newPos.x, newPos.y, newPos.z);
        missile.shootFromRotation(playerIn, playerIn.xRot, playerIn.yRot, 0.0F, getInitialVelocity(stack), 0.0F);

        playerIn.getCooldowns().addCooldown(this, PNCConfig.Common.Micromissiles.launchCooldown);

        if (!worldIn.isClientSide) {
            RayTraceResult res = RayTraceUtils.getMouseOverServer(playerIn, 100);
            if (res instanceof EntityRayTraceResult) {
                EntityRayTraceResult ertr = (EntityRayTraceResult) res;
                if (missile.isValidTarget(ertr.getEntity())) {
                    missile.setTarget(ertr.getEntity());
                }
            }
            worldIn.addFreshEntity(missile);
        }

        if (!playerIn.isCreative()) {
            stack.hurtAndBreak(1, playerIn, playerEntity -> { });
        }
        return ActionResult.success(stack);
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
    public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> curInfo, ITooltipFlag extraInfo) {
        super.appendHoverText(stack, worldIn, curInfo, extraInfo);

        curInfo.add(xlate("pneumaticcraft.gui.micromissile.remaining")
                .append(new StringTextComponent(Integer.toString(stack.getMaxDamage() - stack.getDamageValue())).withStyle(TextFormatting.AQUA))
        );
        if (stack.hasTag()) {
            FireMode mode = getFireMode(stack);
            if (mode == FireMode.SMART) {
                CompoundNBT tag = stack.getTag();
                curInfo.add(xlate("pneumaticcraft.gui.micromissile.topSpeed"));
                curInfo.add(xlate("pneumaticcraft.gui.micromissile.turnSpeed"));
                curInfo.add(xlate("pneumaticcraft.gui.micromissile.damage"));
                String filter = tag.getString(NBT_FILTER);
                if (!filter.isEmpty()) {
                    curInfo.add(xlate("pneumaticcraft.gui.sentryTurret.targetFilter")
                            .append(": ")
                            .append(TextFormatting.AQUA + filter));
                }
            }
            curInfo.add(xlate("pneumaticcraft.gui.micromissile.firingMode")
                    .append(": ")
                    .append(xlate(mode.getTranslationKey()).withStyle(TextFormatting.AQUA)));
            if (PNCConfig.Common.Micromissiles.damageTerrain) {
                curInfo.add(xlate("pneumaticcraft.gui.tooltip.terrainWarning"));
            } else {
                curInfo.add(xlate("pneumaticcraft.gui.tooltip.terrainSafe"));
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

    public static FireMode getFireMode(ItemStack stack) {
        Validate.isTrue(stack.getItem() instanceof ItemMicromissiles);
        return stack.hasTag() ? FireMode.fromString(stack.getTag().getString(ItemMicromissiles.NBT_FIRE_MODE)) : FireMode.SMART;
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID)
    public static class Listener {
        @SubscribeEvent
        public static void onMissilesRepair(AnvilRepairEvent event) {
            // allow repeated repairing without XP costs spiralling
            if (event.getItemResult().getItem() instanceof ItemMicromissiles && event.getItemResult().hasTag()) {
                event.getItemResult().setRepairCost(0);
            }
        }
    }
}
