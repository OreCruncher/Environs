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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.orecruncher.environs.Config;
import org.orecruncher.environs.Environs;
import org.orecruncher.environs.library.config.BiomeConfig;
import org.orecruncher.environs.library.config.ModConfig;
import org.orecruncher.lib.fml.ForgeUtils;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.lib.math.MathStuff;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

@OnlyIn(Dist.CLIENT)
public final class BiomeLibrary {

	private static final IModLog LOGGER = Environs.LOGGER.createChild(BiomeLibrary.class);

	private static final int INSIDE_Y_ADJUST = 3;

	public static final FakeBiome UNDERGROUND = new FakeBiome("Underground");
	public static final FakeBiome PLAYER = new FakeBiome("Player");
	public static final FakeBiome UNDERWATER = new FakeBiome("Underwater");
	public static final FakeBiome UNDEROCEAN = new FakeBiome("UnderOCN");
	public static final FakeBiome UNDERDEEPOCEAN = new FakeBiome("UnderDOCN");
	public static final FakeBiome UNDERRIVER = new FakeBiome("UnderRVR");
	public static final FakeBiome OUTERSPACE = new FakeBiome("OuterSpace");
	public static final FakeBiome CLOUDS = new FakeBiome("Clouds");
	public static final FakeBiome VILLAGE = new FakeBiome("Village");

	public static BiomeInfo UNDERGROUND_INFO;
	public static BiomeInfo PLAYER_INFO;
	public static BiomeInfo UNDERRIVER_INFO;
	public static BiomeInfo UNDEROCEAN_INFO;
	public static BiomeInfo UNDERDEEPOCEAN_INFO;
	public static BiomeInfo UNDERWATER_INFO;

	public static BiomeInfo OUTERSPACE_INFO;
	public static BiomeInfo CLOUDS_INFO;
	public static BiomeInfo VILLAGE_INFO;
	public static BiomeInfo WTF_INFO;

	// This is for cases when the biome coming in doesn't make sense
	// and should default to something to avoid crap.
	private static final FakeBiome WTF = new WTFFakeBiome();

	private static final ObjectOpenHashSet<FakeBiome> theFakes = new ObjectOpenHashSet<>();

	private BiomeLibrary() {

	}

	static void initialize() {

		ForgeUtils.getBiomes().forEach(BiomeLibrary::register);

		// Add our fake biomes
		register(UNDERWATER);
		register(UNDEROCEAN);
		register(UNDERDEEPOCEAN);
		register(UNDERRIVER);
		register(PLAYER);
		register(UNDERGROUND);
		register(CLOUDS);
		register(VILLAGE);
		register(OUTERSPACE);

		UNDERGROUND_INFO = resolve(UNDERGROUND);
		PLAYER_INFO = resolve(PLAYER);
		UNDERRIVER_INFO = resolve(UNDERRIVER);
		UNDEROCEAN_INFO = resolve(UNDEROCEAN);
		UNDERDEEPOCEAN_INFO = resolve(UNDERDEEPOCEAN);

		UNDERWATER_INFO = resolve(UNDERWATER);
		CLOUDS_INFO = resolve(CLOUDS);
		VILLAGE_INFO = resolve(VILLAGE);
		OUTERSPACE_INFO = resolve(OUTERSPACE);

		// WTF is a strange animal
		register(WTF);
		WTF_INFO = resolve(WTF);
	}

	static void initFromConfig(@Nonnull final ModConfig cfg) {

		if (cfg.biomes.size() > 0) {
			final List<BiomeInfo> infoList = getCombinedStream();

			final BiomeEvaluator evaluator = new BiomeEvaluator();

			for (final BiomeInfo bi : infoList) {
				evaluator.update(bi);
				for (final BiomeConfig c : cfg.biomes) {
					if (evaluator.matches(c.conditions)) {
						try {
							bi.update(c);
						} catch(@Nonnull final Throwable t) {
							LOGGER.warn("Unable to process biome sound configuration [%s]", c.toString());
						}
					}
				}
			}

			// Make sure the default PLAINS biome is set. OTG can do some strange things.
			final ResourceLocation plainsLoc = new ResourceLocation("plains");
			final Biome plains = ForgeRegistries.BIOMES.getValue(plainsLoc);
			final BiomeInfo info = BiomeUtil.getBiomeData(plains);
			BiomeUtil.setBiomeData(Biomes.PLAINS, info);
		}
	}

	static void complete() {
		if (Config.CLIENT.logging.get_enableLogging()) {
			LOGGER.info("*** BIOME REGISTRY ***");
			getCombinedStream().stream().sorted().map(Object::toString).forEach(LOGGER::info);
		}
	}

	private static void register(@Nonnull final Biome biome) {
		final BiomeHandler handler = new BiomeHandler(biome);
		final BiomeInfo info = new BiomeInfo(handler);
		BiomeUtil.setBiomeData(biome, info);
	}

	private static void register(@Nonnull final IBiome biome) {
		if (biome.isFake()) {
			final FakeBiome fb = (FakeBiome) biome;
			final BiomeInfo info = new BiomeInfo(fb);
			fb.setBiomeData(info);
			theFakes.add(fb);
		}
	}

	@Nullable
	private static BiomeInfo resolve(@Nonnull final IBiome biome) {
		if (biome.isFake()) {
			final FakeBiome fb = (FakeBiome) biome;
			return fb.getBiomeData();
		}
		return null;
	}

	@Nonnull
	public static BiomeInfo getPlayerBiome(@Nonnull final PlayerEntity player, final boolean getTrue) {
		final Biome biome = player.getEntityWorld().getBiome(new BlockPos(player.posX, 0, player.posZ));
		BiomeInfo info = BiomeUtil.getBiomeData(biome);

		if (!getTrue) {
			if (player.areEyesInFluid(FluidTags.WATER)) {
				if (info.isRiver())
					info = UNDERRIVER_INFO;
				else if (info.isDeepOcean())
					info = UNDERDEEPOCEAN_INFO;
				else if (info.isOcean())
					info = UNDEROCEAN_INFO;
				else
					info = UNDERWATER_INFO;
			} else {
				final DimensionInfo dimInfo = DimensionLibrary.getData(player.getEntityWorld());
				final int theY = MathStuff.floor(player.posY);
				if ((theY + INSIDE_Y_ADJUST) <= dimInfo.getSeaLevel())
					info = UNDERGROUND_INFO;
				else if (theY >= dimInfo.getSpaceHeight())
					info = OUTERSPACE_INFO;
				else if (theY >= dimInfo.getCloudHeight())
					info = CLOUDS_INFO;
			}
		}

		return info;
	}

	private static List<BiomeInfo> getCombinedStream() {
		final ArrayList<BiomeInfo> infos = new ArrayList<>();

		for (final Biome b : ForgeUtils.getBiomes()) {
			final BiomeInfo info = BiomeUtil.getBiomeData(b);
			infos.add(info);
		}

		for (final FakeBiome b : theFakes) {
			final BiomeInfo info = b.getBiomeData();
			if (info != null) {
				infos.add(info);
			}
		}

		return infos;
	}
}
