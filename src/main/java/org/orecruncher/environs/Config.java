/*
 * Dynamic Surroundings: Environs
 * Copyright (C) 2019  OreCruncher
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
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.environs;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Environs.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Config {
    @Nonnull
    public static final Client CLIENT;
    private static final String CLIENT_CONFIG = Environs.MOD_ID + File.separator + Environs.MOD_ID + "-client.toml";
    @Nonnull
    private static final ForgeConfigSpec clientSpec;

    static {
        final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
        clientSpec = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    private Config() {
    }

    private static void applyConfig() {
        CLIENT.update();
        Environs.LOGGER.setDebug(Config.CLIENT.logging.get_enableLogging());
        Environs.LOGGER.setTraceMask(Config.CLIENT.logging.get_flagMask());
    }

    @SubscribeEvent
    public static void onLoad(final ModConfig.Loading configEvent) {
        applyConfig();
        Environs.LOGGER.debug("Loaded config file %s", configEvent.getConfig().getFileName());
    }

    @SubscribeEvent
    public static void onFileChange(final ModConfig.ConfigReloading configEvent) {
        Environs.LOGGER.debug("Config file changed %s", configEvent.getConfig().getFileName());
        applyConfig();
    }

    public static void setup() {
        // The subdir with the mod ID name should have already been created
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.clientSpec, CLIENT_CONFIG);
    }

    public static class Client {

        public final Logging logging;
        public final Rain rain;
        public final Biome biome;
        public final Effects effects;

        Client(@Nonnull final ForgeConfigSpec.Builder builder) {
            this.logging = new Logging(builder);
            this.rain = new Rain(builder);
            this.biome = new Biome(builder);
            this.effects = new Effects(builder);
        }

        void update() {
            this.logging.update();
            this.rain.update();
            this.biome.update();
            this.effects.update();
        }

        public static class Logging {

            private final BooleanValue enableLogging;
            private final BooleanValue onlineVersionCheck;
            private final IntValue flagMask;

            private boolean _enableLogging;
            private boolean _onlineVersionCheck;
            private int _flagMask;

            Logging(@Nonnull final ForgeConfigSpec.Builder builder) {
                builder.comment("Defines how logging will behave")
                        .push("Logging Options");

                this.enableLogging = builder
                        .comment("Enables/disables debug logging of the mod")
                        .translation("environs.cfg.logging.EnableDebug")
                        .define("Debug Logging", false);

                this.onlineVersionCheck = builder
                        .comment("Enables/disables display of version check information")
                        .translation("environs.cfg.logging.VersionCheck")
                        .define("Online Version Check Result", true);

                this.flagMask = builder
                        .comment("Bitmask for toggling various debug traces")
                        .translation("environs.cfg.logging.FlagMask")
                        .defineInRange("Debug Flag Mask", 0, 0, Integer.MAX_VALUE);

                builder.pop();
            }

            void update() {
                this._enableLogging = this.enableLogging.get();
                this._onlineVersionCheck = this.onlineVersionCheck.get();
                this._flagMask = this.flagMask.get();
            }

            public boolean get_enableLogging() {
                return this._enableLogging;
            }

            public boolean get_onlineVersionCheck() {
                return this._onlineVersionCheck;
            }

            public int get_flagMask() {
                return this._flagMask;
            }
        }

        public static class Rain {

            private final IntValue defaultMinRainStrength;
            private final IntValue defaultMaxRainStrength;

            private float _defaultMinRainStrength;
            private float _defaultMaxRaniStrength;

            Rain(@Nonnull final ForgeConfigSpec.Builder builder) {
                builder.comment("Define parameters for rain effects")
                        .push("Rain Options");

                this.defaultMinRainStrength = builder
                        .comment("Minimum rain strength for rain effect")
                        .translation("environs.cfg.rain.MinStrength")
                        .defineInRange("Minimum Rain Strength", 100, 0, 100);

                this.defaultMaxRainStrength = builder
                        .comment("Maximum rain strength for rain effect")
                        .translation("environs.cfg.rain.MaxStrength")
                        .defineInRange("Maximum Rain Strength", 100, 0, 100);

                builder.pop();
            }

            public void update() {
                this._defaultMinRainStrength = this.defaultMinRainStrength.get();
                this._defaultMaxRaniStrength = this.defaultMaxRainStrength.get();
            }

            public float get_defaultMinRainStrength() {
                return this._defaultMinRainStrength;
            }

            public float get_defaultMaxRainStrength() {
                return this._defaultMaxRaniStrength;
            }
        }

        public static class Biome {

            private final IntValue worldSealevelOverride;
            private final ForgeConfigSpec.ConfigValue<List<? extends Integer>> biomeSoundBlacklist;

            private int _worldSealevelOverride;
            private IntOpenHashSet _biomeSoundBlacklist = new IntOpenHashSet();

            Biome(@Nonnull final ForgeConfigSpec.Builder builder) {
                builder.comment("Define parameters for biome effects")
                        .push("Biome Options");

                this.worldSealevelOverride = builder
                        .comment("Override the sea level value for Overworld")
                        .translation("environs.cfg.biome.SeaLevelOverride")
                        .defineInRange("Overworld Sea Level Override", 0, 0, 256);

                this.biomeSoundBlacklist = builder
                        .comment("Prevent biome sounds from playing in specified dimensions")
                        .translation("environs.cfg.biome.BlackList")
                        .defineList("Biome Sound Blacklist", ArrayList::new, s -> true);

                builder.pop();
            }

            public void update() {
                this._worldSealevelOverride = this.worldSealevelOverride.get();
                this._biomeSoundBlacklist = new IntOpenHashSet(this.biomeSoundBlacklist.get());
            }

            public int get_worldSealevelOverride() {
                return this._worldSealevelOverride;
            }

            public IntOpenHashSet get_biomeSoundBlacklist() {
                return this._biomeSoundBlacklist;
            }
        }

        public static class Effects {

            private boolean _enableFireFlies;
            private boolean _enableSteamJets;
            private boolean _enableFireJets;
            private boolean _enableBubbleJets;
            private boolean _enableDustJets;
            private boolean _enableFountainJets;
            private boolean _enableWaterSplashJets;

            Effects(@Nonnull final ForgeConfigSpec.Builder builder) {
                builder.comment("Define parameters for special effects")
                        .push("Effect Options");

                builder.pop();
            }

            public void update() {
                this._enableFireFlies = true;
                this._enableBubbleJets = true;
                this._enableFireJets = true;
                this._enableSteamJets = true;
                this._enableDustJets = true;
            }

            // Reach over and grab from SoundControl
            public int get_effectRange() {
                return org.orecruncher.sndctrl.Config.CLIENT.effects.get_effectRange();
            }

            public boolean get_enableFireFlies() {
                return this._enableFireFlies;
            }

            public boolean get_enableSteamJets() {
                return this._enableSteamJets;
            }

            public boolean get_enableFireJets() {
                return this._enableFireJets;
            }

            public boolean get_enableBubbleJets() {
                return this._enableBubbleJets;
            }

            public boolean get_enableDustJets() {
                return this._enableDustJets;
            }

            public boolean get_enableFountainJets() {
                return this._enableFountainJets;
            }

            public boolean get_enableWaterSplashJets() {
                return this._enableWaterSplashJets;
            }

        }
    }
}
