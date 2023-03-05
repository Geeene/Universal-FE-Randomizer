package fedata.gba.fe8;

/**
 * Dataclass which models a Promotion Data Entry in the FE8 Promotion Table.
 * This is only valid for FE8. In FE6 and 7 this information is part of the
 * class, as there is only a single promotion.
 */
public class PromotionDataEntry {
	/**
	 * The base class for which the promotions are mapped
	 */
	FE8Data.CharacterClass baseClass;
	
	/**
	 * The branch consisting of two possible Promotions
	 */
	PromotionBranch branch;
}
