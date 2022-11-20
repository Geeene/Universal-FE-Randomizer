package random.gcnwii.fe9.loader;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import fedata.gcnwii.fe9.FE9Data;
import fedata.gcnwii.fe9.FE9Data.CharacterClass;
import fedata.gcnwii.fe9.FE9Data.Skill;
import fedata.gcnwii.fe9.FE9Skill;
import io.gcn.GCNDataFileHandler;
import io.gcn.GCNDataFileHandlerV2;
import io.gcn.GCNDataFileHandlerV2.GCNDataFileDataSection;
import io.gcn.GCNFileHandler;
import io.gcn.GCNISOException;
import io.gcn.GCNISOHandler;
import util.DebugPrinter;
import util.Diff;
import util.WhyDoesJavaNotHaveThese;

public class FE9SkillDataLoader {
	private static final DebugPrinter LOGGER = DebugPrinter.forKey(DebugPrinter.Key.FE9_SKILL_LOADER);
	List<FE9Skill> allSkills;
	
	Map<String, FE9Skill> skillBySID;
	
	GCNDataFileHandlerV2 fe8databin;
	FE9CommonTextLoader commonTextLoader;
	
	public FE9SkillDataLoader(GCNISOHandler isoHandler, FE9CommonTextLoader commonTextLoader) throws GCNISOException {
		allSkills = new ArrayList<FE9Skill>();
		skillBySID = new HashMap<String, FE9Skill>();
		
		GCNFileHandler handler = isoHandler.handlerForFileWithName(FE9Data.SkillDataFilename);
		assert (handler instanceof GCNDataFileHandlerV2);
		if (handler instanceof GCNDataFileHandlerV2) {
			fe8databin = (GCNDataFileHandlerV2)handler;
		}
		
		this.commonTextLoader = commonTextLoader;
		
		List<String> allDataSections = fe8databin.getSectionNames().stream().filter(name -> name.startsWith(FE9Data.SkillDataSectionPrefix)).collect(Collectors.toList());
		for (String sectionName : allDataSections) {
			GCNDataFileDataSection section = fe8databin.getSectionWithName(sectionName);
			FE9Skill skill = new FE9Skill(section.getRawData(0, (int)section.getLength()), 0);
			allSkills.add(skill);
			
			debugPrintSkill(skill, handler);
			
			String sid = fe8databin.stringForPointer(skill.getSkillIDPointer());
			skillBySID.put(sid, skill);
		}
	}

	private void debugPrintSkill(FE9Skill skill, GCNFileHandler handler) {
		LOGGER.log( "===== Printing Skill =====");
		
		LOGGER.log( "SID: " + getSID(skill));
		LOGGER.log( 
				"Unknown Pointer: 0x" + Long.toHexString(skill.getUnknownPointer()) + 
				" (" + rawBytesStringForPointer(skill.getUnknownPointer(), handler) + ")");
		LOGGER.log( "MSID: " + stringForPointer(skill.getSkillNamePointer(), handler));
		LOGGER.log( "Mess_Help: " + stringForPointer(skill.getHelpText1Pointer(), handler));
		LOGGER.log( "Mess_Help2: " + stringForPointer(skill.getHelpText2Pointer(), handler));
		LOGGER.log( "EID: " + stringForPointer(skill.getEffectIDPointer(), handler));
		
		LOGGER.log( "Skill Number?: " + skill.getSkillNumber());
		LOGGER.log( "Unknown Value 1: " + skill.getUnknownValue1());
		LOGGER.log( "Skill Cost: " + skill.getSkillCost());
		LOGGER.log( "Unknown Value 2: " + skill.getUnknownValue2());
		
		LOGGER.log( "Number of Restrictions: " + skill.getRestrictionCount());
		LOGGER.log( "Item granting skill: " + stringForPointer(pointerAtPointer(skill.getItemIDPointer(), handler), handler));
		if (skill.getRestrictionCount() > 0) {
			List<String> restrictions = new ArrayList<String>();
			for (int i = 0; i < skill.getRestrictionCount(); i++) {
				restrictions.add(stringForPointer(pointerAtPointer(skill.getRestrictionPointer() + (i * 4), handler), handler));
			}
			LOGGER.log( "Restrictions: " + String.join(", ", restrictions));
		} else {
			LOGGER.log( "Restrictions: None");
		}
		
		LOGGER.log( "===== End Printing Skill =====");
	}
	
	public FE9Skill getSkillWithSID(String sid) {
		return skillBySID.get(sid);
	}
	
	public String getSID(FE9Skill skill) {
		assert(fe8databin != null);
		if (skill == null) { return null; }
		return fe8databin.stringForPointer(skill.getSkillIDPointer());
	}
	
	public String getMSID(FE9Skill skill) {
		return fe8databin.stringForPointer(skill.getSkillNamePointer());
	}
	
	public FE9Skill skillWithDisplayName(String displayName) {
		for (FE9Data.Skill skill : FE9Data.Skill.values()) {
			if (skill.getDisplayString().equals(displayName)) {
				return getSkillWithSID(skill.getSID());
			}
		}
		
		return null;
	}
	
