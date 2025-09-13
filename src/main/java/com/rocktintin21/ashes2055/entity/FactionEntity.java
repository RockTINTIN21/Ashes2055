package com.rocktintin21.ashes2055.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.level.Level;

public abstract class FactionEntity extends PathfinderMob implements RangedAttackMob {
    private final Faction faction;

    protected FactionEntity(EntityType<? extends PathfinderMob> type, Level level, Faction faction) {
        super(type, level);
        this.faction = faction;
    }

    public Faction getFaction() {
        return this.faction;
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        if (other instanceof FactionEntity fe) {
            return fe.getFaction() == this.getFaction();
        }
        return super.isAlliedTo(other);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (target instanceof FactionEntity fe) {
            return fe.getFaction() != this.getFaction();
        }
        return super.canAttack(target);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker instanceof FactionEntity fe && fe.getFaction() == this.getFaction()) {
            return false; // no friendly fire
        }
        return super.hurt(source, amount);
    }
}
