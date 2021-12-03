package ui.model;

public class PromotionOptions {
	
	public enum Mode {
		STRICT,
		LOOSE,
		RANDOM
	}
	
	public final Mode promotionMode;
	
	public final boolean allowMountChanges;
	public final boolean allowEnemyOnlyPromotedClasses; //FE4 Only
	
	public final boolean requireCommonWeapon;
	
	public PromotionOptions(Mode mode, boolean allowMountChange, boolean allowEnemyClass, boolean commonWeapon) {
		super();
		this.promotionMode = mode;
		
		this.allowMountChanges = allowMountChange;
		this.allowEnemyOnlyPromotedClasses = allowEnemyClass;
		
		this.requireCommonWeapon = commonWeapon;
	}

}
