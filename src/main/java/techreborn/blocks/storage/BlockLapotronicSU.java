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
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import reborncore.common.RebornCoreConfig;
import techreborn.client.EGui;
import techreborn.init.TRContent;
import techreborn.tiles.storage.lesu.TileLapotronicSU;

public class BlockLapotronicSU extends BlockEnergyStorage {
	
	public BlockLapotronicSU() {
		super("LESU", EGui.LESU);
	}

	@Override
	public BlockEntity createBlockEntity(BlockView worldIn) {
		return new TileLapotronicSU();
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
