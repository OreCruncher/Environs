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

package org.orecruncher.environs.library;

import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.TempCategory;
import net.minecraftforge.common.BiomeDictionary.Type;
import org.orecruncher.environs.Environs;

@OnlyIn(Dist.CLIENT)
public class BiomeHandler implements IBiome {

	protected final Biome biome;
	protected final Set<Type> types;

	public BiomeHandler(@Nonnull final Biome biome) {
		this.biome = biome;
		this.types = BiomeUtil.getBiomeTypes(this.biome);
	}

	@Override
	public Biome getBiome() {
		return this.biome;
	}

	@Override
	public ResourceLocation getKey() {
		return this.biome.getRegistryName();
	}

	@Override
	public String getName() {
		return BiomeUtil.getBiomeName(this.biome);
	}

	@Override
	public Set<Type> getTypes() {
		return this.types;
	}

	@Override
	public Biome.RainType getPrecipitationType() {
		return this.biome.getPrecipitation();
	}

	@Override
	public float getFloatTemperature(@Nonnull final BlockPos pos) {
		return this.biome.getTemperature(pos);
	}

	@Override
	public float getTemperature() {
		return this.biome.getDefaultTemperature();
	}

	@Override
	public TempCategory getTempCategory() {
		return this.biome.getTempCategory();
	}

	@Override
	public boolean isHighHumidity() {
		return this.biome.isHighHumidity();
	}

	@Override
	public float getDownfall() {
		return this.biome.getDownfall();
	}

	@Nonnull
	public static ResourceLocation getKey(@Nonnull final Biome biome) {
		ResourceLocation res = biome.getRegistryName();
		if (res == null) {
			final String name = biome.getClass().getName() + "_" + BiomeUtil.getBiomeName(biome).replace(' ', '_').toLowerCase();
			res = new ResourceLocation(Environs.MOD_ID, name);
		}
		return res;
	}

}
