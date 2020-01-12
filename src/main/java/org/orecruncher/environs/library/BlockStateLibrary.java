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

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.orecruncher.environs.Environs;
import org.orecruncher.environs.effects.BlockEffect;
import org.orecruncher.environs.effects.BlockEffectType;
import org.orecruncher.environs.library.config.AcousticConfig;
import org.orecruncher.environs.library.config.BlockConfig;
import org.orecruncher.environs.library.config.EffectConfig;
import org.orecruncher.environs.library.config.ModConfig;
import org.orecruncher.lib.blockstate.BlockStateMatcher;
import org.orecruncher.lib.blockstate.BlockStateMatcherMap;
import org.orecruncher.lib.fml.ForgeUtils;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.sndctrl.audio.acoustic.IAcoustic;
import org.orecruncher.sndctrl.library.AcousticLibrary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public final class BlockStateLibrary {

    private static final IModLog LOGGER = Environs.LOGGER.createChild(BlockStateLibrary.class);

    private BlockStateLibrary() {

    }

    private static final BlockStateMatcherMap<BlockStateData> registry = new BlockStateMatcherMap<>();

    static void initialize() {
        ForgeUtils.getBlockStates().forEach(state -> BlockStateUtil.setData(state, null));
        BlockStateUtil.setData(Blocks.AIR.getDefaultState(), BlockStateData.DEFAULT);
        BlockStateUtil.setData(Blocks.CAVE_AIR.getDefaultState(), BlockStateData.DEFAULT);
        BlockStateUtil.setData(Blocks.VOID_AIR.getDefaultState(), BlockStateData.DEFAULT);
    }

    static void initFromConfig(@Nonnull final ModConfig config) {
        config.blocks.forEach(BlockStateLibrary::register);
    }

    static void complete() {
        int blockStates = (int) ForgeUtils.getBlockStates().stream().map(BlockStateLibrary::get).count();
        LOGGER.info("%d block states processed, %d registry entries", blockStates, registry.size());
    }

    @Nonnull
    private static BlockStateData get(@Nonnull final BlockState state) {
        BlockStateData profile = BlockStateUtil.getData(state);
        if (profile == null) {
            profile = registry.get(state);
            if (profile == null)
                profile = BlockStateData.DEFAULT;
            BlockStateUtil.setData(state, profile);
        }
        return profile;
    }

    @Nullable
    private static BlockStateData getOrCreateProfile(@Nonnull final BlockStateMatcher info) {
        if (info.isEmpty())
            return null;

        BlockStateData profile = registry.get(info);
        if (profile == null) {
            profile = new BlockStateData();
            registry.put(info, profile);
        }

        return profile;
    }

    private static void register(@Nonnull final BlockConfig entry) {
        if (entry.blocks.isEmpty())
            return;

        for (final String blockName : entry.blocks) {
            final BlockStateMatcher blockInfo = BlockStateMatcher.create(blockName);

            final BlockStateData blockData = getOrCreateProfile(blockInfo);
            if (blockData == null) {
                LOGGER.warn("Unknown block [%s] in block config file", blockName);
                continue;
            }

            // Reset of a block clears all registry
            if (entry.soundReset != null && entry.soundReset)
                blockData.clearSounds();
            if (entry.effectReset != null && entry.effectReset)
                blockData.clearEffects();

            if (entry.chance != null)
                blockData.setChance(entry.chance);

            for (final AcousticConfig sr : entry.acoustics) {
                if (sr.acoustic != null) {
                    final ResourceLocation res = AcousticLibrary.resolveResource(Environs.MOD_ID, sr.acoustic);
                    final IAcoustic acoustic = AcousticLibrary.resolve(res, sr.acoustic);
                    final int weight = sr.weight;
                    final WeightedAcousticEntry acousticEntry = new WeightedAcousticEntry(acoustic, sr.conditions, weight);
                    blockData.addSound(acousticEntry);
                }
            }

            for (final EffectConfig e : entry.effects) {
                if (StringUtils.isEmpty(e.effect))
                    continue;
                final BlockEffectType type = BlockEffectType.get(e.effect);
                if (type == BlockEffectType.UNKNOWN) {
                    LOGGER.warn("Unknown block effect type in configuration: [%s]", e.effect);
                } else if (type.isEnabled()) {
                    final int chance = e.chance != null ? e.chance : 100;
                    final BlockEffect blockEffect = type.getInstance(chance);
                    if (blockEffect != null) {
                        if (e.conditions != null)
                            blockEffect.setConditions(e.conditions);
                        blockData.addEffect(blockEffect);
                    }
                }
            }
        }
    }

}
