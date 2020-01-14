/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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

@OnlyIn(Dist.CLIENT)
public abstract class JetEffect extends BlockEffect {

	protected static final int MAX_STRENGTH = 10;

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
		return count;
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
