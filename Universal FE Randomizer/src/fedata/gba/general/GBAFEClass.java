package fedata.gba.general;

public interface GBAFEClass {
	public int getID();

	public String getName();

	public Boolean isLord();

	public Boolean isThief();

	public Boolean isFemale();

	public Boolean isPromoted();

	public Boolean canAttack();
}
