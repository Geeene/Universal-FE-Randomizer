package ui.model;

import fedata.general.FEBase.GameType;
import util.recordkeeper.RecordKeeper;

import static ui.model.PromotionOptions.Mode.STRICT;

public class PromotionOptions implements RecordableOption {

	public enum Mode {
		STRICT, LOOSE, RANDOM
	}

	public final Mode promotionMode;

	public final Boolean allowMountChanges;
	public final Boolean allowEnemyOnlyPromotedClasses;

	public final Boolean requireCommonWeapon;
	public final Boolean allowMonsterClasses;
	public final Boolean keepSameDamageType;

	// FE6 Only
	public final Boolean allowThiefPromotion;
	public final Boolean keepThiefAbilities;
	public final Boolean universal;

	public PromotionOptions(Mode mode, Boolean allowMountChange, Boolean allowEnemyClass, Boolean commonWeapon,
							Boolean allowMonsterClasses, Boolean keepSameDamageType, Boolean allowThiefPromotion, Boolean keepThiefAbilities, Boolean universal) {
		this.promotionMode = mode;

		this.allowMountChanges = allowMountChange;
		this.allowEnemyOnlyPromotedClasses = allowEnemyClass;

		this.requireCommonWeapon = commonWeapon;
		this.allowMonsterClasses = allowMonsterClasses;
		this.keepSameDamageType = keepSameDamageType;
		this.allowThiefPromotion = allowThiefPromotion;
		this.keepThiefAbilities = keepThiefAbilities;
		this.universal = universal;
	}

	@Override
	public void record(RecordKeeper rk, GameType type) {
		if (GameType.FE4.equals(type)) {
			// GBA only for now
			return;
		}

		if (STRICT.equals(this.promotionMode)) {
			rk.addHeaderItem("Promotion Randomization", "NO");
			return;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("Randomization Mode: ");
		switch (promotionMode) {
			case LOOSE:
				sb.append("Similar promotions").append("<br>");
				sb.append("Allow Mount Change? ").append(allowMountChanges ? "YES" : "NO").append("<br>");
				sb.append("Allow Enemy-only Promotions? ").append(allowEnemyOnlyPromotedClasses ? "YES" : "NO").append("<br>");
				break;
			case RANDOM:
				sb.append("Fully random");
				sb.append("Common Weapon Required? ").append(requireCommonWeapon ? "YES" : "NO").append("<br>");
				sb.append("Keep same damage type? ").append(keepSameDamageType ? "YES" : "NO").append("<br>");
				break;
		}

		if (GameType.FE8.equals(type)) {
			sb.append("Allow Monster promotions? ").append(allowMonsterClasses ? "YES" : "NO").append("<br>");
		} else if(GameType.FE6.equals(type)) {
			sb.append("Allow Thief promotions? ").append(allowThiefPromotion ? "YES" : "NO").append("<br>");
			if (allowThiefPromotion) {
				sb.append("Keep thief abilities? ").append(keepThiefAbilities ? "YES" : "NO").append("<br>");
				if(keepThiefAbilities) {
					sb.append("Universally? ").append(universal ? "YES" : "NO").append("<br>");
				}
			}
		}

		rk.addHeaderItem("Promotion Randomization", sb.toString());
	}

}
