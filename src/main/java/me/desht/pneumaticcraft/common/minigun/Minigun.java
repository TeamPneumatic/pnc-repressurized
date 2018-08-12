package me.desht.pneumaticcraft.common.minigun;

import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.client.render.RenderProgressingLine;
import me.desht.pneumaticcraft.client.sound.MovingSounds;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.item.ItemGunAmmo;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlayMovingSound;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Sounds;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

public abstract class Minigun {
    public static final double MAX_GUN_SPEED = 0.4;
    private static final double RAYTRACE_RANGE = 50;
    private static final double MAX_GUN_YAW_CHANGE = 10;
    private static final double MAX_GUN_PITCH_CHANGE = 10;

    private final boolean requiresTarget;

    private double minigunSpeed;
    private int minigunTriggerTimeOut;
    private int minigunSoundCounter = -1;
    private final Random rand = new Random();
    private double minigunRotation, oldMinigunRotation;
    public double minigunYaw, oldMinigunYaw;
    public double minigunPitch, oldMinigunPitch;
    private final RenderProgressingLine minigunFire = new RenderProgressingLine().setProgress(1);
    private boolean sweeping; //When true, the yaw of the minigun will sweep with a sinus pattern when not targeting.
    private double sweepingProgress;

    private boolean gunAimedAtTarget;

    private IPressurizable pressurizable;
    private int airUsage;
    protected ItemStack stack = ItemStack.EMPTY, ammo = ItemStack.EMPTY;
    protected EntityPlayer player;
    protected World world;
    private EntityLivingBase attackTarget;

    public Minigun(boolean requiresTarget) {
        this.requiresTarget = requiresTarget;
    }

    public Minigun setPressurizable(IPressurizable pressurizable, int airUsage) {
        this.pressurizable = pressurizable;
        this.airUsage = airUsage;
        return this;
    }

    public Minigun setItemStack(@Nonnull ItemStack stack) {
        this.stack = stack;
        return this;
    }

    public Minigun setAmmo(@Nonnull ItemStack ammo) {
        this.ammo = ammo;
        return this;
    }

    public Minigun setPlayer(EntityPlayer player) {
        this.player = player;
        return this;
    }

    public Minigun setWorld(World world) {
        this.world = world;
        return this;
    }

    public Minigun setAttackTarget(EntityLivingBase entity) {
        attackTarget = entity;
        return this;
    }

    public abstract boolean isMinigunActivated();

    public abstract void setMinigunActivated(boolean activated);

    public abstract void setAmmoColorStack(@Nonnull ItemStack ammo);

    public abstract int getAmmoColor();

    public abstract void playSound(SoundEvent soundName, float volume, float pitch);

    protected int getAmmoColor(@Nonnull ItemStack stack) {
        return stack.isEmpty() ? 0xFF313131 : Minecraft.getMinecraft().getItemColors().colorMultiplier(stack, 1);
    }

    /**
     * Get the source for this sound, where the client should play the sound loop at.  Can be an Entity, a
     * TileEntity, or a BlockPos; anything else will cause an exception to be thrown.
     *
     * @return
     */
    public Object getSoundSource() {
        return player;
    }

    public double getMinigunSpeed() {
        return minigunSpeed;
    }

    public void setMinigunSpeed(double minigunSpeed) {
        this.minigunSpeed = minigunSpeed;
    }

    public int getMinigunTriggerTimeOut() {
        return minigunTriggerTimeOut;
    }

    public void setMinigunTriggerTimeOut(int minigunTriggerTimeOut) {
        this.minigunTriggerTimeOut = minigunTriggerTimeOut;
    }

    public int getMinigunSoundCounter() {
        return minigunSoundCounter;
    }

    public void setMinigunSoundCounter(int minigunSoundCounter) {
        this.minigunSoundCounter = minigunSoundCounter;
    }

    public double getMinigunRotation() {
        return minigunRotation;
    }

    public void setMinigunRotation(double minigunRotation) {
        this.minigunRotation = minigunRotation;
    }

    public double getOldMinigunRotation() {
        return oldMinigunRotation;
    }

    public void setOldMinigunRotation(double oldMinigunRotation) {
        this.oldMinigunRotation = oldMinigunRotation;
    }

    public EntityLivingBase getAttackTarget() {
        return attackTarget;
    }

    public void setSweeping(boolean sweeping) {
        this.sweeping = sweeping;
    }

    public boolean isSweeping() {
        return sweeping;
    }

