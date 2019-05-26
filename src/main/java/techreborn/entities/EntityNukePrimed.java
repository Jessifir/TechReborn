/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2018 TechReborn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package techreborn.entities;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.PrimedTntEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import reborncore.common.explosion.RebornExplosion;
import reborncore.common.registration.RebornRegister;
import reborncore.common.registration.config.ConfigRegistry;
import techreborn.TechReborn;

/**
 * Created by Mark on 13/03/2016.
 */
@RebornRegister(TechReborn.MOD_ID)
public class EntityNukePrimed extends PrimedTntEntity {

	@ConfigRegistry(config = "misc", category = "nuke", key = "fusetime", comment = "Nuke fuse time (ticks)")
	public static int fuseTime = 400;

	@ConfigRegistry(config = "misc", category = "nuke", key = "radius", comment = "Nuke explision radius")
	public static int radius = 40;

	@ConfigRegistry(config = "misc", category = "nuke", key = "enabled", comment = "Should the nuke explode, set to false to prevent block damage")
	public static boolean enabled = true;

	public EntityNukePrimed(World world) {
		super(world);
		setFuse(EntityNukePrimed.fuseTime);
	}

	public EntityNukePrimed(World world, double x, double y, double z, LivingEntity tntPlacedBy) {
		super(world, x, y, z, tntPlacedBy);
		setFuse(EntityNukePrimed.fuseTime);
	}

	@Override
	public void tick() {
		this.prevX = this.x;
		this.prevY = this.y;
		this.prevZ = this.z;

		if (!this.hasNoGravity()) {
			this.motionY -= 0.03999999910593033D;
		}

		this.move(MovementType.SELF, this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.9800000190734863D;
		this.motionY *= 0.9800000190734863D;
		this.motionZ *= 0.9800000190734863D;

		if (this.onGround) {
			this.motionX *= 0.699999988079071D;
			this.motionZ *= 0.699999988079071D;
			this.motionY *= -0.5D;
		}

		setFuse(getFuseTimer() - 1);

		if (getFuseTimer() <= 0) {
			this.remove();
			if (!this.world.isClient) {
				explodeNuke();
			}
		} else {
			this.method_5713();
			this.world.addParticle(ParticleTypes.SMOKE, this.x, this.y + 0.5D, this.z, 0.0D, 0.0D, 0.0D);
		}
	}

	public void explodeNuke() {
		if (!enabled) {
			return;
		}
		RebornExplosion nukeExplosion = new RebornExplosion(new BlockPos(x, y, z), world, radius);
		nukeExplosion.setLivingBase(getCausingEntity());
		nukeExplosion.explode();
	}
}
