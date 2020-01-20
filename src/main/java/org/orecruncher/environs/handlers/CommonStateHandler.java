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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.BellTileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.orecruncher.environs.library.BiomeLibrary;
import org.orecruncher.environs.library.DimensionLibrary;
import org.orecruncher.environs.scanner.CeilingCoverage;
import org.orecruncher.lib.DayCycle;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.TickCounter;
import org.orecruncher.lib.WorldUtils;
import org.orecruncher.lib.events.DiagnosticEvent;

import javax.annotation.Nonnull;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
class CommonStateHandler extends HandlerBase {

    private static final double VILLAGE_RANGE = 48 * 48;

    protected final CeilingCoverage ceilingCoverage = new CeilingCoverage();

    CommonStateHandler() {
        super("Common State");
    }

    @Override
    public void process(@Nonnull final PlayerEntity player) {

        final long currentTick = TickCounter.getTickCount();
        final CommonState data = CommonState.getData();
        final World world = player.getEntityWorld();

        ceilingCoverage.tick();

        data.clock.update(world);
        data.playerBiome = BiomeLibrary.getPlayerBiome(GameUtils.getPlayer(), false);
        data.truePlayerBiome = BiomeLibrary.getPlayerBiome(GameUtils.getPlayer(), true);
        data.dimensionId = world.getDimension().getType().getId();
        data.dimensionName = world.getProviderName();
        data.dimInfo = DimensionLibrary.getData(world);
        data.playerPosition = GameUtils.getPlayer().getPosition();
        data.playerEyePosition = GameUtils.getPlayer().getEyePosition(1F);
        data.dayCycle = DayCycle.getCycle(world);
        data.inside = ceilingCoverage.isReallyInside();
        data.biomeTemperature = WorldUtils.getTemperatureAt(world, data.playerPosition);

        data.isUnderground = data.playerBiome == BiomeLibrary.UNDERGROUND_INFO;
        data.isInSpace = data.playerBiome == BiomeLibrary.OUTERSPACE_INFO;
        data.isInClouds = data.playerBiome == BiomeLibrary.CLOUDS_INFO;

        final int blockLight = world.getLightFor(LightType.BLOCK, data.playerPosition);
        final int skyLight = world.getLightFor(LightType.SKY, data.playerPosition) - world.getLightSubtracted(data.playerPosition, 0);
        data.lightLevel = Math.max(blockLight, skyLight);

        // Only check once a second
        if (currentTick % 20 == 0) {
            // Only for surface worlds
            if (world.getDimension().isSurfaceWorld()) {
                // Villages changed with 1.14.  There is no independent tracking of village centers.  Everything is centered
                // on village bells.  Determine village based on that.
                final Optional<BellTileEntity> bell = GameUtils.getWorld().loadedTileEntityList.stream()
                        .filter(te -> te instanceof BellTileEntity)
                        .map(te -> (BellTileEntity) te)
                        .filter(bte -> bte.getDistanceSq(data.playerEyePosition.x, data.playerEyePosition.y, data.playerEyePosition.z) <= VILLAGE_RANGE)
                        .findAny();

                data.isInVillage = bell.isPresent();
            } else {
                data.isInVillage = false;
            }
        }

        // Resets cached script variables so they are updated
        ConditionEvaluator.INSTANCE.tick();
    }

    private final static String[] scripts = {
            "'Dim: ' + dim.getId() + '/' + dim.getDimName()",
            "'Biome: ' + biome.getName() + '(' + biome.getId() + '); Temp ' + biome.getTemperature() + '/' + state.getCurrentTemperature() + ' rainfall: ' + biome.getRainfall() + ' traits: ' + biome.getTraits()",
            "'Weather: ' + lib.iif(weather.isRaining(),'rainfall: ' + weather.getRainFall(),'not raining') + lib.iif(weather.isThundering(),' thundering','') + ' Temp: ' + weather.getTemperature() + ' ice: ' + lib.iif(weather.getTemperature() < 0.15, 'true', 'false') + ' ' + lib.iif(weather.getTemperature() < 0.2, '(breath)', '')",
            "'Diurnal: ' + lib.iif(diurnal.isNight(),' night,',' day,') + lib.iif(state.isInside(),' inside,',' outside,') + ' celestial angle: ' + diurnal.getCelestialAngle()",
            "'Player: health ' + player.getHealth() + '/' + player.getMaxHealth() + ' pos: (' + player.getX() + ',' + player.getY() + ',' + player.getZ() + ') light: ' + state.getLightLevel()",
            "'Village: ' + state.isInVillage()"
    };

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void diagnostics(@Nonnull final DiagnosticEvent event) {
        for (final String s : scripts) {
            final String result = ConditionEvaluator.INSTANCE.eval(s).toString();
            event.getLeft().add(TextFormatting.YELLOW + result);
        }
    }

}
