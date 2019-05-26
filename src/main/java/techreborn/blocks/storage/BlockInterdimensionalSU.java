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

package techreborn.blocks.storage;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import reborncore.common.RebornCoreConfig;
import techreborn.client.EGui;
import techreborn.init.TRContent;
import techreborn.tiles.storage.idsu.TileInterdimensionalSU;

public class BlockInterdimensionalSU extends BlockEnergyStorage {
	
	public BlockInterdimensionalSU() {
		super("IDSU", EGui.IDSU);
	}

	@Override
	public BlockEntity createBlockEntity(BlockView worldIn) {
		return new TileInterdimensionalSU();
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext context) {
		final BlockEntity tile = context.getWorld().getBlockEntity(context.getBlockPos());
		if (tile instanceof TileInterdimensionalSU) {
			((TileInterdimensionalSU) tile).ownerUdid = context.getPlayer().getUuid().toString();
		}
		return this.getDefaultState();
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.onPlaced(world, pos, state, placer, stack);
		BlockEntity tile = world.getBlockEntity(pos);
		if (tile instanceof TileInterdimensionalSU) {
			((TileInterdimensionalSU) tile).ownerUdid = placer.getUuid().toString();
		}
	}
	
	@Override
	public void getDrops(BlockState state, DefaultedList<ItemStack> drops, World world, BlockPos pos, int fortune) {
		if (RebornCoreConfig.wrenchRequired) {
			drops.add(new ItemStack(TRContent.MachineBlocks.ADVANCED.getFrame()));
		} else {
			super.getDrops(state, drops, world, pos, fortune);
		}
	}
}
