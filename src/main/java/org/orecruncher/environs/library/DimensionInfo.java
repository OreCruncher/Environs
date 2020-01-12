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

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.environs.Config;
import org.orecruncher.environs.Environs;
import org.orecruncher.environs.library.config.DimensionConfig;
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.lib.random.XorShiftRandom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class DimensionInfo {

    public final static DimensionInfo NONE = new DimensionInfo();

    public final static float MIN_INTENSITY = 0.0F;
    public final static float MAX_INTENSITY = 1.0F;

    private static final int SPACE_HEIGHT_OFFSET = 32;
    private static final DecimalFormat FORMATTER = new DecimalFormat("0");

    protected final Random RANDOM = XorShiftRandom.current();

    // Rain/weather tracking data. Some of the data is synchronized from the server.
    private float intensity = 0.0F;
    private float currentIntensity = 0.0F;
    private float minIntensity = Config.CLIENT.rain.get_defaultMinRainStrength();
    private float maxIntensity = Config.CLIENT.rain.get_defaultMaxRainStrength();
    private int thunderTimer = 0;

    // Attributes about the dimension. This is information is loaded from local configs.
    protected int dimensionId;
    protected ResourceLocation name;
    protected int seaLevel;
    protected int skyHeight;
    protected int cloudHeight;
    protected int spaceHeight;
    protected boolean hasHaze = false;
    protected boolean hasAuroras = false;
    protected boolean hasWeather = false;
    protected boolean hasFog = false;
    protected boolean alwaysOutside = false;
    protected boolean playBiomeSounds = true;

    DimensionInfo() {
        this.dimensionId = Integer.MIN_VALUE;
        this.name = new ResourceLocation(Environs.MOD_ID, "no_dimension");
    }

    public DimensionInfo(@Nonnull final World world) {
        this(world, null);
    }

    public DimensionInfo(@Nonnull final World world, @Nullable final DimensionConfig dimConfig) {
        // Attributes that come from the world object itself. Set now because the config may override.
        this.dimensionId = world.getDimension().getType().getId();
        this.name = world.getDimension().getType().getRegistryName();
        this.seaLevel = world.getSeaLevel();
        this.skyHeight = world.getActualHeight();
        this.cloudHeight = this.skyHeight;
        this.spaceHeight = this.skyHeight + SPACE_HEIGHT_OFFSET;

        if (world.getDimension().isSurfaceWorld() && world.getDimension().hasSkyLight()) {
            this.hasWeather = true;
            this.hasAuroras = true;
            this.hasFog = true;
        }

        // Force sea level based on known world types that give heartburn
        final WorldType wt = world.getWorldType();

        if (wt == WorldType.FLAT)
            this.seaLevel = 0;
        else if (this.dimensionId == 0 && Config.CLIENT.biome.get_worldSealevelOverride() > 0)
            this.seaLevel = Config.CLIENT.biome.get_worldSealevelOverride();

        if (Config.CLIENT.biome.get_biomeSoundBlacklist().contains(this.dimensionId))
            this.playBiomeSounds = false;

        // Override based on player config settings
        if (dimConfig != null) {
            if (dimConfig.seaLevel != null)
                this.seaLevel = dimConfig.seaLevel;
            if (dimConfig.skyHeight != null)
                this.skyHeight = dimConfig.skyHeight;
            if (dimConfig.hasHaze != null)
                this.hasHaze = dimConfig.hasHaze;
            if (dimConfig.hasAurora != null)
                this.hasAuroras = dimConfig.hasAurora;
            if (dimConfig.hasWeather != null)
                this.hasWeather = dimConfig.hasWeather;
            if (dimConfig.cloudHeight != null)
                this.cloudHeight = dimConfig.cloudHeight;
            else
                this.cloudHeight = this.hasHaze ? this.skyHeight / 2 : this.skyHeight;
            if (dimConfig.hasFog != null)
                this.hasFog = dimConfig.hasFog;
            if (dimConfig.alwaysOutside != null)
                this.alwaysOutside = dimConfig.alwaysOutside;

            this.spaceHeight = this.skyHeight + SPACE_HEIGHT_OFFSET;
        }
    }

    public int getId() {
        return this.dimensionId;
    }

    @Nonnull
    public ResourceLocation getName() {
        return this.name;
    }

    public int getSeaLevel() {
        return this.seaLevel;
    }

    public int getSkyHeight() {
        return this.skyHeight;
    }

    public int getCloudHeight() {
        return this.cloudHeight;
    }

    public int getSpaceHeight() {
        return this.spaceHeight;
    }

    public boolean hasHaze() {
        return this.hasHaze;
    }

    public boolean hasAuroras() {
        return this.hasAuroras;
    }

    public boolean hasWeather() {
        return this.hasWeather;
    }

    public boolean hasFog() {
        return this.hasFog;
    }

    public boolean playBiomeSounds() {
        return this.playBiomeSounds;
    }

    public boolean alwaysOutside() {
        return this.alwaysOutside;
    }

    public float getRainIntensity() {
        return this.intensity;
    }

    public float getCurrentRainIntensity() {
        return this.currentIntensity;
    }

    public void setRainIntensity(final float intensity) {
        this.intensity = MathStuff.clamp(intensity, MIN_INTENSITY, MAX_INTENSITY);
    }

    public void setCurrentRainIntensity(final float intensity) {
        this.currentIntensity = MathStuff.clamp(intensity, 0, this.intensity);
    }

    public float getMinRainIntensity() {
        return this.minIntensity;
    }

    public void setMinRainIntensity(final float intensity) {
        this.minIntensity = MathStuff.clamp(intensity, MIN_INTENSITY, this.maxIntensity);
    }

    public float getMaxRainIntensity() {
        return this.maxIntensity;
    }

    public void setMaxRainIntensity(final float intensity) {
        this.maxIntensity = MathStuff.clamp(intensity, this.minIntensity, MAX_INTENSITY);
    }

    public int getThunderTimer() {
        return this.thunderTimer;
    }

    public void setThunderTimer(final int time) {
        this.thunderTimer = MathStuff.clamp(time, 0, Integer.MAX_VALUE);
    }

    public void randomizeRain() {
        final float result;
        final float delta = this.maxIntensity - this.minIntensity;
        if (delta <= 0.0F) {
            result = this.minIntensity;
        } else {
            final float mid = delta / 2.0F;
            result = this.minIntensity + this.RANDOM.nextFloat() * mid + this.RANDOM.nextFloat() * mid;
        }
        setRainIntensity(MathStuff.clamp(result, 0.01F, MAX_INTENSITY));
        setCurrentRainIntensity(0.0F);
    }

    @Nonnull
    public String configString() {
        return "dim " + getId() + ": " +
                "rainIntensity [" + FORMATTER.format(getMinRainIntensity() * 100) +
                "," + FORMATTER.format(getMaxRainIntensity() * 100) +
                "]";
    }

    @Nonnull
    public String toString() {
        // Dump out some diagnostics for the current dimension
        return "dim " + getId() + ": " +
                "rainIntensity: " + FORMATTER.format(getRainIntensity() * 100) +
                '/' + FORMATTER.format(getCurrentRainIntensity() * 100) +
                " [" + FORMATTER.format(getMinRainIntensity() * 100) +
                "," + FORMATTER.format(getMaxRainIntensity() * 100) +
                "], thunderTimer: " + getThunderTimer();
    }

}
