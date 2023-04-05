package fedata.gba;

import java.util.Arrays;
import java.util.List;

import util.WhyDoesJavaNotHaveThese;

/**
 * Stat DAO for convenient setting / getting of the 7 Main Stats
 */
public class GBAFEStatDto {
	
	/**
	 * Static GBAFEStatDao which contains the minimum stats that a character may have
	 */
	public static final GBAFEStatDto MINIMUM_STATS = new GBAFEStatDto(10, 0, 0, 0, 0, 0 ,0);
	
	/**
	 * Static GBAFEStatDao which contains only zeroes, usable for Clamping
	 */
	public static final GBAFEStatDto ALL_ZEROS = new GBAFEStatDto(10, 0, 0, 0, 0, 0 ,0);
	
	/**
	 * Static GBAFEStatDao which contains the maximum growths that a character may have
	 */
	public static final GBAFEStatDto MAXIMUM_GROWTHS = new GBAFEStatDto(255, 255, 255, 255, 255, 255, 255);
	
	public int hp;
	public int str;
	public int skl;
	public int spd;
	public int def;
	public int res;
	public int lck;
	
	/**
	 * Empty default constructor 
	 */
	public GBAFEStatDto() {
	}
	
	/**
	 * Copy constructor
	 */
	public GBAFEStatDto(GBAFEStatDto other) {
		this.hp  = other.hp;
		this.str = other.str;
		this.skl = other.skl;
		this.spd = other.spd;
		this.def = other.def;
		this.res = other.res;
		this.lck = other.lck;
	}
	
	/**
	 * accumulation constructor
	 */
	public GBAFEStatDto(List<GBAFEStatDto> bonuses) {
		for (GBAFEStatDto bonus : bonuses) {
			this.add(bonus);
		}
	}
	
	/**
	 * Constructor with 7 int values, in order hp, str, skl, spd, def, res, lck
	 */
	public GBAFEStatDto(int... args) {
		assert args.length == 7;
		
		hp = args[0];
		str = args[1];
		skl = args[2];
		spd = args[3];
		def = args[4];
		res = args[5];
		lck = args[6];
	}
	
	public GBAFEStatDto(int stat) {
		hp  = stat;
		str = stat;
		skl = stat;
		spd = stat;
		def = stat;
		res = stat;
		lck = stat;
	}
	
	/**
	 * Returns the stats as a list with stats in order hp, str, skl, spd, def, res, lck
	 */
	public List<Integer> asList(){
		return Arrays.asList(hp, str, skl, spd, def, res, lck);
	}
	
	/**
	 * Multiplies all the stas with the given multiplier
	 */
	public GBAFEStatDto multiply(double multiplier) {
		this.hp *= multiplier;
		this.str *= multiplier;
		this.skl *= multiplier;
		this.spd *= multiplier;
		this.def *= multiplier;
		this.res *= multiplier;
		this.lck *= multiplier;
		
		return this;
	}
	
	/**
	 * Adds the stats of the given DAO to the current instance
	 */
	public GBAFEStatDto add(GBAFEStatDto other) {
		this.hp += other.hp;
		this.str += other.str;
		this.skl += other.skl;
		this.spd += other.spd;
		this.def += other.def;
		this.res += other.res;
		this.lck += other.lck;
		return this;
	} 
	
	/**
	 * Substracts the stats from the given DAO from the current instance
	 */
	public GBAFEStatDto subtract(GBAFEStatDto other) {
		this.hp  -= other.hp;
		this.str -= other.str;
		this.skl -= other.skl;
		this.spd -= other.spd;
		this.def -= other.def;
		this.res -= other.res;
		this.lck -= other.lck;
		return this;
	} 
	
	
	public int getStatTotal() {
		return hp + str + skl + spd + lck + def + res;
	}
	
