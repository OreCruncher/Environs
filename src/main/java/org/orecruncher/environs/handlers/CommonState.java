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

package org.orecruncher.environs.handlers;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.orecruncher.environs.library.BiomeInfo;
import org.orecruncher.environs.library.BiomeLibrary;
import org.orecruncher.environs.library.DimensionInfo;
import org.orecruncher.lib.DayCycle;
import org.orecruncher.lib.MinecraftClock;
import org.orecruncher.lib.TickCounter;

@OnlyIn(Dist.CLIENT)
public final class CommonState {

    private static CommonState instance = new CommonState();

    static CommonState getData() {
        return instance;
    }

    // State that is gathered from the various sources
    // to avoid requery. Used during the tick.
    BiomeInfo playerBiome = BiomeLibrary.WTF_INFO;
    BiomeInfo truePlayerBiome = BiomeLibrary.WTF_INFO;
    int dimensionId;
    String dimensionName = StringUtils.EMPTY;
    DimensionInfo dimInfo = DimensionInfo.NONE;
    BlockPos playerPosition = BlockPos.ZERO;
    float biomeTemperature = 0F;

    boolean inside;
    boolean inVillage;
    boolean isUnderground;
    boolean isInSpace;
    boolean isInClouds;
    int lightLevel;

    DayCycle dayCycle = DayCycle.NO_SKY;

    MinecraftClock clock = new MinecraftClock();

    CommonState() {
    }

    public static BiomeInfo getPlayerBiome() {
        return instance.playerBiome;
    }

    public static BiomeInfo getTruePlayerBiome() {
        return instance.truePlayerBiome;
    }

    public static int getDimensionId() {
        return instance.dimensionId;
    }

    public static String getDimensioName() {
        return instance.dimensionName;
    }

    public static DimensionInfo getDimensionInfo() {
        return instance.dimInfo;
    }

    public static BlockPos getPlayerPosition() {
        return instance.playerPosition;
    }

    public static float getCurrentTemperature() {
        return instance.biomeTemperature;
    }

    public static boolean isInside() {
        return instance.inside;
    }

    public static boolean isInVillage() {
        return instance.inVillage;
    }

    public static boolean isUnderground() {
        return instance.isUnderground;
    }

    public static boolean isInClouds() {
        return instance.isInClouds;
    }

    public static boolean isInSpace() {
        return instance.isInSpace;
    }

    public static int getLightLevel() {
        return instance.lightLevel;
    }

    public static long getTick() {
        return TickCounter.getTickCount();
    }

    public static DayCycle getDayCycle() {
        return instance.dayCycle;
    }

    public static MinecraftClock getClock() {
        return instance.clock;
    }
}
