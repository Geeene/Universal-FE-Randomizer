package fedata.gba.fe6;

import java.util.ArrayList;

import fedata.gba.GBAFEChapterUnitData;

public class FE6ChapterUnit extends GBAFEChapterUnitData {
	public FE6ChapterUnit(byte[] data, long originalOffset) {
		super();
		this.originalData = data;
		this.data = data;
		this.originalOffset = originalOffset;
	}

	@Override
	public int getCharacterNumber() {
		return data[0] & 0xFF;
	}

	@Override
	public int getStartingClass() {
		return data[1] & 0xFF;
	}

	@Override
	public void setStartingClass(int classID) {
		data[1] = (byte)(classID & 0xFF);
		wasModified = true;
	}
	
	// Level and alliance are stored in byte 3, but we need to interpret it properly.
	// LLLL LENA
	// L = Level (5 bits, 0 - 31)
	// E = Set if enemy
	// N = Set if NPC
	// A = Set if autolevel
	// Note: playable characters are defined as not enemies and not NPCs.
	public int getStartingLevel() {	
		int value = data[3] & 0xFF;
		return value >> 3;
	}
	
	public void setStartingLevel(int newLevel) {
		int levelShifted = (newLevel << 3) & 0xF8;
		data[3] = (byte)((byte)levelShifted | (data[3] & 0x7));
		wasModified = true;
	}
	
	public boolean isEnemy() {
		int value = data[3] & 0xFF;
		return (value & 0x4) != 0;
	}
	
	public boolean isNPC() {
		int value = data[3] & 0xFF;
		return (value & 0x2) != 0;
	}
	
	public boolean isAutolevel() {
		int value = data[3] & 0xFF;
		return (value & 0x1) != 0;
	}

	@Override
	public int getLeaderID() {
		return data[2] & 0xFF;
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
	

	@Override
	public void setAIToHeal(Boolean allowAttack) {
		if (allowAttack) { // Based off of late-game sages
			data[12] = 0x0E;
			data[13] = 0x03;
			data[14] = 0x29;
			data[15] = 0x00;
		} else { // Based off of early-game priests
			data[12] = 0x0E;
			data[13] = 0x03;
			data[14] = 0x10;
			data[15] = 0x00;
		}
		wasModified = true;
	}

	@Override
	public void setAIToOnlyAttack(Boolean allowMove) {
		if (allowMove) { // Based off of chapter 1 AI (aggressive minions)
			data[12] = 0x00;
			data[13] = 0x00;
			data[14] = 0x09;
			data[15] = 0x00;
		} else { // Based off of chapter 1 AI (waiting minions)
			data[12] = 0x00;
			data[13] = 0x03;
			data[14] = 0x09; // Some archers use 0x29 here instead. This value changes throughout the
								// chapters, so no idea how it'll perform beyond chapter 1.
			data[15] = 0x00;
		}
		wasModified = true;
	}

	@Override
	public void setUnitToDropLastItem(boolean drop) {
		// We don't support this on FE6 (at least not yet).
	}
}
