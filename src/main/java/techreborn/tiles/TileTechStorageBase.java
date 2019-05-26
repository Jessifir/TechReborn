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

package techreborn.tiles;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.network.packet.BlockEntityUpdateS2CPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;




import reborncore.api.IListInfoProvider;
import reborncore.api.IToolDrop;
import reborncore.api.tile.ItemHandlerProvider;
import reborncore.common.tile.TileMachineBase;
import reborncore.common.util.Inventory;
import reborncore.common.util.ItemUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TileTechStorageBase extends TileMachineBase
		implements ItemHandlerProvider, IToolDrop, IListInfoProvider {

	public final int maxCapacity;
	public final Inventory<TileTechStorageBase> inventory;
	public ItemStack storedItem;

	public TileTechStorageBase(BlockEntityType<?> tileEntityTypeIn, String name, int maxCapacity) {
		super(tileEntityTypeIn);
		this.maxCapacity = maxCapacity;
		storedItem = ItemStack.EMPTY;
		inventory = new Inventory<>(3, name, maxCapacity, this).withConfiguredAccess();
	}

	public void readWithoutCoords(CompoundTag tagCompound) {

		storedItem = ItemStack.EMPTY;

		if (tagCompound.containsKey("storedStack")) {
			storedItem = ItemStack.fromTag(tagCompound.getCompound("storedStack"));
		}

		if (!storedItem.isEmpty()) {
			storedItem.setAmount(Math.min(tagCompound.getInt("storedQuantity"), this.maxCapacity));
		}

		inventory.read(tagCompound);
	}

	public CompoundTag writeWithoutCoords(CompoundTag tagCompound) {
		if (!storedItem.isEmpty()) {
			ItemStack temp = storedItem.copy();
			if (storedItem.getAmount() > storedItem.getMaxAmount()) {
				temp.setAmount(storedItem.getMaxAmount());
			}
			tagCompound.put("storedStack", temp.toTag(new CompoundTag()));
			tagCompound.putInt("storedQuantity", Math.min(storedItem.getAmount(), maxCapacity));
		} else {
			tagCompound.putInt("storedQuantity", 0);
		}
		inventory.write(tagCompound);
		return tagCompound;
	}

	public ItemStack getDropWithNBT() {
		CompoundTag tileEntity = new CompoundTag();
		ItemStack dropStack = new ItemStack(getBlockType(), 1);
		writeWithoutCoords(tileEntity);
		dropStack.setTag(new CompoundTag());
		dropStack.getTag().put("tileEntity", tileEntity);
		storedItem.setAmount(0);
		inventory.setStackInSlot(1, ItemStack.EMPTY);
		syncWithAll();

		return dropStack;
	}

	public int getStoredCount() {
		return storedItem.getAmount();
	}

	public List<ItemStack> getContentDrops() {
		ArrayList<ItemStack> stacks = new ArrayList<>();

		if (!getStoredItemType().isEmpty()) {
			if (!inventory.getInvStack(1).isEmpty()) {
				stacks.add(inventory.getInvStack(1));
			}
			int size = storedItem.getMaxAmount();
			for (int i = 0; i < getStoredCount() / size; i++) {
				ItemStack droped = storedItem.copy();
				droped.setAmount(size);
				stacks.add(droped);
			}
			if (getStoredCount() % size != 0) {
				ItemStack droped = storedItem.copy();
				droped.setAmount(getStoredCount() % size);
				stacks.add(droped);
			}
		}

		return stacks;
	}

	// TileMachineBase
	@Override
	public void tick() {
		super.tick();
		if (!world.isClient) {
			ItemStack outputStack = ItemStack.EMPTY;
			if (!inventory.getInvStack(1).isEmpty()) {
				outputStack = inventory.getInvStack(1);
			}
			if (!inventory.getInvStack(0).isEmpty()
					&& (storedItem.getAmount() + outputStack.getAmount()) < maxCapacity) {
				ItemStack inputStack = inventory.getInvStack(0);
				if (getStoredItemType().isEmpty()
						|| (storedItem.isEmpty() && ItemUtils.isItemEqual(inputStack, outputStack, true, true))) {

					storedItem = inputStack;
					inventory.setStackInSlot(0, ItemStack.EMPTY);
				} else if (ItemUtils.isItemEqual(getStoredItemType(), inputStack, true, true)) {
					int reminder = maxCapacity - storedItem.getAmount() - outputStack.getAmount();
					if (inputStack.getAmount() <= reminder) {
						setStoredItemCount(inputStack.getAmount());
						inventory.setStackInSlot(0, ItemStack.EMPTY);
					} else {
						setStoredItemCount(maxCapacity - outputStack.getAmount());
						inventory.getInvStack(0).subtractAmount(reminder);
					}
				}
				markDirty();
				syncWithAll();
			}

			if (!storedItem.isEmpty()) {
				if (outputStack.isEmpty()) {

					ItemStack delivered = storedItem.copy();
					delivered.setAmount(Math.min(storedItem.getAmount(), delivered.getMaxAmount()));
					storedItem.subtractAmount(delivered.getAmount());

					if (storedItem.isEmpty()) {
						storedItem = ItemStack.EMPTY;
					}

					inventory.setStackInSlot(1, delivered);
					markDirty();
					syncWithAll();
				} else if (ItemUtils.isItemEqual(storedItem, outputStack, true, true)
						&& outputStack.getAmount() < outputStack.getMaxAmount()) {

					int wanted = Math.min(storedItem.getAmount(),
							outputStack.getMaxAmount() - outputStack.getAmount());
					outputStack.setAmount(outputStack.getAmount() + wanted);
					storedItem.subtractAmount(wanted);

					if (storedItem.isEmpty()) {
						storedItem = ItemStack.EMPTY;
					}
					markDirty();
					syncWithAll();
				}
			}
		}
	}

	@Override
	public boolean canBeUpgraded() {
		return false;
	}

	@Override
	public void onDataPacket(ClientConnection net, BlockEntityUpdateS2CPacket packet) {
		world.markBlockRangeForRenderUpdate(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
		fromTag(packet.getCompoundTag());
	}

	@Override
	public void fromTag(CompoundTag tagCompound) {
		super.fromTag(tagCompound);
		readWithoutCoords(tagCompound);
	}

	@Override
	public CompoundTag toTag(CompoundTag tagCompound) {
		super.toTag(tagCompound);
		writeWithoutCoords(tagCompound);
		return tagCompound;
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {

			return LazyOptional.of(new NonNullSupplier<T>() {
				@Nonnull
				@Override
				public T get() {
					return (T) inventory;
				}
			});
		}
		return super.getCapability(cap);
	}

	// ItemHandlerProvider
	@Override
	public Inventory<TileTechStorageBase> getInventory() {
		return inventory;
	}

	// IToolDrop
	@Override
	public ItemStack getToolDrop(PlayerEntity entityPlayer) {
		return getDropWithNBT();
	}

	// IListInfoProvider
	@Override
	public void addInfo(List<Component> info, boolean isRealTile, boolean hasData) {
		if (isRealTile || hasData) {
			int size = 0;
			String name = "of nothing";
			if (!storedItem.isEmpty()) {
				name = storedItem.getDisplayName().getString();
				size += storedItem.getAmount();
			}
			if (!inventory.getInvStack(1).isEmpty()) {
				name = inventory.getInvStack(1).getDisplayName().getString();
				size += inventory.getInvStack(1).getAmount();
			}
			info.add(new TextComponent(size + " " + name));
		}
	}

	public ItemStack getStoredItemType() {
		return storedItem.isEmpty() ? inventory.getInvStack(1) : storedItem;
	}


	public void setStoredItemCount(int amount) {
		storedItem.addAmount(amount);
		markDirty();
	}

	public void setStoredItemType(ItemStack type, int amount) {
		storedItem = type;
		storedItem.setAmount(amount);
		markDirty();
	}

	public int getMaxStoredCount() {
		return maxCapacity;
	}
}
