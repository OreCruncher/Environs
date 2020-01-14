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

package org.orecruncher.environs.effects;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.environs.effects.emitters.Jet;
import org.orecruncher.environs.effects.emitters.SteamJet;

import javax.annotation.Nonnull;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class SteamJetEffect extends JetEffect {

    public SteamJetEffect(final int chance) {
        super(chance);
    }

    @Override
    @Nonnull
    public BlockEffectType getEffectType() {
        return BlockEffectType.STEAM_JET;
    }

    public static boolean isValidSpawnBlock(@Nonnull final IWorldReader provider, @Nonnull final BlockPos pos) {
        return isValidSpawnBlock(provider.getBlockState(pos), provider, pos);
    }

    public static boolean isValidSpawnBlock(@Nonnull final BlockState state, @Nonnull final IWorldReader provider,
                                            @Nonnull final BlockPos pos) {
        return FLUID_PREDICATE.test(state)
                && provider.isAirBlock(pos.up())
                && countHorizontalBlocks(provider, pos, HOTBLOCK_PREDICATE, true) > 0;
    }

    @Override
    public boolean canTrigger(@Nonnull final IWorldReader provider, @Nonnull final BlockState state,
                              @Nonnull final BlockPos pos, @Nonnull final Random random) {
        return isValidSpawnBlock(state, provider, pos) && super.canTrigger(provider, state, pos, random);
    }

    @Override
    public void doEffect(@Nonnull final IWorldReader provider, @Nonnull final BlockState state,
                         @Nonnull final BlockPos pos, @Nonnull final Random random) {
        final int strength = countHorizontalBlocks(provider, pos, HOTBLOCK_PREDICATE, false);
        if (strength > 0) {
            final float spawnHeight = pos.getY() + state.getFluidState().getHeight() + 0.1F;
            final Jet effect = new SteamJet(strength, provider, pos.getX() + 0.5D, spawnHeight, pos.getZ() + 0.5D);
            addEffect(effect);
        }
    }
}
