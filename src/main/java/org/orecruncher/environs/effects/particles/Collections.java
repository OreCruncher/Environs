/*
 *  Dynamic Surroundings: Environs
 *  Copyright (C) 2019  OreCruncher
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

package org.orecruncher.environs.effects.particles;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;
import org.orecruncher.environs.Environs;
import org.orecruncher.lib.events.DiagnosticEvent;
import org.orecruncher.lib.opengl.OpenGlUtil;
import org.orecruncher.lib.particles.IParticleMote;
import org.orecruncher.lib.particles.ParticleCollectionHelper;
import org.orecruncher.lib.particles.ParticleRenderType;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = Environs.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class Collections {

    private static final IParticleRenderType RIPPLE_RENDER =
            new ParticleRenderType(new ResourceLocation(Environs.MOD_ID,"textures/particles/ripple.png")) {
                @Override
                public void beginRender(@Nonnull BufferBuilder buffer, @Nonnull TextureManager textureManager) {
                    RenderHelper.disableStandardItemLighting();
                    textureManager.bindTexture(RippleStyle.get().getTexture());
                    GlStateManager.depthMask(false);
                    GlStateManager.enableDepthTest();
                    OpenGlUtil.setStandardBlend();
                    GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);
                    buffer.begin(GL11.GL_QUADS, this.getVertexFormat());
                }
            };

    private static final IParticleRenderType SPRAY_RENDER = new ParticleRenderType(new ResourceLocation(Environs.MOD_ID,"textures/particles/rainsplash.png"));
    private static final IParticleRenderType FIREFLY_RENDER = IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;

    private final static ParticleCollectionHelper theRipples = new ParticleCollectionHelper("Rain Ripples", RIPPLE_RENDER);
    private final static ParticleCollectionHelper theSprays = new ParticleCollectionHelper("Water Spray", SPRAY_RENDER);
    private final static ParticleCollectionHelper theFireFlies = new ParticleCollectionHelper("Fireflies", FIREFLY_RENDER);

    private Collections() {

    }

    public static void addWaterRipple(@Nonnull final IWorldReader world, final double x, final double y,
                                               final double z) {
        IParticleMote mote = null;
        if (theRipples.get().canFit()) {
            mote = new MoteWaterRipple(world, x, y, z);
            theRipples.get().addParticle(mote);
        }
    }

    public static void addWaterSpray(@Nonnull final IWorldReader world, final double x, final double y,
                                              final double z, final double dX, final double dY, final double dZ) {
        IParticleMote mote = null;
        if (theSprays.get().canFit()) {
            mote = new MoteWaterSpray(world, x, y, z, dX, dY, dZ);
            theSprays.get().addParticle(mote);
        }
    }

    public static boolean canFitWaterSpray() {
        return theSprays.get().canFit();
    }

    public static void addRainSplash(@Nonnull final IWorldReader world, final double x, final double y,
                                              final double z) {
        IParticleMote mote = null;
        if (theSprays.get().canFit()) {
            mote = new MoteRainSplash(world, x, y, z);
            theSprays.get().addParticle(mote);
        }
    }

    public static void addFireFly(@Nonnull final IWorldReader world, final double x, final double y, final double z) {
        IParticleMote mote = null;
        if (theFireFlies.get().canFit()) {
            mote = new MoteFireFly(world, x, y, z);
            theFireFlies.get().addParticle(mote);
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(@Nonnull final WorldEvent.Unload event) {
        if (event.getWorld() instanceof ClientWorld) {
            theFireFlies.clear();
        }
    }

    @SubscribeEvent
    public static void diagnostics(@Nonnull final DiagnosticEvent event) {
        event.getLeft().add(TextFormatting.AQUA + theRipples.toString());
        event.getLeft().add(TextFormatting.AQUA + theSprays.toString());
        event.getLeft().add(TextFormatting.AQUA + theFireFlies.toString());
    }
}
