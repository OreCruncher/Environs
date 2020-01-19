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

import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.world.World;

/*
@OnlyIn(Dist.CLIENT)
public class SeasonFogRangeCalculator extends MorningFogRangeCalculator {

	private static final Map<SeasonKey, FogType> MAPPING = new Object2ReferenceOpenHashMap<>();
	static {
		MAPPING.put(new SeasonKey(SeasonType.AUTUMN, SubType.EARLY), FogType.NORMAL);
		MAPPING.put(new SeasonKey(SeasonType.AUTUMN, SubType.MID), FogType.MEDIUM);
		MAPPING.put(new SeasonKey(SeasonType.AUTUMN, SubType.LATE), FogType.HEAVY);

		MAPPING.put(new SeasonKey(SeasonType.WINTER, SubType.EARLY), FogType.MEDIUM);
		MAPPING.put(new SeasonKey(SeasonType.WINTER, SubType.MID), FogType.LIGHT);
		MAPPING.put(new SeasonKey(SeasonType.WINTER, SubType.LATE), FogType.NORMAL);

		MAPPING.put(new SeasonKey(SeasonType.SPRING, SubType.EARLY), FogType.MEDIUM);
		MAPPING.put(new SeasonKey(SeasonType.SPRING, SubType.MID), FogType.HEAVY);
		MAPPING.put(new SeasonKey(SeasonType.SPRING, SubType.LATE), FogType.NORMAL);

		MAPPING.put(new SeasonKey(SeasonType.SUMMER, SubType.EARLY), FogType.LIGHT);
		MAPPING.put(new SeasonKey(SeasonType.SUMMER, SubType.MID), FogType.NONE);
		MAPPING.put(new SeasonKey(SeasonType.SUMMER, SubType.LATE), FogType.LIGHT);
	}

	@Override
	public FogType getFogType() {
		final World world = EnvironState.getWorld();
		final ISeasonInfo cap = CapabilitySeasonInfo.getCapability(world);
		if (cap != null) {
			final SeasonType t = cap.getSeasonType();
			final SeasonType.SubType st = cap.getSeasonSubType();
			final FogType type = MAPPING.get(new SeasonKey(t, st));
			if (type != null)
				return type;
		}
		return super.getFogType();
	}

	private static class SeasonKey implements Map.Entry<SeasonType, SubType> {

		private final SeasonType season;
		private final SubType subType;

		public SeasonKey(@Nonnull final SeasonType s, @Nonnull final SubType st) {
			this.season = s;
			this.subType = st;
		}

		@Override
		@Nonnull
		public SeasonType getKey() {
			return this.season;
		}

		@Override
		@Nonnull
		public SubType getValue() {
			return this.subType;
		}

		@Override
		public SubType setValue(@Nonnull final SubType value) {
			return null;
		}

		@Override
		public int hashCode() {
			return this.season.hashCode() ^ (31 * this.subType.hashCode());
		}

		@Override
		public boolean equals(@Nonnull final Object key) {
			final SeasonKey sk = (SeasonKey) key;
			return this.season == sk.season && this.subType == sk.subType;
		}

	}

}
*/
