package com.ashes2055.entity.projectile;

import com.ashes2055.entity.ModEntities;
import com.ashes2055.sound.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

public class BulletEntity extends Projectile {

    public static final float SPEED = 7.5F;
    private static final int LIFE_TICKS = 200;
    private float damage = 4.0F;
    private SoundEvent hitSound = ModSounds.FLESH_HIT.get();

    private static final String FLESH_HIT_CD_TAG = "ashes2055_flesh_hit_cd";
    private static final int FLESH_HIT_COOLDOWN_TICKS = 12; // ~0.6 c при 20 TPS

    public BulletEntity(EntityType<? extends BulletEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    /** Удобный конструктор — создаём пулю от стрелка у его глаз. */
    public BulletEntity(Level level, LivingEntity shooter, double x, double y, double z) {
        this(ModEntities.BULLET.get(), level);
        this.setOwner(shooter);
        this.setPos(x, y, z);
    }

    public BulletEntity setDamage(float dmg) {
        this.damage = dmg;
        return this;
    }

    public BulletEntity setHitSound(SoundEvent sound) {
        this.hitSound = sound;
        return this;
    }

    @Override
    public void tick() {
        super.tick();

        // трассируем на пути текущего вектора
        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hit.getType() != HitResult.Type.MISS) {
            this.onHit(hit);
        }

        // линейный полёт без гравитации/затухания
        Vec3 motion = this.getDeltaMovement();
        this.move(MoverType.SELF, motion);

        if (this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
        }

        if (this.tickCount > LIFE_TICKS) this.discard();
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && entity != this.getOwner();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        // Звук при попадании в блок (если нужен другой — задайте свой, иначе можно убрать вовсе)
        if (!this.level().isClientSide && result instanceof BlockHitResult) {
            // Например, рикошет/металл и т.п. Или просто ничего не проигрывать.
            // this.level().playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.RICOCHET.get(), SoundSource.HOSTILE, 0.6F, 1.0F);
            this.discard();
            return;
        }

        if (result instanceof EntityHitResult ehr) {
            this.onHitEntity(ehr);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult ehr) {
        Entity target = ehr.getEntity();
        Entity owner  = this.getOwner();

        if (!this.level().isClientSide && target.isAlive()) {
            // Урон
            target.hurt(this.level().damageSources().mobProjectile(this, owner instanceof LivingEntity le ? le : null), this.damage);

            // ===== Анти-спам Flesh Hit (пер-цели) =====
            long now = this.level().getGameTime();
            CompoundTag pdata = target.getPersistentData();
            long nextAllowed = pdata.getLong(FLESH_HIT_CD_TAG);

            if (now >= nextAllowed) {
                this.level().playSound(
                        null,
                        target.getX(), target.getY(), target.getZ(),
                        this.hitSound != null ? this.hitSound : ModSounds.FLESH_HIT.get(),
                        SoundSource.HOSTILE,
                        0.9F, 1.0F
                );
                // Обновляем «окно» на будущее проигрывание
                pdata.putLong(FLESH_HIT_CD_TAG, now + FLESH_HIT_COOLDOWN_TICKS);
            }
            // ==========================================

            // Доп. логика — уведомление владельца при смерти цели
            if (target instanceof LivingEntity living && (living.isDeadOrDying() || living.getHealth() <= 0f)) {
                if (owner instanceof com.ashes2055.entity.RaiderEntity raider) {
                    raider.onEnemyDown();
                }
            }
        }
        this.discard();
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Damage", this.damage);
        // корректная запись id звука
        ResourceLocation key = ForgeRegistries.SOUND_EVENTS.getKey(this.hitSound);
        if (key != null) tag.putString("HitSound", key.toString());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Damage")) this.damage = tag.getFloat("Damage");
        if (tag.contains("HitSound")) {
            ResourceLocation id = ResourceLocation.tryParse(tag.getString("HitSound"));
            if (id != null) {
                SoundEvent se = ForgeRegistries.SOUND_EVENTS.getValue(id);
                if (se != null) this.hitSound = se;
            }
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