	public String displayNameForSkill(FE9Skill skill) {
		if (skill == null || skill.getSkillNamePointer() == 0) { return "(null)"; }
		String resolvedValue = commonTextLoader.textStringForIdentifier(fe8databin.stringForPointer(skill.getSkillNamePointer()));
		if (resolvedValue != null) {
			return resolvedValue;
		} else {
			return "(?)";
		}
	}
	
	public List<FE9Skill> skillList(boolean isForPlayableCharacter) {
		Set<FE9Data.Skill> skills = new HashSet<FE9Data.Skill>();
		skills.addAll(FE9Data.Skill.allValidSkills);
		if (!isForPlayableCharacter) {
			skills.removeAll(FE9Data.Skill.playerOnlySkills);
		}
		
		return skills.stream().sorted(new Comparator<FE9Data.Skill>() {
			@Override
			public int compare(Skill arg0, Skill arg1) {
				return arg0.getSID().compareTo(arg1.getSID());
			}
		}).map(fe9dataskill -> {
			return getSkillWithSID(fe9dataskill.getSID());
		}).collect(Collectors.toList());
	}
	
	public Long pointerForSkill(FE9Skill skill) {
		if (skill == null) { return null; }
		return fe8databin.pointerForString(getSID(skill));
	}
	
	public boolean isModifiableSkill(FE9Skill skill) {
		return FE9Data.Skill.withSID(getSID(skill)).isModifiable();
	}
	
	public boolean isOccultSkill(FE9Skill skill) {
		return FE9Data.Skill.withSID(getSID(skill)).isOccult();
	}
	
	public FE9Skill occultSkillForJID(String jid) {
		FE9Data.CharacterClass charClass = FE9Data.CharacterClass.withJID(jid);
		if (charClass == null) { return null; }
		FE9Data.Skill occultSkill = FE9Data.Skill.occultSkillForClass(charClass);
		if (occultSkill == null) { return null; }
		return getSkillWithSID(occultSkill.getSID());
	}
	
	public List<FE9Skill> requiredSkillsForJID(String jid) {
		if (jid == null) { return null; }
		List<FE9Skill> skills = new ArrayList<FE9Skill>();
		FE9Data.CharacterClass charClass = FE9Data.CharacterClass.withJID(jid);
		
		if (charClass == CharacterClass.W_HERON) { skills.add(getSkillWithSID(FE9Data.Skill.CANTO.getSID())); }
		else if (charClass == CharacterClass.ELINCIA_FALCON_KNIGHT) { skills.add(getSkillWithSID(FE9Data.Skill.EQUIP_B.getSID())); }
		else if (charClass == CharacterClass.ARCHER || 
				charClass == CharacterClass.SNIPER) { skills.add(getSkillWithSID(FE9Data.Skill.EQUIP_A.getSID())); }
		else if (charClass == CharacterClass.ASSASSIN || 
				charClass == CharacterClass.THIEF ||
				charClass == CharacterClass.SAGE_KNIFE || 
				charClass == CharacterClass.SAGE_KNIFE_F) { 
			skills.add(getSkillWithSID(FE9Data.Skill.EQUIP_KNIFE.getSID()));
			if (charClass == CharacterClass.ASSASSIN || 
				charClass == CharacterClass.THIEF) {
				skills.add(getSkillWithSID(FE9Data.Skill.KEY_0.getSID()));
			}
		}
		return skills;
	}

	private String stringForPointer(long pointer, GCNFileHandler handler) {
		if (pointer == 0) { return "(null)"; }
		handler.setNextReadOffset(pointer);
		byte[] bytes = handler.continueReadingBytesUpToNextTerminator(pointer + 0xFF);
		String identifier = WhyDoesJavaNotHaveThese.stringFromShiftJIS(bytes);
		String resolvedValue = commonTextLoader.textStringForIdentifier(identifier);
		if (resolvedValue != null) {
			return identifier + " (" + resolvedValue + ")";
		} else {
			return identifier;
		}
	}
	
	private long pointerAtPointer(long pointer, GCNFileHandler handler) {
		if (pointer == 0) { return 0; }
		handler.setNextReadOffset(pointer);
		long nextPointer = WhyDoesJavaNotHaveThese.longValueFromByteArray(handler.continueReadingBytes(4), false);
		return nextPointer + 0x20;
	}
	
	private String rawBytesStringForPointer(long pointer, GCNFileHandler handler) {
		if (pointer == 0) { return "(null)"; }
		handler.setNextReadOffset(pointer);
		byte[] bytes = handler.continueReadingBytesUpToNextTerminator(pointer + 0xFF);
		return WhyDoesJavaNotHaveThese.displayStringForBytes(bytes) + " (" + WhyDoesJavaNotHaveThese.stringFromAsciiBytes(bytes) + ")";
	}
	
	public void commit() {
		for (FE9Skill skill : allSkills) {
			skill.commitChanges();
		}
	}
	
	public void compileDiffs(GCNISOHandler isoHandler) {
		for (FE9Skill skill : allSkills) {
			LOGGER.log( "Writing skill: " + getSID(skill));
			skill.commitChanges();
			if (skill.hasCommittedChanges()) {
				GCNDataFileDataSection section = fe8databin.getSectionWithName(getSID(skill));
				fe8databin.writeDataToSection(section, 0, skill.getData());
			}
		}
	}
}
