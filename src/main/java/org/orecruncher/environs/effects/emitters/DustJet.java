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

package org.orecruncher.environs.effects.emitters;

import net.minecraft.block.BlockState;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;
import org.orecruncher.environs.effects.particles.DustParticle;

@OnlyIn(Dist.CLIENT)
public class DustJet extends Jet {

	protected final BlockState blockState;

	public DustJet(final int strength, final IWorldReader world, final double x, final double y, final double z,
				   final BlockState state) {
		super(1, strength, world, x, y, z, 2);
		this.blockState = state;
	}

	@Override
	protected void spawnJetParticle() {
		final double x = this.posX + RANDOM.nextGaussian() * 0.2D;
		final double z = this.posZ + RANDOM.nextGaussian() * 0.2D;
		final Particle particle = new DustParticle((World) this.world, x, this.posY, z, this.blockState).init();
		addParticle(particle);
	}

}