    public boolean tryFireMinigun(EntityLivingBase target) {
        boolean lastShotOfAmmo = false;
        if (!ammo.isEmpty() && (pressurizable == null || pressurizable.getPressure(stack) > 0)) {
            setMinigunTriggerTimeOut(Math.max(10, getMinigunSoundCounter()));
            if (getMinigunSpeed() == MAX_GUN_SPEED && (!requiresTarget || gunAimedAtTarget)) {
                RayTraceResult rtr = null;
                if (!requiresTarget) {
                    rtr = raytraceTarget();
                    target = rtr.entityHit instanceof EntityLivingBase ? (EntityLivingBase) rtr.entityHit : null;
                }
                int ammoCost = 1;
                if (pressurizable != null) pressurizable.addAir(stack, -airUsage);
                if (target != null) {
                    ItemStack potion = ItemGunAmmo.getPotion(ammo);
                    if (!potion.isEmpty()) {
                        if (rand.nextInt(ConfigHandler.general.minigunPotionProcChance) == 0) {
                            if (potion.getItem() == Items.POTIONITEM) {
                                List<PotionEffect> effects = PotionUtils.getEffectsFromStack(potion);
                                for (PotionEffect effect : effects) {
                                    target.addPotionEffect(new PotionEffect(effect));
                                }
                            } else if (potion.getItem() == Items.SPLASH_POTION || potion.getItem() == Items.LINGERING_POTION) {
                                EntityPotion entityPotion = new EntityPotion(world, player, potion);
                                entityPotion.setPosition(target.posX, target.posY, target.posZ);
                                world.spawnEntity(entityPotion);
                            }
                        }
                    } else {
                        target.attackEntityFrom(DamageSource.causePlayerDamage(player), ConfigHandler.general.configMinigunDamage);
                    }
                } else if (rtr != null && rtr.typeOfHit == RayTraceResult.Type.BLOCK) {
                    ItemStack potion = ItemGunAmmo.getPotion(ammo);
                    if (potion.getItem() == Items.SPLASH_POTION || potion.getItem() == Items.LINGERING_POTION) {
                        ammoCost = potion.getItem() == Items.SPLASH_POTION ? 3 : 6;
                        if (rand.nextInt(ConfigHandler.general.minigunPotionProcChance) == 0) {
                            EntityPotion entityPotion = new EntityPotion(world, player, potion);
                            BlockPos pos = rtr.getBlockPos();
                            entityPotion.setPosition(pos.getX(), pos.getY(), pos.getZ());
                            world.spawnEntity(entityPotion);
                        }
                    }
                }
                lastShotOfAmmo = ammo.attemptDamageItem(ammoCost, rand, player instanceof EntityPlayerMP ? (EntityPlayerMP) player : null);
            }
        }
        return lastShotOfAmmo;
    }

