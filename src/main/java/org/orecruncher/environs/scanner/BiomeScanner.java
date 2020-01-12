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

package org.orecruncher.environs.scanner;

import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import it.unimi.dsi.fastutil.objects.Reference2FloatOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import org.orecruncher.environs.library.BiomeInfo;
import org.orecruncher.environs.library.BiomeLibrary;
import org.orecruncher.environs.library.BiomeUtil;
import org.orecruncher.lib.GameUtils;

/**
 * Performs an area scan around the to calculate the relative weights of the
 * biomes in the local area.
 */
@OnlyIn(Dist.CLIENT)
public final class BiomeScanner implements ITickable {

	private static final int BIOME_SURVEY_RANGE = 20;
	private static final int MAX_BIOME_AREA = (int) Math.pow(BIOME_SURVEY_RANGE * 2 + 1, 2);

	private final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

	private int biomeArea;
	private Reference2FloatOpenHashMap<BiomeInfo> weights = new Reference2FloatOpenHashMap<>();

	// "Finger print" of the last area survey.
	private BiomeInfo surveyedBiome = null;
	private int surveyedDimension = 0;
	private BlockPos surveyedPosition = BlockPos.ZERO;

	@Override
	public void tick() {
		final BlockPos position = GameUtils.getPlayer().getPosition();
		final BiomeInfo playerBiome = BiomeLibrary.getPlayerBiome(GameUtils.getPlayer(), false);
		final int dimId = GameUtils.getWorld().getDimension().getType().getId();

		if (this.surveyedBiome != playerBiome
				|| this.surveyedDimension != dimId
				|| this.surveyedPosition.compareTo(position) != 0) {

			this.surveyedBiome = playerBiome;
			this.surveyedDimension = dimId;
			this.surveyedPosition = position;

			this.biomeArea = 0;
			this.weights = new Reference2FloatOpenHashMap<>();

			if (playerBiome.isFake()) {
				this.biomeArea = 1;
				this.weights.put(playerBiome, 1);
			} else {
				final IWorldReader provider = GameUtils.getWorld();
				for (int dX = -BIOME_SURVEY_RANGE; dX <= BIOME_SURVEY_RANGE; dX++)
					for (int dZ = -BIOME_SURVEY_RANGE; dZ <= BIOME_SURVEY_RANGE; dZ++) {
						this.mutable.setPos(this.surveyedPosition.getX() + dX, 0, this.surveyedPosition.getZ() + dZ);
						final Biome biome = provider.getBiome(this.mutable);
						final BiomeInfo info = BiomeUtil.getBiomeData(biome);
						this.weights.addTo(info, 1F);
					}

				this.biomeArea = MAX_BIOME_AREA;
			}
		}
	}

	public int getBiomeArea() {
		return this.biomeArea;
	}

	public Reference2FloatOpenHashMap<BiomeInfo> getBiomes() {
		return this.weights;
	}

}