	/**
	 * Clamps the stats of the current instance against the given upper and lower values
	 */
	public boolean isBetween(GBAFEStatDto lower, GBAFEStatDto upper) {
		boolean ret = true;
		ret &= WhyDoesJavaNotHaveThese.isValueBetween(this.hp,   lower.hp , upper.hp );
		ret &= WhyDoesJavaNotHaveThese.isValueBetween(this.str,  lower.str, upper.str);
		ret &= WhyDoesJavaNotHaveThese.isValueBetween(this.skl,  lower.skl, upper.skl);
		ret &= WhyDoesJavaNotHaveThese.isValueBetween(this.spd,  lower.spd, upper.spd);
		ret &= WhyDoesJavaNotHaveThese.isValueBetween(this.def,  lower.def, upper.def);
		ret &= WhyDoesJavaNotHaveThese.isValueBetween(this.res,  lower.res, upper.res);
		ret &= WhyDoesJavaNotHaveThese.isValueBetween(this.lck,  lower.lck, upper.lck);
		
		return ret;
	}
	
	/**
	 * Clamps the stats of the current instance against the given upper and lower values
	 */
	public GBAFEStatDto clamp(GBAFEStatDto lower, GBAFEStatDto upper) {
		this.hp =  WhyDoesJavaNotHaveThese.clamp(this.hp,   lower.hp , upper.hp );
		this.str = WhyDoesJavaNotHaveThese.clamp(this.str,  lower.str, upper.str);
		this.skl = WhyDoesJavaNotHaveThese.clamp(this.skl,  lower.skl, upper.skl);
		this.spd = WhyDoesJavaNotHaveThese.clamp(this.spd,  lower.spd, upper.spd);
		this.def = WhyDoesJavaNotHaveThese.clamp(this.def,  lower.def, upper.def);
		this.res = WhyDoesJavaNotHaveThese.clamp(this.res,  lower.res, upper.res);
		this.lck = WhyDoesJavaNotHaveThese.clamp(this.lck,  lower.lck, upper.lck);
		
		return this;
	}
	
	/**
	 * Given a GBAFEStatDAO o1 that logically should have lower stats than another GBAFEStatDAO o2, but doesn't, 
	 * this will return a new DAO with the necessary decreases to get from the higher stat in o1 to the lower one in o2.
	 */
	public static GBAFEStatDto downAdjust(GBAFEStatDto o1, GBAFEStatDto o2) {
		GBAFEStatDto dao = new GBAFEStatDto();
		if (o1.hp  > o2.hp ) { dao.hp  -= o1.hp  - o2.hp ; }
		if (o1.str > o2.str) { dao.str -= o1.str - o2.str; }
		if (o1.skl > o2.skl) { dao.skl -= o1.skl - o2.skl; }
		if (o1.spd > o2.spd) { dao.spd -= o1.spd - o2.spd; }
		if (o1.def > o2.def) { dao.def -= o1.def - o2.def; }
		if (o1.res > o2.res) { dao.res -= o1.res - o2.res; }
		if (o1.lck > o2.lck) { dao.lck -= o1.lck - o2.lck; }
		return dao;
	}
	
	/**
	 * Given a GBAFEStatDAO o1 that logically should have higher stats than another GBAFEStatDAO o2, but doesn't, 
	 * this will return a new DAO with the necessary increases to get from the lower stat in o1 to the higher one in o2.
	 */
	public static GBAFEStatDto upAdjust(GBAFEStatDto o1, GBAFEStatDto o2) {
		GBAFEStatDto dao = new GBAFEStatDto();
		if (o1.hp  < o2.hp ) { dao.hp  += o1.hp  - o2.hp ; }
		if (o1.str < o2.str) { dao.str += o1.str - o2.str; }
		if (o1.skl < o2.skl) { dao.skl += o1.skl - o2.skl; }
		if (o1.spd < o2.spd) { dao.spd += o1.spd - o2.spd; }
		if (o1.def < o2.def) { dao.def += o1.def - o2.def; }
		if (o1.res < o2.res) { dao.res += o1.res - o2.res; }
		if (o1.lck < o2.lck) { dao.lck += o1.lck - o2.lck; }
		return dao;
	}
	
	public void setStatAtIndex(int index, int stat) {
		switch(index) {
			case 0: hp  = stat; break;
			case 1: str = stat; break;
			case 2: skl = stat; break;
			case 3: spd = stat; break;
			case 4: def = stat; break;
			case 5: res = stat; break;
			case 6: lck = stat; break;
		}
	}

	@Override
	public String toString() {
		return String.format("GBAFEStatDAO: hp %d, str %d, skl %d, spd %d, def %d, res %d, lck %d", hp, str, skl, spd, def, res, lck);
	}
}