    private RayTraceResult raytraceTarget() {
        RayTraceResult mop = PneumaticCraftUtils.getMouseOverServer(player, RAYTRACE_RANGE);
        if (mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK && this.world instanceof WorldServer) {
            BlockPos pos = mop.getBlockPos();
            double x = pos.getX() + mop.sideHit.getFrontOffsetX();
            double y = pos.getY() + mop.sideHit.getFrontOffsetY();
            double z = pos.getZ() + mop.sideHit.getFrontOffsetZ();
            IBlockState state = world.getBlockState(pos);
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_DUST, x, y, z, 25, 0.0D, 0.5D, 0.0D, 0.15, Block.getStateId(state));
        }
        return mop;
    }

    public void update(double posX, double posY, double posZ) {
        setOldMinigunRotation(getMinigunRotation());
        oldMinigunYaw = minigunYaw;
        oldMinigunPitch = minigunPitch;
        if (attackTarget != null && attackTarget.isDead) attackTarget = null;
        if (!world.isRemote) {
            setMinigunActivated(getMinigunTriggerTimeOut() > 0);

            setAmmoColorStack(ammo);

            if (getMinigunTriggerTimeOut() > 0) {
                setMinigunTriggerTimeOut(getMinigunTriggerTimeOut() - 1);
                if (getMinigunSpeed() == 0) {
                    playSound(Sounds.HUD_INIT, 3, 0.9F);
                }
            }
            if (getMinigunSoundCounter() == 0 && getMinigunTriggerTimeOut() == 0) {
                playSound(Sounds.MINIGUN_STOP, 3, 0.5F);
                setMinigunSoundCounter(-1);
            }
        }
        if (isMinigunActivated()) {
            double lastSpeed = getMinigunSpeed();
            setMinigunSpeed(Math.min(getMinigunSpeed() + 0.01D, MAX_GUN_SPEED));
            if (getMinigunSpeed() > lastSpeed && getMinigunSpeed() >= MAX_GUN_SPEED && !world.isRemote) {
                NetworkHandler.sendToDimension(new PacketPlayMovingSound(MovingSounds.Sound.MINIGUN, getSoundSource()), world.provider.getDimension());
            }
        } else {
            setMinigunSpeed(Math.max(0, getMinigunSpeed() - 0.003D));
        }

        setMinigunRotation(getMinigunRotation() + getMinigunSpeed());

        double targetYaw = 0;
        double targetPitch = 0;
        if (attackTarget != null) {
            double deltaX = posX - attackTarget.posX;
            double deltaZ = posZ - attackTarget.posZ;

            if (deltaX >= 0 && deltaZ < 0) {
                targetYaw = Math.atan(Math.abs(deltaX / deltaZ)) / Math.PI * 180D;
            } else if (deltaX >= 0 && deltaZ >= 0) {
                targetYaw = Math.atan(Math.abs(deltaZ / deltaX)) / Math.PI * 180D + 90;
            } else if (deltaX < 0 && deltaZ >= 0) {
                targetYaw = Math.atan(Math.abs(deltaX / deltaZ)) / Math.PI * 180D + 180;
            } else {
                targetYaw = Math.atan(Math.abs(deltaZ / deltaX)) / Math.PI * 180D + 270;
            }
            if (targetYaw - minigunYaw > 180) {
                targetYaw -= 360;
            } else if (minigunYaw - targetYaw > 180) {
                targetYaw += 360;
            }
            targetPitch = Math.toDegrees(Math.atan((posY - attackTarget.posY - attackTarget.height / 2) / PneumaticCraftUtils.distBetween(posX, posZ, attackTarget.posX, attackTarget.posZ)));

            if (minigunPitch > targetPitch) {
                if (minigunPitch - MAX_GUN_PITCH_CHANGE > targetPitch) {
                    minigunPitch -= MAX_GUN_PITCH_CHANGE;
                } else {
                    minigunPitch = targetPitch;
                }
            } else {
                if (minigunPitch + MAX_GUN_PITCH_CHANGE < targetPitch) {
                    minigunPitch += MAX_GUN_PITCH_CHANGE;
                } else {
                    minigunPitch = targetPitch;
                }
            }

            if (minigunPitch < -80 || minigunPitch > 80) {
                minigunYaw = targetYaw;
            } else {
                if (minigunYaw > targetYaw) {
                    if (minigunYaw - MAX_GUN_YAW_CHANGE > targetYaw) {
                        minigunYaw -= MAX_GUN_YAW_CHANGE;
                    } else {
                        minigunYaw = targetYaw;
                    }
                } else {
                    if (minigunYaw + MAX_GUN_YAW_CHANGE < targetYaw) {
                        minigunYaw += MAX_GUN_YAW_CHANGE;
                    } else {
                        minigunYaw = targetYaw;
                    }
                }
            }
            gunAimedAtTarget = minigunYaw == targetYaw && minigunPitch == targetPitch;
        } else if (isSweeping()) {
            minigunYaw -= Math.cos(sweepingProgress) * 22;
            sweepingProgress += 0.05D;
            minigunYaw += Math.cos(sweepingProgress) * 22;

            if (minigunPitch > targetPitch) {
                if (minigunPitch - MAX_GUN_PITCH_CHANGE > targetPitch) {
                    minigunPitch -= MAX_GUN_PITCH_CHANGE;
                } else {
                    minigunPitch = targetPitch;
                }
            } else {
                if (minigunPitch + MAX_GUN_PITCH_CHANGE < targetPitch) {
                    minigunPitch += MAX_GUN_PITCH_CHANGE;
                } else {
                    minigunPitch = targetPitch;
                }
            }
        }

        if (!world.isRemote && isMinigunActivated() && getMinigunSpeed() == MAX_GUN_SPEED && (!requiresTarget || gunAimedAtTarget && attackTarget != null)) {
            if (getMinigunSoundCounter() <= 0) {
                setMinigunSoundCounter(20);
            }
        }
        if (getMinigunSoundCounter() > 0) setMinigunSoundCounter(getMinigunSoundCounter() - 1);
    }

    @SideOnly(Side.CLIENT)
    public void render(double x, double y, double z, double gunRadius) {
        if (isMinigunActivated() && getMinigunSpeed() == MAX_GUN_SPEED && gunAimedAtTarget && attackTarget != null) {
            GlStateManager.pushMatrix();
            GlStateManager.scale(1, 1, 1);
            GlStateManager.translate(-x, -y, -z);
            GlStateManager.disableTexture2D();
            GL11.glEnable(GL11.GL_LINE_STIPPLE);
//            GlStateManager.disableLighting();
            RenderUtils.glColorHex(0xFF000000 | getAmmoColor());
            for (int i = 0; i < 5; i++) {
                int stipple = 0xFFFF & ~(2 << rand.nextInt(16));
                GL11.glLineStipple(4, (short) stipple);
                Vec3d vec = new Vec3d(attackTarget.posX - x, attackTarget.posY - y, attackTarget.posZ - z).normalize();
                minigunFire.startX = x + vec.x * gunRadius;
                minigunFire.startY = y + vec.y * gunRadius;
                minigunFire.startZ = z + vec.z * gunRadius;
                minigunFire.endX = attackTarget.posX + rand.nextDouble() - 0.5;
                minigunFire.endY = attackTarget.posY + attackTarget.height / 2 + rand.nextDouble() - 0.5;
                minigunFire.endZ = attackTarget.posZ + rand.nextDouble() - 0.5;
                minigunFire.render();
            }
            GlStateManager.color(1, 1, 1, 1);
            // GlStateManager.enabledLighting();
            GL11.glDisable(GL11.GL_LINE_STIPPLE);
            GlStateManager.enableTexture2D();
            GlStateManager.popMatrix();
        }
    }
}
