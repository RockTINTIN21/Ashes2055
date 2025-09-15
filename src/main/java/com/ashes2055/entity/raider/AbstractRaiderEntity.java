package com.ashes2055.entity.raider;

import com.ashes2055.entity.BulletEntity;
import com.ashes2055.entity.Faction;
import com.ashes2055.entity.FactionMob;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;

import net.minecraftforge.registries.ForgeRegistries;

/**
 * Shared behaviour for all raider gunmen, including shooting, reloading and
 * distance-based gunshot audio.
 */
public abstract class AbstractRaiderEntity extends FactionMob implements RangedAttackMob {
    private final GunProperties gunProperties;
    private int reloadTicks;
    private int ammoInMagazine;
    @Nullable
    private BlockPos anchorPosition;

    protected AbstractRaiderEntity(EntityType<? extends AbstractRaiderEntity> entityType, Level level,
                                   GunProperties gunProperties) {
        super(entityType, level, Faction.RAIDERS);
        this.gunProperties = gunProperties;
        this.ammoInMagazine = gunProperties.magazineSize();
    }

    public GunProperties getGunProperties() {
        return this.gunProperties;
    }

    public int getAttackInterval() {
        return this.gunProperties.attackInterval();
    }

    public boolean isReloading() {
        return this.reloadTicks > 0;
    }

    @Nullable
    public BlockPos getAnchorPosition() {
        return this.anchorPosition;
    }

    protected void setAnchorPosition(BlockPos anchorPosition) {
        this.anchorPosition = anchorPosition;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.anchorPosition == null) {
            this.anchorPosition = this.blockPosition();
        }
        if (this.reloadTicks > 0) {
            this.handleReloadTick();
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (this.level().isClientSide || this.isReloading() || this.ammoInMagazine <= 0) {
            return;
        }

        BulletEntity bullet = new BulletEntity(this.level(), this);
        bullet.setDamage(this.gunProperties.bulletDamage());
        double deltaX = target.getX() - this.getX();
        double deltaY = target.getEyeY() - bullet.getY();
        double deltaZ = target.getZ() - this.getZ();
        bullet.shoot(deltaX, deltaY, deltaZ, this.gunProperties.projectileSpeed(), 0.0F);
        this.level().addFreshEntity(bullet);

        SoundEvent sound = this.gunProperties.sounds().getForDistance(Math.sqrt(this.distanceToSqr(target)));
        if (sound != null) {
            this.level().playSound(null, this, sound, SoundSource.HOSTILE, 1.0F, 1.0F);
        }

        this.ammoInMagazine--;
        if (this.ammoInMagazine <= 0) {
            this.startReload();
        }
    }

    private void startReload() {
        if (this.reloadTicks <= 0) {
            this.reloadTicks = this.gunProperties.reloadTime();
            this.onReloadStage(ReloadStage.MAG_REMOVED);
        }
    }

    private void handleReloadTick() {
        int total = this.gunProperties.reloadTime();
        if (this.reloadTicks == total / 2) {
            this.onReloadStage(ReloadStage.MAG_INSERTED);
        } else if (this.reloadTicks == 1) {
            this.onReloadStage(ReloadStage.BOLT);
        }

        --this.reloadTicks;
        if (this.reloadTicks <= 0) {
            this.ammoInMagazine = this.gunProperties.magazineSize();
            this.onReloadStage(ReloadStage.COMPLETE);
        }
    }

    protected void onReloadStage(ReloadStage stage) {
        switch (stage) {
            case MAG_REMOVED -> this.broadcastAction("магазин вытащен");
            case MAG_INSERTED -> this.broadcastAction("новый магазин вставлен");
            case BOLT -> this.broadcastAction("звук передергивания затвора");
            default -> {
            }
        }
    }

    protected void broadcastAction(String message) {
        if (!this.level().isClientSide && this.level().getServer() != null) {
            this.level().getServer().getPlayerList().broadcastSystemMessage(Component.literal(message), false);
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        SpawnGroupData spawnData, @Nullable CompoundTag data) {
        SpawnGroupData groupData = super.finalizeSpawn(level, difficulty, reason, spawnData, data);
        this.anchorPosition = this.blockPosition();
        RandomSource random = level.getRandom();
        this.populateDefaultEquipmentSlots(random, difficulty);
        this.populateDefaultEquipmentEnchantments(random, difficulty);
        return groupData;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        super.populateDefaultEquipmentSlots(random, difficulty);
        ItemStack weapon = this.gunProperties.createWeaponStack();
        if (!weapon.isEmpty()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, weapon);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Ammo", this.ammoInMagazine);
        tag.putInt("Reload", this.reloadTicks);
        if (this.anchorPosition != null) {
            tag.putInt("AnchorX", this.anchorPosition.getX());
            tag.putInt("AnchorY", this.anchorPosition.getY());
            tag.putInt("AnchorZ", this.anchorPosition.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.ammoInMagazine = Mth.clamp(tag.getInt("Ammo"), 0, this.gunProperties.magazineSize());
        this.reloadTicks = tag.getInt("Reload");
        if (tag.contains("AnchorX") && tag.contains("AnchorY") && tag.contains("AnchorZ")) {
            this.anchorPosition = new BlockPos(tag.getInt("AnchorX"), tag.getInt("AnchorY"), tag.getInt("AnchorZ"));
        }
        if (this.ammoInMagazine == 0 && this.reloadTicks <= 0) {
            this.ammoInMagazine = this.gunProperties.magazineSize();
        }
    }

    protected enum ReloadStage {
        MAG_REMOVED,
        MAG_INSERTED,
        BOLT,
        COMPLETE
    }

    /**
     * Definition of a gun used by the raiders.
     */
    public record GunProperties(ResourceLocation weaponId, float bulletDamage, float projectileSpeed,
                                int magazineSize, int reloadTime, int attackInterval,
                                GunshotSoundProfile sounds) {
        public ItemStack createWeaponStack() {
            Item item = ForgeRegistries.ITEMS.getValue(this.weaponId);
            return item != null ? new ItemStack(item) : ItemStack.EMPTY;
        }
    }

    /**
     * Holds the different gunshot sounds based on distance.
     */
    public record GunshotSoundProfile(@Nullable SoundEvent close, @Nullable SoundEvent medium,
                                      @Nullable SoundEvent far) {
        public SoundEvent getForDistance(double distance) {
            if (distance < 16.0 && this.close != null) {
                return this.close;
            }
            if (distance < 48.0 && this.medium != null) {
                return this.medium;
            }
            if (this.far != null) {
                return this.far;
            }
            return this.medium != null ? this.medium : this.close;
        }

        public static GunshotSoundProfile single(SoundEvent sound) {
            return new GunshotSoundProfile(sound, sound, sound);
        }
    }
}
