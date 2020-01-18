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

import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.orecruncher.environs.library.BiomeInfo;
import org.orecruncher.environs.library.BiomeLibrary;
import org.orecruncher.environs.library.DimensionLibrary;
import org.orecruncher.environs.scanner.BiomeScanner;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.TickCounter;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.lib.events.DiagnosticEvent;
import org.orecruncher.sndctrl.audio.AudioEngine;
import org.orecruncher.sndctrl.audio.acoustic.IAcoustic;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class BiomeSoundEffects extends HandlerBase {

    public static final int SCAN_INTERVAL = 4;
    protected final BiomeScanner biomes = new BiomeScanner();
    private final Object2ObjectOpenHashMap<IAcoustic, BackgroundAcousticEmitter> emitters = new Object2ObjectOpenHashMap<>();
    BiomeSoundEffects() {
        super("Biome Sounds");
    }

    @Override
    public boolean doTick(final long tick) {
        return DimensionLibrary.getData(GameUtils.getWorld()).playBiomeSounds();
    }

    private boolean doBiomeSounds() {
        return CommonState.isUnderground() || CommonState.getDimensionInfo().alwaysOutside()
                || !CommonState.isInside();
    }

    private void getBiomeSounds(@Nonnull final Object2FloatOpenHashMap<IAcoustic> result) {
        // Need to collect sounds from all the applicable biomes
        // along with their weights.
        this.biomes.getBiomes().reference2FloatEntrySet()
                .forEach(e -> e.getKey().findSoundMatches().forEach(fx -> result.addTo(fx, e.getFloatValue())));

        // Scale the volumes in the resulting list based on the weights
        final float area = this.biomes.getBiomeArea();
        result.replaceAll((fx, v) -> 0.1F + 0.9F * (v / area));
    }

    @Override
    public void process(@Nonnull final PlayerEntity player) {
        this.emitters.values().forEach(BackgroundAcousticEmitter::tick);
        if ((TickCounter.getTickCount() % SCAN_INTERVAL) == 0) {
            this.biomes.tick();
            handleBiomeSounds(player);
        }
    }

    @Override
    public void onConnect() {
        clearSounds();
    }

    @Override
    public void onDisconnect() {
        clearSounds();
    }

    private void handleBiomeSounds(@Nonnull final PlayerEntity player) {
        this.biomes.tick();

        final Object2FloatOpenHashMap<IAcoustic> sounds = new Object2FloatOpenHashMap<>();
        sounds.defaultReturnValue(0);

        // Only gather data if the player is alive. If the player is dead the biome
        // sounds will cease playing.
        if (player.isAlive()) {

            if (doBiomeSounds())
                getBiomeSounds(sounds);

            final ObjectArray<IAcoustic> playerSounds = new ObjectArray<>();
            BiomeLibrary.PLAYER_INFO.findSoundMatches(playerSounds);
            if (CommonState.isInVillage())
                BiomeLibrary.VILLAGE_INFO.findSoundMatches(playerSounds);

            playerSounds.forEach(fx -> sounds.put(fx, 1.0F));

            if (doBiomeSounds()) {
                final BiomeInfo playerBiome = CommonState.getPlayerBiome();
                final IAcoustic sound = playerBiome.getSpotSound(this.RANDOM);
                if (sound != null)
                    sound.playAt(CommonState.getPlayerPosition());
            }

            final IAcoustic sound = BiomeLibrary.PLAYER_INFO.getSpotSound(this.RANDOM);
            if (sound != null)
                sound.playNear(player);
        }

        queueAmbientSounds(sounds);
    }

    private void queueAmbientSounds(@Nonnull final Object2FloatOpenHashMap<IAcoustic> sounds) {

        // Iterate through the existing emitters:
        // * If done, remove
        // * If not in the incoming list, fade
        // * If it does exist, update volume throttle and unfade if needed
        this.emitters.object2ObjectEntrySet().removeIf(entry -> {
            final BackgroundAcousticEmitter backgroundAcousticEmitter = entry.getValue();
            if (backgroundAcousticEmitter.isDonePlaying()) {
                return true;
            }
            final float volume = sounds.getFloat(entry.getKey());
            if (volume > 0) {
                backgroundAcousticEmitter.setVolumeThrottle(volume);
                if (backgroundAcousticEmitter.isFading())
                    backgroundAcousticEmitter.unfade();
                sounds.removeFloat(entry.getKey());
            } else if (!backgroundAcousticEmitter.isFading()) {
                backgroundAcousticEmitter.fade();
            }
            return false;
        });

        // Any sounds left in the list are new and need an emitter created.
        sounds.forEach((fx, volume) -> {
            final BackgroundAcousticEmitter e = new BackgroundAcousticEmitter(fx);
            e.setVolumeThrottle(volume);
            this.emitters.put(fx, e);
        });
    }

    public void clearSounds() {
        this.emitters.values().forEach(BackgroundAcousticEmitter::stop);
        this.emitters.clear();
        AudioEngine.stopAll();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void diagnostics(@Nonnull final DiagnosticEvent event) {
        this.emitters.values().forEach(backgroundAcousticEmitter -> event.getLeft().add("EMITTER: " + backgroundAcousticEmitter.toString()));
    }
}
