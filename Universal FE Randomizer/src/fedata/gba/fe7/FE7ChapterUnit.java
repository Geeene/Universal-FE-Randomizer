package fedata.gba.fe7;

import java.util.ArrayList;

import fedata.gba.GBAFEChapterUnitData;

public class FE7ChapterUnit extends GBAFEChapterUnitData {

	public FE7ChapterUnit(byte[] data, long originalOffset) {
		super();
		this.originalData = data;
		this.data = data;
		this.originalOffset = originalOffset;
	}

	

	public int getLoadingX() {
		return data[4] & 0xFF;
	}

	public int getLoadingY() {
		return data[5] & 0xFF;
	}
	
	public void setLoadingX(int newX) {
		data[4] = (byte)(newX & 0xFF);
		wasModified = true;
	}
	
	public void setLoadingY(int newY) {
		data[5] = (byte)(newY & 0xFF);
		wasModified = true;
	}
	
	public int getStartingX() {
		return data[6] & 0xFF;
	}
	
	public int getStartingY() {
		return data[7] & 0xFF;
	}
	
	public void setStartingX(int newX) {
		data[6] = (byte)(newX & 0xFF);
		wasModified = true;
	}
	
	public void setStartingY(int newY) {
		data[7] = (byte)(newY & 0xFF);
		wasModified = true;
	}
	
	public int getItem(int i) {
		assert(i>=1 && i<=4);
		
		return data[7+i] & 0xFF;
	}
	
	public void setItem(int i, int itemId) {
		assert(i>=1 && i<=4);
		data[7+i] = (byte)(itemId & 0xFF);
		wasModified = true;
	}

	public int getItem1() {
		return data[8] & 0xFF;
	}

	public void setItem1(int itemID) {
		data[8] = (byte)(itemID & 0xFF);
		wasModified = true;
	}

	public int getItem2() {
		return data[9] & 0xFF;
	}

	public void setItem2(int itemID) {
		data[9] = (byte)(itemID & 0xFF);
		wasModified = true;
	}

	public int getItem3() {
		return data[10] & 0xFF;
	}

	public void setItem3(int itemID) {
		data[10] = (byte)(itemID & 0xFF);
		wasModified = true;
	}

	public int getItem4() {
		return data[11] & 0xFF;
	}

	public void setItem4(int itemID) {
		data[11] = (byte)(itemID & 0xFF);
		wasModified = true;
	}
	
	public void giveItems(int[] itemIDs) {
		ArrayList<Integer> workingIDs = new ArrayList<Integer>();
		for (int i = 0; i < itemIDs.length; i++) {
			if (getItem1() == itemIDs[i] || getItem2() == itemIDs[i] || getItem3() == itemIDs[i] || getItem4() == itemIDs[i]) { continue; }
			workingIDs.add(itemIDs[i]);
		}
		
		if (!workingIDs.isEmpty()) {
			setItem4(workingIDs.remove(0));
			if (!workingIDs.isEmpty()) {
				setItem3(workingIDs.remove(0));
				if (!workingIDs.isEmpty()) {
					setItem2(workingIDs.remove(0));
					if (!workingIDs.isEmpty()) {
						setItem1(workingIDs.remove(0));
					}
				}
			}
		}
		
		collapseItems();
	}
	
	public void giveItem(int itemID) {
		if (getItem1() == 0) {
			setItem1(itemID);
		} else if (getItem2() == 0) {
			setItem2(itemID);
		} else if (getItem3() == 0) {
			setItem3(itemID);
		} else {
			setItem4(itemID);
		}
	}
	
	public void removeItem(int itemID) {
		if (getItem1() == itemID) {
			setItem1(0);
		}
		if (getItem2() == itemID) {
			setItem2(0);
		}
		if (getItem3() == itemID) {
			setItem3(0);
		}
		if (getItem4() == itemID) {
			setItem4(0);
		}
		
		collapseItems();
	}
	
	public boolean hasItem(int itemID) {
		if (getItem1() == itemID) {
			return true;
		}
		if (getItem2() == itemID) {
			return true;
		}
		if (getItem3() == itemID) {
			return true;
		}
		if (getItem4() == itemID) {
			return true;
		}
		
		return false;
	}
	
	public void resetData() {
		data = originalData;
		wasModified = false;
	}
	
	public void commitChanges() {
		if (wasModified) {
			hasChanges = true;
		}
		wasModified = false;
	}
	
	public Boolean hasCommittedChanges() {
		return hasChanges;
	}
	
	public byte[] getData() {
		return data;
	}
	
	public Boolean wasModified() {
		return wasModified;
	}
	
	public long getAddressOffset() {
		return originalOffset;
	}
	
	public void setAIToHeal(Boolean allowAttack) {
		data[12] = (byte) (allowAttack ? 0x0F : 0x0E);
		wasModified = true;
	}

	@Override
	public void setAIToOnlyAttack(Boolean allowMove) {
		data[12] = (byte) (allowMove ? 0x00 : 0x03);
		wasModified = true;
	}

	@Override
	public void setUnitToDropLastItem(boolean drop) {
		if (drop) {
			data[15] |= 0x40;
		} else {
			data[15] &= ~0x40;
		}

		wasModified = true;
	}
}
