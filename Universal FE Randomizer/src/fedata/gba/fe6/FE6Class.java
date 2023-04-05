package fedata.gba.fe6;

import java.util.Arrays;

import fedata.gba.GBAFEClassData;
import fedata.gba.GBAFEItemData;
import fedata.gba.general.WeaponRank;
import fedata.gba.general.WeaponType;
import fedata.general.FEBase.GameType;

public class FE6Class extends GBAFEClassData {

	int promoHP;
	int promoSTR;
	int promoSKL;
	int promoSPD;
	int promoDEF;
	int promoRES;

	public FE6Class(GBAFEClassData reference) {
		super(GameType.FE6);
		this.originalData = Arrays.copyOf(reference.getData(), reference.getData().length);
		this.data = Arrays.copyOf(reference.getData(), reference.getData().length);

		this.promoHP = reference.getPromoHP();
		this.promoSTR = reference.getPromoSTR();
		this.promoSKL = reference.getPromoSKL();
		this.promoSPD = reference.getPromoSPD();
		this.promoDEF = reference.getPromoDEF();
		this.promoRES = reference.getPromoRES();
	}

	public FE6Class(byte[] data, long originalOffset, GBAFEClassData demotedClass) {
		super(GameType.FE6);
		this.originalData = data;
		this.data = data;
		this.originalOffset = originalOffset;

		if (demotedClass != null) {
			promoHP = getBaseHP() - demotedClass.getBaseHP();
			promoSTR = getBaseSTR() - demotedClass.getBaseSTR();
			promoSKL = getBaseSKL() - demotedClass.getBaseSKL();
			promoSPD = getBaseSPD() - demotedClass.getBaseSPD();
			promoDEF = getBaseDEF() - demotedClass.getBaseDEF();
			promoRES = getBaseRES() - demotedClass.getBaseRES();
		}
	}

	public int getLCKGrowth() {
		return 0;
	}

	public void setLCKGrowth(int lckGrowth) {
		// FE6 has no luck growth
	}

	public int getPromoHP() {
		return promoHP;
	}

	public int getPromoSTR() {
		return promoSTR;
	}

	public int getPromoSKL() {
		return promoSKL;
	}

	public int getPromoSPD() {
		return promoSPD;
	}

	public int getPromoDEF() {
		return promoDEF;
	}

	public int getPromoRES() {
		return promoRES;
	}

	@Override
	public int getSwordRank() {
		return data[40] & 0xFF;
	}

	@Override
	public void setSwordRank(WeaponRank rank) {
		data[40] = (byte) (rank.fe6RankValue & 0xFF);
		wasModified = true;
	}

	@Override
	public int getLanceRank() {
		return data[41] & 0xFF;
	}

	@Override
	public void setLanceRank(WeaponRank rank) {
		data[41] = (byte) (rank.fe6RankValue & 0xFF);
		wasModified = true;
	}

	@Override
	public int getAxeRank() {
		return data[42] & 0xFF;
	}

	@Override
	public void setAxeRank(WeaponRank rank) {
		data[42] = (byte) (rank.fe6RankValue & 0xFF);
		wasModified = true;
	}

	@Override
	public int getBowRank() {
		return data[43] & 0xFF;
	}

	@Override
	public void setBowRank(WeaponRank rank) {
		data[43] = (byte) (rank.fe6RankValue & 0xFF);
		wasModified = true;
	}

	@Override
	public int getAnimaRank() {
		return data[45] & 0xFF;
	}

	@Override
	public void setAnimaRank(WeaponRank rank) {
		data[45] = (byte) (rank.fe6RankValue & 0xFF);
		wasModified = true;
	}

	@Override
	public int getLightRank() {
		return data[46] & 0xFF;
	}

	@Override
	public void setLightRank(WeaponRank rank) {
		data[46] = (byte) (rank.fe6RankValue & 0xFF);
		wasModified = true;
	}

	@Override
	public int getDarkRank() {
		return data[47] & 0xFF;
	}

	@Override
	public void setDarkRank(WeaponRank rank) {
		data[47] = (byte) (rank.fe6RankValue & 0xFF);
		wasModified = true;
	}

	@Override
	public int getStaffRank() {
		return data[44] & 0xFF;
	}

	@Override
	public void setStaffRank(WeaponRank rank) {
		data[44] = (byte) (rank.fe6RankValue & 0xFF);
		wasModified = true;
	}

	@Override
	public Boolean canUseWeapon(GBAFEItemData weapon) {
		if (weapon == null) {
			return false;
		}

		WeaponType type = weapon.getType();
		return getRankForType(type) != WeaponRank.NONE;
	}

	protected WeaponRank getRankForType(WeaponType type) {
		int rankValue = 0;
		switch (type) {
		case SWORD:
			rankValue = getSwordRank();
			break;
		case LANCE:
			rankValue = getLanceRank();
			break;
		case AXE:
			rankValue = getAxeRank();
			break;
		case BOW:
			rankValue = getBowRank();
			break;
		case ANIMA:
			rankValue = getAnimaRank();
			break;
		case LIGHT:
			rankValue = getLightRank();
			break;
		case DARK:
			rankValue = getDarkRank();
			break;
		case STAFF:
			rankValue = getStaffRank();
			break;
		default:
			rankValue = 0;
		}

		if (rankValue == 0) {
			return WeaponRank.NONE;
		}

		return WeaponRank.valueOf(rankValue);
	}

	public void removeLordLocks() {
		data[38] &= 0xFE;
		wasModified = true;
	}

	public GBAFEClassData createClone() {
		FE6Class clone = new FE6Class(this);
		clone.originalOffset = -1;
		return clone;
	}
}