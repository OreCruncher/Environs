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

package org.orecruncher.environs.fog;

import javax.annotation.Nonnull;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.collections.ObjectArray;

import net.minecraftforge.client.event.EntityViewRenderEvent;

/**
 * Consults various different fog calculators and aggregates the results into a
 * single set.
 */
@OnlyIn(Dist.CLIENT)
public class HolisticFogRangeCalculator implements IFogRangeCalculator {

	protected final ObjectArray<IFogRangeCalculator> calculators = new ObjectArray<>(8);
	protected final FogResult cached = new FogResult();

	public void add(@Nonnull final IFogRangeCalculator calc) {
		this.calculators.add(calc);
	}

	@Override
	@Nonnull
	public FogResult calculate(@Nonnull final EntityViewRenderEvent.RenderFogEvent event) {
		float start = event.getFarPlaneDistance();
		float end = event.getFarPlaneDistance();
		for (int i = 0; i < this.calculators.size(); i++) {
			final FogResult result = this.calculators.get(i).calculate(event);
			start = Math.min(start, result.getStart());
			end = Math.min(end, result.getEnd());
		}
		this.cached.set(start, end);
		return this.cached;
	}

	@Override
	public void tick() {
		this.calculators.forEach(IFogRangeCalculator::tick);
	}

	@Override
	@Nonnull
	public String toString() {
		return this.cached.toString();
	}
}
