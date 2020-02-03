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

package org.orecruncher.environs.fog;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public final class FogResult {

    public static final float DEFAULT_PLANE_SCALE = 0.75F;

    private int fogMode;
    private float start;
    private float end;

    public FogResult() {
        this.fogMode = 0;
        this.start = 0F;
        this.end = 0F;
    }

    public FogResult(final int fogMode, final float distance, final float scale) {
        this.set(fogMode, distance, scale);
    }

    public FogResult(final float start, final float end) {
        this.set(start, end);
    }

    public FogResult(@Nonnull final EntityViewRenderEvent.RenderFogEvent event) {
        this.set(event);
    }

    public void set(final int fogMode, final float distance, final float scale) {
        this.fogMode = fogMode;
        this.start = fogMode < 0 ? 0F : distance * scale;
        this.end = distance;
    }

    public void set(final float start, final float end) {
        this.fogMode = 0;
        this.start = start;
        this.end = end;
    }

    public void set(@Nonnull final EntityViewRenderEvent.RenderFogEvent event) {
        this.set(event.getFogMode(), event.getFarPlaneDistance(), DEFAULT_PLANE_SCALE);
    }

    public int getFogMode() {
        return this.fogMode;
    }

    public float getStart() {
        return this.start;
    }

    public float getEnd() {
        return this.end;
    }

    public boolean isValid(@Nonnull final EntityViewRenderEvent.RenderFogEvent event) {
        return this.end > this.start && event.getFogMode() == this.fogMode;
    }

    @Override
    public String toString() {
        return String.format("[mode: %d, start: %f, end: %f]", this.fogMode, this.start, this.end);
    }

}
