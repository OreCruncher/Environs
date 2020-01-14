/*
 *  Dynamic Surroundings: Environs
 *  Copyright (C) 2020  OreCruncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.environs.handlers;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.environs.Config;
import org.orecruncher.environs.effects.emitters.ParticleEmitter;
import org.orecruncher.lib.BlockPosUtil;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class ParticleSystems extends HandlerBase {
    ParticleSystems() {
        super("Particle Systems");
    }

    private static ParticleSystems _instance = null;

    private final Object2ObjectOpenHashMap<BlockPos, ParticleEmitter> systems = new Object2ObjectOpenHashMap<>();

    @Override
    public boolean doTick(final long tick) {
        return !this.systems.isEmpty();
    }

    @Override
    public void process(@Nonnull final PlayerEntity player) {
        final double range = Config.CLIENT.effects.get_effectRange();
        final BlockPos min = CommonState.getPlayerPosition().add(-range, -range, -range);
        final BlockPos max = CommonState.getPlayerPosition().add(range, range, range);

        this.systems.object2ObjectEntrySet().removeIf(entry -> {
            final ParticleEmitter system = entry.getValue();
            if (BlockPosUtil.notContains(system.getPos(), min, max)) {
                system.setExpired();
            } else {
                system.tick();
            }
            return !system.isAlive();
        });
    }

    @Override
    public void onConnect() {
        _instance = this;
        this.systems.clear();
    }

    @Override
    public void onDisconnect() {
        this.systems.clear();
        _instance = null;
    }

    // Determines if it is OK to spawn a particle system at the specified
    // location. Generally only a single system can occupy a block.
    public static boolean okToSpawn(@Nonnull final BlockPos pos) {
        return !_instance.systems.containsKey(pos);
    }

    public static void add(@Nonnull final ParticleEmitter system) {
        _instance.systems.put(system.getPos().toImmutable(), system);
    }

}
