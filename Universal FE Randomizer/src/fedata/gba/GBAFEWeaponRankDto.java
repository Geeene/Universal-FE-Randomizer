package fedata.gba;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fedata.gba.general.WeaponRank;
import fedata.gba.general.WeaponType;
import fedata.general.FEBase.GameType;
import util.WhyDoesJavaNotHaveThese;

/**
 * Stat DAO for convenient setting / getting of the 7 Main Stats
 */
public class GBAFEWeaponRankDto {
	
	public static final GBAFEWeaponRankDto HIGHEST_RANKS = new GBAFEWeaponRankDto(WeaponRank.S, WeaponRank.S, WeaponRank.S, WeaponRank.S, WeaponRank.S, WeaponRank.S, WeaponRank.S, WeaponRank.S);
	
	private GameType type;
	public WeaponRank sword = WeaponRank.NONE;
	public WeaponRank lance = WeaponRank.NONE;
	public WeaponRank axe = WeaponRank.NONE;
	public WeaponRank bow = WeaponRank.NONE;
	public WeaponRank staff = WeaponRank.NONE;
	public WeaponRank anima = WeaponRank.NONE;
	public WeaponRank light = WeaponRank.NONE;
	public WeaponRank dark = WeaponRank.NONE;
	
	/**
	 * Empty default constructor 
	 */
	public GBAFEWeaponRankDto() {
	}
	
	/**
	 * Copy constructor
	 */
	public GBAFEWeaponRankDto(GBAFEWeaponRankDto other) {
		this.sword = other.sword;
		this.lance = other.lance;
		this.axe   = other.axe;
		this.bow   = other.bow;
		this.staff = other.staff;
		this.anima = other.anima;
		this.light = other.light;
		this.dark  = other.dark;
	}
	
	/**
	 * Constructor with 7 int values, in order hp, str, skl, spd, def, res, lck
	 */
	public GBAFEWeaponRankDto(GameType type, int... args) {
		assert args.length == 8;
		this.type = type;
		
		sword = WeaponRank.valueOf(args[0]);
		lance = WeaponRank.valueOf(args[1]);
		axe   = WeaponRank.valueOf(args[2]);
		bow   = WeaponRank.valueOf(args[3]);
		staff = WeaponRank.valueOf(args[4]);
		anima = WeaponRank.valueOf(args[5]);
		light = WeaponRank.valueOf(args[6]);
		dark  = WeaponRank.valueOf(args[7]);
	}
	/**
	 * Constructor with 7 int values, in order hp, str, skl, spd, def, res, lck
	 */
	public GBAFEWeaponRankDto(WeaponRank... args) {
		assert args.length == 8;

		
		sword = args[0];
		lance = args[1];
		axe   = args[2];
		bow   = args[3];
		staff = args[4];
		anima = args[5];
		light = args[6];
		dark  = args[7];
	}
	
	/**
	 * Returns the stats as a list with stats in order hp, str, skl, spd, def, res, lck
	 */
	public List<WeaponRank> asList(){
		return Arrays.asList(sword, lance, axe, bow, staff, anima, light, dark);
	}
	
	
	public void setRankAtIndex(int index, WeaponRank rank) {
		switch(index) {
			case 0: sword = rank; break;
			case 1: lance = rank; break;
			case 2: axe   = rank; break;
			case 3: bow   = rank; break;
			case 4: staff = rank; break;
			case 5: anima = rank; break;
			case 6: light = rank; break;
			case 7: dark  = rank; break;
		}
	}
	
	
	/**
	 * Override all of the fields with the values from the given other Dto where the weapon rank values aren't 0.
	 * 
	 * This is to emulate the GBAFE Logic, where if the user (other dto) defines a Weapon Rank, it overrides the class weapon rank (this dto)  
	 */
	public GBAFEWeaponRankDto override(GBAFEWeaponRankDto other) {
		if(other.sword != WeaponRank.NONE) {this.sword = other.sword;}
		if(other.lance != WeaponRank.NONE) {this.lance = other.lance;}
		if(other.axe   != WeaponRank.NONE) {this.axe   = other.axe  ;}
		if(other.bow   != WeaponRank.NONE) {this.bow   = other.bow  ;}
		if(other.staff != WeaponRank.NONE) {this.staff = other.staff;}
		if(other.anima != WeaponRank.NONE) {this.anima = other.anima;}
		if(other.light != WeaponRank.NONE) {this.light = other.light;}
		if(other.dark  != WeaponRank.NONE) {this.dark  = other.dark ;}
		return this;
	}
	
	public List<WeaponType> getTypes() {
		List<WeaponType> types = new ArrayList<WeaponType>();
		if (sword != WeaponRank.NONE) { types.add(WeaponType.SWORD); }
		if (lance != WeaponRank.NONE) { types.add(WeaponType.LANCE); }
		if (axe != WeaponRank.NONE) { types.add(WeaponType.AXE); }
		if (bow != WeaponRank.NONE) { types.add(WeaponType.BOW); }
		if (light != WeaponRank.NONE) { types.add(WeaponType.LIGHT); }
		if (dark != WeaponRank.NONE) { types.add(WeaponType.DARK); }
		if (anima != WeaponRank.NONE) { types.add(WeaponType.ANIMA); }
		if (staff != WeaponRank.NONE) { types.add(WeaponType.STAFF); }
		return types;
	}
	
	public WeaponRank rankForType(WeaponType type) {
		switch (type) {
		case SWORD: return sword;
		case LANCE: return lance;
		case AXE: return axe;
		case BOW: return bow;
		case ANIMA: return anima;
		case LIGHT: return light;
		case DARK: return dark;
		case STAFF: return staff;
		default:
			return WeaponRank.NONE;
		}
	}
	
	public void reset() {
		this.sword = WeaponRank.NONE;
		this.lance = WeaponRank.NONE;
		this.axe   = WeaponRank.NONE;
		this.bow   = WeaponRank.NONE;
		this.staff = WeaponRank.NONE;
		this.anima = WeaponRank.NONE;
		this.light = WeaponRank.NONE;
		this.dark  = WeaponRank.NONE;
	}

	
	@Override
	public String toString() {
		return String.format("GBAFEWeaponRankDto: sword %d, lance %d, axe %d, bow %d, staff %d, anima %d, light %d, dark %d", sword, lance, axe, bow, staff, anima, light, dark);
	}
}
