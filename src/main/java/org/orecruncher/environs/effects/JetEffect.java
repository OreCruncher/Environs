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

import java.util.Random;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.util.math.BlockPos;
import org.orecruncher.environs.effects.emitters.Jet;
import org.orecruncher.environs.handlers.ConditionEvaluator;
import org.orecruncher.environs.handlers.ParticleSystems;
import org.orecruncher.lib.math.MathStuff;

@OnlyIn(Dist.CLIENT)
public abstract class JetEffect extends BlockEffect {

	public static final int MAX_STRENGTH = 10;

	protected static final Predicate<BlockState> FLUID_PREDICATE = (state) -> !state.getFluidState().isEmpty();

	public static final Predicate<BlockState> LAVA_PREDICATE = (state) -> {
		final IFluidState fs = state.getFluidState();
		return !fs.isEmpty() && fs.isTagged(FluidTags.LAVA);
	};

	public static final Predicate<BlockState> WATER_PREDICATE = (state) -> {
		final IFluidState fs = state.getFluidState();
		return !fs.isEmpty() && fs.isTagged(FluidTags.WATER);
	};

	public static final Predicate<BlockState> SOLID_PREDICATE = (state) -> state.getMaterial().isSolid();

	public static final Predicate<BlockState> HOTBLOCK_PREDICATE = (state) -> LAVA_PREDICATE.test(state) || state.getBlock() == Blocks.MAGMA_BLOCK;

	public static int countVerticalBlocks(final IWorldReader provider, final BlockPos pos,
										  final Predicate<BlockState> predicate, final int step) {
		int count = 0;
		final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(pos);
		for (; count < MAX_STRENGTH && predicate.test(provider.getBlockState(mutable)); count++)
			mutable.setY(mutable.getY() + step);
		return MathStuff.clamp(count, 0, MAX_STRENGTH);
	}

	public static int countHorizontalBlocks(final IWorldReader provider, final BlockPos pos,
											final Predicate<BlockState> predicate, final boolean fastFirst) {
		int blockCount = 0;
		for (int i = -1; i <= 1; i++)
			for (int j = -1; j <= 1; j++)
				for (int k = -1; k <= 1; k++) {
					final BlockState state = provider.getBlockState(pos.add(i, j, k));
					if (predicate.test(state)) {
						if (fastFirst)
							return 1;
						blockCount++;
					}
				}
		return blockCount;
	}

	public JetEffect(final int chance) {
		super(chance);
	}

	@Override
	public boolean canTrigger(@Nonnull final IWorldReader provider, @Nonnull final BlockState state,
							  @Nonnull final BlockPos pos, @Nonnull final Random random) {
		if (alwaysExecute() || random.nextInt(getChance()) == 0) {
			return ParticleSystems.okToSpawn(pos) && ConditionEvaluator.INSTANCE.check(getConditions());
		}
		return false;
	}

	protected void addEffect(final Jet fx) {
		ParticleSystems.add(fx);
	}

}
