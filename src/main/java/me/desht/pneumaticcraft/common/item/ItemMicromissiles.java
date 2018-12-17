package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.GuiHandler;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.config.MicromissileDefaults;
import me.desht.pneumaticcraft.common.entity.projectile.EntityMicromissile;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemMicromissiles extends ItemPneumatic {
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
        super("micromissiles");
        setMaxDamage(ConfigHandler.microMissile.missilePodSize - 1);  // -1 because of counting from 0
        setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack iStack = playerIn.getHeldItem(handIn);

        if (playerIn.isSneaking()) {
            playerIn.openGui(PneumaticCraftRepressurized.instance, GuiHandler.EnumGuiId.MICROMISSILE.ordinal(),
                    worldIn, (int)playerIn.posX, (int)playerIn.posY, (int)playerIn.posZ);
            return ActionResult.newResult(EnumActionResult.SUCCESS, iStack);
        }

        EntityMicromissile missile = new EntityMicromissile(worldIn, playerIn, iStack);
        Vec3d directionVec = playerIn.getLookVec().normalize();
        missile.posX += directionVec.x;
        missile.posY += directionVec.y + 0.1;
        missile.posZ += directionVec.z;
        missile.shoot(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, getInitialVelocity(iStack), 0.0F);

        playerIn.getCooldownTracker().setCooldown(this, ConfigHandler.microMissile.launchCooldown);

        if (!worldIn.isRemote) {
            RayTraceResult res = PneumaticCraftUtils.getMouseOverServer(playerIn, 100);
            if (res.typeOfHit == RayTraceResult.Type.ENTITY && missile.isValidTarget(res.entityHit)) {
                missile.setTarget(res.entityHit);
            }
            worldIn.spawnEntity(missile);
        }

        if (!playerIn.capabilities.isCreativeMode) {
            iStack.damageItem(1, playerIn);
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, iStack);
    }

    private float getInitialVelocity(ItemStack stack) {
        if (stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();
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
    public void addInformation(ItemStack stack, World worldIn, List<String> curInfo, ITooltipFlag extraInfo) {
        super.addInformation(stack, worldIn, curInfo, extraInfo);

        curInfo.add(I18n.format("gui.micromissile.remaining") + ": " + TextFormatting.AQUA + (stack.getMaxDamage() - stack.getItemDamage() + 1));
        if (stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();
            // padding for ClientEventHandler#renderTooltipEvent() to draw in
            curInfo.add(" ");
            curInfo.add(" ");
            curInfo.add(" ");
            String filter = tag.getString(NBT_FILTER);
            if (!filter.isEmpty()) {
                curInfo.add(I18n.format("gui.sentryTurret.targetFilter") + ": " + TextFormatting.AQUA + filter);
            }
            curInfo.add(I18n.format("gui.micromissile.firingMode") + ": "+ TextFormatting.AQUA + I18n.format("gui.micromissile.mode." + tag.getString(NBT_FIRE_MODE)));
            if (ConfigHandler.microMissile.damageTerrain) {
                curInfo.add(I18n.format("gui.tooltip.terrainWarning"));
            } else {
                curInfo.add(I18n.format("gui.tooltip.terrainSafe"));
            }
        }
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (!stack.hasTagCompound() && entityIn instanceof EntityPlayer) {
            MicromissileDefaults.Entry def = MicromissileDefaults.INSTANCE.getDefaults((EntityPlayer) entityIn);
            if (def != null) {
                stack.setTagCompound(def.toNBT());
            }
        }
        super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
    }

    public static ItemStack getHeldMicroMissile(EntityPlayer player) {
        ItemStack stack = player.getHeldItemMainhand();
        if (stack.getItem() == Itemss.MICROMISSILES) {
            return stack;
        } else {
            stack = player.getHeldItemOffhand();
            if (stack.getItem() == Itemss.MICROMISSILES) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
