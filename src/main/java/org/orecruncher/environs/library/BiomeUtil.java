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

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import org.orecruncher.environs.Environs;
import org.orecruncher.lib.gui.Color;
import org.orecruncher.lib.reflection.ObjectField;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public final class BiomeUtil {

    private static final Color NO_COLOR = new Color(1F, 1F, 1F);

    private static final ObjectField<Biome, BiomeInfo> environs_biomeData =
            new ObjectField<>(
                    Biome.class,
                    () -> BiomeLibrary.WTF_INFO,
                    "environs_biomeData"
            );

    @Nonnull
    public static BiomeInfo getBiomeData(@Nonnull final Biome biome) {
        BiomeInfo result = environs_biomeData.get(biome);
        if (result == null) {
            Environs.LOGGER.warn("Unable to find configuration for biome [%s] (hc=%d)", biome.getRegistryName(),
                    System.identityHashCode(biome));
            result = BiomeLibrary.WTF_INFO;
            setBiomeData(biome, result);
        }
        return result;
    }

    public static void setBiomeData(@Nonnull final Biome biome, @Nullable final BiomeInfo data) {
        environs_biomeData.set(biome, data);
    }

    @Nonnull
    public static String getBiomeName(@Nonnull final Biome biome) {
        return biome.getDisplayName().getFormattedText();
    }

    // ===================================
    //
    // Miscellaneous Support Functions
    //
    // ===================================
    @Nonnull
    public static Collection<Type> getBiomeTypes() {
        return BiomeDictionary.Type.getAll();
    }

    @Nullable
    public static Color getColorForLiquid(@Nonnull final World world, @Nonnull final BlockPos pos) {
        final IFluidState fluidState = world.getFluidState(pos);

        if (fluidState.isEmpty())
            return NO_COLOR;

        final Fluid fluid = fluidState.getFluid();
        return new Color(fluid.getAttributes().getColor());
    }

    @Nonnull
    public static Set<Type> getBiomeTypes(@Nonnull final Biome biome) {
        // It's possible to have a biome that is not registered come through here
        // There is an internal check that will throw an exception if that is the
        // case. Seen this with OTG installed.
        try {
            return BiomeDictionary.getTypes(biome);
        } catch (@Nonnull final Throwable t) {
            final String name = getBiomeName(biome);
            Environs.LOGGER.warn("Unable to get biome type data for biome '%s'", name);
        }
        return new ReferenceOpenHashSet<>();
    }

    public static boolean areBiomesSimilar(@Nonnull final Biome b1, @Nonnull final Biome b2) {
        return BiomeDictionary.areSimilar(b1, b2);
    }

}
