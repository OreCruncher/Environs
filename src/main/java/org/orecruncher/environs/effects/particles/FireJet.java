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

package org.orecruncher.environs.effects.particles;

import net.minecraft.client.particle.FlameParticle;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.client.particle.Particle;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.sndctrl.library.AcousticLibrary;

@OnlyIn(Dist.CLIENT)
public class FireJet extends Jet {

	private static final ResourceLocation FIRE_ACOUSTIC = new ResourceLocation("block.fire.ambient");
	protected final boolean isLava;
	protected final IParticleData particleType;
	protected boolean soundFired;

	public FireJet(final int strength, final IWorldReader world, final double x, final double y, final double z) {
		super(strength, world, x, y, z);
		this.isLava = RANDOM.nextInt(3) == 0;
		this.particleType = this.isLava ? ParticleTypes.LAVA : ParticleTypes.FLAME;
	}

	@Override
	protected void soundUpdate() {
		if (!this.soundFired) {
			this.soundFired = true;
			AcousticLibrary.resolve(FIRE_ACOUSTIC).playAt(getPos());
		}
	}

	@Override
	protected void spawnJetParticle() {
		final double speedY = this.isLava ? 0 : this.jetStrength / 10.0D;
		final Particle particle = GameUtils.getMC().particles.addParticle(this.particleType, this.posX, this.posY, this.posZ, 0, speedY, 0D);

		if (particle instanceof FlameParticle) {
			particle.multipleParticleScaleBy(this.jetStrength);
		}
	}
}
