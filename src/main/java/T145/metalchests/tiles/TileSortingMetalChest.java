/*******************************************************************************
 * Copyright 2018 T145
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package T145.metalchests.tiles;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Strings;

import T145.metalchests.api.ModSupport;
import T145.metalchests.blocks.BlockMetalChest.ChestType;
import net.blay09.mods.refinedrelocation.api.Capabilities;
import net.blay09.mods.refinedrelocation.api.filter.IRootFilter;
import net.blay09.mods.refinedrelocation.api.grid.ISortingInventory;
import net.blay09.mods.refinedrelocation.capability.CapabilityRootFilter;
import net.blay09.mods.refinedrelocation.capability.CapabilitySimpleFilter;
import net.blay09.mods.refinedrelocation.capability.CapabilitySortingGridMember;
import net.blay09.mods.refinedrelocation.capability.CapabilitySortingInventory;
import net.blay09.mods.refinedrelocation.tile.INameable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackDataLists;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

@Optional.Interface(
		modid = ModSupport.RefinedRelocation.MOD_ID,
		iface = ModSupport.RefinedRelocation.NAMEABLE,
		striprefs = false)
public class TileSortingMetalChest extends TileMetalChest implements INameable {

	private final ISortingInventory sortingInventory = Capabilities.getDefaultInstance(Capabilities.SORTING_INVENTORY);
	private final IRootFilter rootFilter = Capabilities.getDefaultInstance(Capabilities.ROOT_FILTER);

	private String customName = StringUtils.EMPTY;

	public TileSortingMetalChest(ChestType type) {
		super(type);
		this.inventory = new ItemStackHandler(type.getInventorySize()) {

			@Override
			protected void onContentsChanged(int slot) {
				super.onContentsChanged(slot);
				TileSortingMetalChest.this.markDirty();
				sortingInventory.onSlotChanged(slot);
				world.updateComparatorOutputLevel(pos, blockType);
			}
		};
	}

	public TileSortingMetalChest() {
		super();
	}

	public static void registerFixes(DataFixer fixer) {
		fixer.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackDataLists(TileSortingMetalChest.class, new String[] { "Items" }));
	}

	@Override
	public void onLoad() {
		super.onLoad();
		sortingInventory.onLoad(this);
	}

	@Override
	public void update() {
		sortingInventory.onUpdate(this);
		super.update();
	}

	@Override
	public void invalidate() {
		super.invalidate();
		sortingInventory.onInvalidate(this);
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		sortingInventory.onInvalidate(this);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);

		customName = compound.getString("CustomName");
		sortingInventory.deserializeNBT(compound.getCompoundTag("SortingInventory"));

		if (compound.getTagId("RootFilter") == Constants.NBT.TAG_LIST) {
			NBTTagList tagList = compound.getTagList("RootFilter", Constants.NBT.TAG_COMPOUND);
			compound.removeTag("RootFilter");
			NBTTagCompound rootFilter = new NBTTagCompound();
			rootFilter.setTag("FilterList", tagList);
			compound.setTag("RootFilter", rootFilter);
		}

		rootFilter.deserializeNBT(compound.getCompoundTag("RootFilter"));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setString("CustomName", customName);
		compound.setTag("SortingInventory", sortingInventory.serializeNBT());
		compound.setTag("RootFilter", rootFilter.serializeNBT());
		return compound;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
				|| capability == CapabilitySortingInventory.CAPABILITY
				|| capability == CapabilitySortingGridMember.CAPABILITY || capability == CapabilityRootFilter.CAPABILITY
				|| capability == CapabilitySimpleFilter.CAPABILITY || super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return (T) inventory;
		} else if (capability == CapabilitySortingInventory.CAPABILITY
				|| capability == CapabilitySortingGridMember.CAPABILITY) {
			return (T) sortingInventory;
		} else if (capability == CapabilityRootFilter.CAPABILITY || capability == CapabilitySimpleFilter.CAPABILITY) {
			return (T) rootFilter;
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public String getItemHandlerName() {
		return "tile.metalchests:sorting_metal_chest." + type.getName() + ".name";
	}

	@Optional.Method(modid = ModSupport.RefinedRelocation.MOD_ID)
	@Override
	public void setCustomName(String customName) {
		this.customName = customName;
	}

	@Optional.Method(modid = ModSupport.RefinedRelocation.MOD_ID)
	@Override
	public String getCustomName() {
		return customName;
	}

	@Optional.Method(modid = ModSupport.RefinedRelocation.MOD_ID)
	@Override
	public boolean hasCustomName() {
		return !Strings.isNullOrEmpty(customName);
	}

	@Optional.Method(modid = ModSupport.RefinedRelocation.MOD_ID)
	@Override
	public String getUnlocalizedName() {
		return getItemHandlerName();
	}
}
