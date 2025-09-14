package com.ashes2055.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Base class for all faction mobs. Handles faction logic and friendly fire.
 */
public abstract class FactionMob extends Monster {
    private final Faction faction;

    protected FactionMob(EntityType<? extends Monster> entityType, Level level, Faction faction) {
        super(entityType, level);
        this.faction = faction;
    }

    public Faction getFaction() {
        return faction;
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        return other instanceof FactionMob && ((FactionMob) other).getFaction() == this.faction;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !isAlliedTo(target) && super.canAttack(target);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker instanceof FactionMob && ((FactionMob) attacker).getFaction() == this.faction) {
            // Ignore friendly fire
            return false;
        }
        return super.hurt(source, amount);
    }
}
