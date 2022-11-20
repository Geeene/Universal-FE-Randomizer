package random.gba.randomizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import fedata.gba.GBAFEChapterData;
import fedata.gba.GBAFEChapterItemData;
import fedata.gba.GBAFEChapterUnitData;
import fedata.gba.GBAFECharacterData;
import fedata.gba.GBAFEClassData;
import fedata.gba.GBAFEItemData;
import fedata.gba.GBAFEWorldMapData;
import fedata.gba.GBAFEWorldMapPortraitData;
import fedata.gba.general.WeaponRank;
import fedata.gba.general.WeaponType;
import fedata.general.FEBase.GameType;
import random.gba.loader.ChapterLoader;
import random.gba.loader.CharacterDataLoader;
import random.gba.loader.ClassDataLoader;
import random.gba.loader.ItemDataLoader;
import random.gba.loader.TextLoader;
import random.general.RelativeValueMapper;
import ui.model.ItemAssignmentOptions;
import ui.model.RecruitmentOptions;
import ui.model.RecruitmentOptions.BaseStatAutolevelType;
import ui.model.RecruitmentOptions.ClassMode;
import ui.model.RecruitmentOptions.GrowthAdjustmentMode;
import ui.model.RecruitmentOptions.StatAdjustmentMode;
import util.DebugPrinter;
import util.FreeSpaceManager;
import util.WhyDoesJavaNotHaveThese;

public class RecruitmentRandomizer {
	private static final DebugPrinter LOGGER = DebugPrinter.forKey(DebugPrinter.Key.GBA_RANDOM_RECRUITMENT);
	static final int rngSalt = 911;
	
	public static Map<GBAFECharacterData, GBAFECharacterData> randomizeRecruitment(RecruitmentOptions options, ItemAssignmentOptions inventoryOptions, GameType type, 
			CharacterDataLoader characterData, ClassDataLoader classData, ItemDataLoader itemData, ChapterLoader chapterData, TextLoader textData, FreeSpaceManager freeSpace,
			Random rng) {
		
		// Figure out mapping first.
		List<GBAFECharacterData> characterPool = new ArrayList<GBAFECharacterData>(characterData.canonicalPlayableCharacters(options.includeExtras));
		characterPool.removeIf(character -> (characterData.charactersExcludedFromRandomRecruitment().contains(character)));
		
		if (!options.includeLords) {
			characterPool.removeIf(character -> (characterData.isLordCharacterID(character.getID())));
		}
		if (!options.includeThieves) {
			characterPool.removeIf(character -> (characterData.isThiefCharacterID(character.getID())));
		}
		if (!options.includeSpecial) {
			characterPool.removeIf(character -> (characterData.isSpecialCharacterID(character.getID())));
		}
		
		Map<Integer, GBAFECharacterData> referenceData = characterPool.stream().map(character -> {
			GBAFECharacterData copy = character.createCopy(false);
			copy.lock();
			return copy;
		}).collect(Collectors.toMap(charData -> (charData.getID()), charData -> (charData)));
		
		boolean separateByGender = !options.allowCrossGender;
		
		Map<GBAFECharacterData, GBAFECharacterData> characterMap = new HashMap<GBAFECharacterData, GBAFECharacterData>();
		List<GBAFECharacterData> slotsRemaining = new ArrayList<GBAFECharacterData>(characterPool);
		
		LOGGER.log( "Slots Remaining: " + slotsRemaining.size());
		LOGGER.log( "Pool Size: " + characterPool.size());
		
		// The restrictions here only need to be implemented if we use the fill class.
		// If we're using the slot class, then the class restrictions are no longer needed.
		if (options.classMode == ClassMode.USE_FILL) {
			// Assign fliers first, since they are restricted in where they can end up.
			// The slots are determined by the character, since we know which characters must be flying normally.
			// The pool is determined by the character's new class (if it was randomized). This pool should always be larger than the number of slots
			// since all fliers are required to randomize into flier classes. There might be other characters that randomized into fliers though.
			// All fliers can promote and demote, so we should be ok here for promotions.
			List<GBAFECharacterData> flierSlotsRemaining = slotsRemaining.stream().filter(character -> (characterData.isFlyingCharacter(character.getID()))).collect(Collectors.toList());
			List<GBAFECharacterData> flierPool = characterPool.stream().filter(character -> (classData.isFlying(character.getClassID()))).collect(Collectors.toList());
			LOGGER.log( "Assigning fliers...");
			List<SlotAssignment> assignedSlots = shuffleCharactersInPool(false, separateByGender, flierSlotsRemaining, flierPool, characterMap, referenceData, characterData, classData, textData, rng);
			for (SlotAssignment assignment : assignedSlots) {
				slotsRemaining.removeIf(character -> (character.getID() == assignment.slot.getID()));
				characterPool.removeIf(character -> (character.getID() == assignment.fill.getID()));
			}
			
			LOGGER.log( "Slots Remaining: " + slotsRemaining.size());
			LOGGER.log( "Pool Size: " + characterPool.size());
			
			// Prioritize those with melee/ranged requirements too.
			List<GBAFECharacterData> meleeRequiredSlotsRemaining = slotsRemaining.stream().filter(character -> (characterData.characterIDRequiresMelee(character.getID()))).collect(Collectors.toList());
			List<GBAFECharacterData> meleePool = characterPool.stream().filter(character -> (classData.canSupportMelee(character.getClassID()))).collect(Collectors.toList());
			LOGGER.log( "Assigning Required Melee Units...");
			assignedSlots = shuffleCharactersInPool(false, separateByGender, meleeRequiredSlotsRemaining, meleePool, characterMap, referenceData, characterData, classData, textData, rng);
			for (SlotAssignment assignment : assignedSlots) {
				slotsRemaining.removeIf(character -> (character.getID() == assignment.slot.getID()));
				characterPool.removeIf(character -> (character.getID() == assignment.fill.getID()));
			}
			
			LOGGER.log( "Slots Remaining: " + slotsRemaining.size());
			LOGGER.log( "Pool Size: " + characterPool.size());
			
			List<GBAFECharacterData> rangeRequiredSlotsRemaining = slotsRemaining.stream().filter(character -> (characterData.characterIDRequiresRange(character.getID()))).collect(Collectors.toList());
			List<GBAFECharacterData> rangePool = characterPool.stream().filter(character -> (classData.canSupportRange(character.getClassID()))).collect(Collectors.toList());
			LOGGER.log( "Assigning Required Ranged Units...");
			assignedSlots = shuffleCharactersInPool(false, separateByGender, rangeRequiredSlotsRemaining, rangePool, characterMap, referenceData, characterData, classData, textData, rng);
			for (SlotAssignment assignment : assignedSlots) {
				slotsRemaining.removeIf(character -> (character.getID() == assignment.slot.getID()));
				characterPool.removeIf(character -> (character.getID() == assignment.fill.getID()));
			}
			
			LOGGER.log( "Slots Remaining: " + slotsRemaining.size());
			LOGGER.log( "Pool Size: " + characterPool.size());
			
			// Prioritize anybody that HAS to promote. This usually isn't an issue except for FE6, where the one class that can attack but can't promote is thieves.
			List<GBAFECharacterData> mustPromoteSlots = slotsRemaining.stream().filter(character -> (characterData.mustPromote(character.getID()))).collect(Collectors.toList());
			List<GBAFECharacterData> promotablePool = characterPool.stream().filter(character -> {
				GBAFEClassData charClass = classData.classForID(character.getClassID());
				if (classData.isPromotedClass(charClass.getID()) && classData.canClassDemote(charClass.getID())) {
					// Roll in the requires attack here as well.
					for (GBAFEClassData demotedClass : classData.demotionOptions(charClass.getID())) {
						if (classData.canClassAttack(demotedClass.getID()) == false) { return false; }
					}
					return true;
				} else if (!classData.isPromotedClass(charClass.getID()) && classData.canClassPromote(charClass.getID())) {
					return classData.canClassAttack(charClass.getID()); // Roll in the attack requirement as well.
				}
				return false; // Everything else is not allowed.
			}).collect(Collectors.toList());
			LOGGER.log( "Assigning Required Promotion Slots...");
			assignedSlots = shuffleCharactersInPool(false, separateByGender, mustPromoteSlots, promotablePool, characterMap, referenceData, characterData, classData, textData, rng);
			for (SlotAssignment assignment : assignedSlots) {
				slotsRemaining.removeIf(character -> (character.getID() == assignment.slot.getID()));
				characterPool.removeIf(character -> (character.getID() == assignment.fill.getID()));
			}
			
			LOGGER.log( "Slots Remaining: " + slotsRemaining.size());
			LOGGER.log( "Pool Size: " + characterPool.size());
			
			// Prioritize those that require attack next. This generally means lords.
			// Note: these also have to be able to demote.
			List<GBAFECharacterData> attackingSlotsRemaining = slotsRemaining.stream().filter(character -> (characterData.mustAttack(character.getID()))).collect(Collectors.toList());
			List<GBAFECharacterData> attackingPool = characterPool.stream().filter(character -> {
				GBAFEClassData charClass = classData.classForID(character.getClassID());
				// Promoted class that can demote should check all of their demotion options. Any demotion that can't attack disqualifies the class.
				if (classData.isPromotedClass(charClass.getID()) && classData.canClassDemote(charClass.getID())) {
					for (GBAFEClassData demotedClass : classData.demotionOptions(charClass.getID())) {
						if (classData.canClassAttack(demotedClass.getID()) == false) { return false; }
					}
				}
				return classData.canClassAttack(charClass.getID());
			}).collect(Collectors.toList());
			LOGGER.log( "Assigning Required Attackers...");
			assignedSlots = shuffleCharactersInPool(false, separateByGender, attackingSlotsRemaining, attackingPool, characterMap, referenceData, characterData, classData, textData, rng);
			for (SlotAssignment assignment : assignedSlots) {
				slotsRemaining.removeIf(character -> (character.getID() == assignment.slot.getID()));
				characterPool.removeIf(character -> (character.getID() == assignment.fill.getID()));
			}
			
			LOGGER.log( "Slots Remaining: " + slotsRemaining.size());
			LOGGER.log( "Pool Size: " + characterPool.size());
			
			// Prioritize those that can't demote into valid classes so they don't get left behind.
			// Unlike the other criteria, this one is primarily based off the character's class and not the slot.
			// We only need to do this if the pool is not empty.
			List<GBAFECharacterData> promotedSlotsRemaining = slotsRemaining.stream().filter(character -> (classData.isPromotedClass(character.getClassID()))).collect(Collectors.toList());
			List<GBAFECharacterData> mustBePromotedPool = characterPool.stream().filter(character -> {
				GBAFEClassData charClass = classData.classForID(character.getClassID());
				return !classData.canClassDemote(charClass.getID()) && classData.isPromotedClass(charClass.getID());
			}).collect(Collectors.toList());
			if (!mustBePromotedPool.isEmpty()) {
				LOGGER.log( "Assigning non-demotable classes...");
				assignedSlots = shuffleCharactersInPool(false, separateByGender, promotedSlotsRemaining, mustBePromotedPool, characterMap, referenceData, characterData, classData, textData, rng);
				for (SlotAssignment assignment : assignedSlots) {	
					slotsRemaining.removeIf(character -> (character.getID() == assignment.slot.getID()));
					characterPool.removeIf(character -> (character.getID() == assignment.fill.getID()));
				}
				
				LOGGER.log( "Slots Remaining: " + slotsRemaining.size());
				LOGGER.log( "Pool Size: " + characterPool.size());
			}
		}
		
		// Assign everybody else randomly.
		// We do have to make sure characters that can get assigned can promote/demote if necessary.
		LOGGER.log( "Assigning the remainder of the characters...");
		List<SlotAssignment> assignedSlots = shuffleCharactersInPool(true, separateByGender, slotsRemaining, characterPool, characterMap, referenceData, characterData, classData, textData, rng);
		for (SlotAssignment assignment : assignedSlots) {	
			slotsRemaining.removeIf(character -> (character.getID() == assignment.slot.getID()));
			characterPool.removeIf(character -> (character.getID() == assignment.fill.getID()));
		}
		
		
		LOGGER.log( "Slots Remaining: " + slotsRemaining.size());
		LOGGER.log( "Pool Size: " + characterPool.size());
		
		if (!characterPool.isEmpty()) {
			for (GBAFECharacterData unassigned: characterPool) {
				LOGGER.log( "Unassigned: 0x" + Integer.toHexString(unassigned.getID()) + " (" + textData.getStringAtIndex(unassigned.getNameIndex(), true) + ")");
			}
		}
		
		assert characterPool.isEmpty() : "Unable to satisfy all constraints for random recruitment.";
		
		Map<String, String> textReplacements = new HashMap<String, String>();
		
		// Process every mapped character.
		// The fill should always be reference data, so it will not have changed from earlier substitutions.
		for (GBAFECharacterData slot : characterMap.keySet()) {
			GBAFECharacterData fill = characterMap.get(slot);
			if (fill != null) {
				// Track the text changes before we change anything.
				// Face IDs
				// Some games have multiple portraits per character. Replace all of them (think Eliwood's many faces in FE7).
				if (textData.allowTextChanges) {
					if (characterData.multiPortraitsForCharacter(slot.getID()).isEmpty()) {
						textReplacements.put("[LoadFace][0x" + Integer.toHexString(slot.getFaceID()) + "]", "[LoadFace][0x" + Integer.toHexString(fill.getFaceID()) + "]");
					} else {
						for (int faceID : characterData.multiPortraitsForCharacter(slot.getID())) {
							textReplacements.put("[LoadFace][0x" + Integer.toHexString(faceID) + "]", "[LoadFace][0x" + Integer.toHexString(fill.getFaceID()) + "]");
						}
					}
					textReplacements.put(textData.getStringAtIndex(slot.getNameIndex(), true).trim(), textData.getStringAtIndex(fill.getNameIndex(), true).trim());
					textReplacements.put(textData.getStringAtIndex(slot.getNameIndex(), true).toUpperCase().trim(), textData.getStringAtIndex(fill.getNameIndex(), true).trim()); // Sometimes people yell too. :(
					// TODO: pronouns?
				}
				
				// Apply the change to the data.
				fillSlot(options, inventoryOptions, slot, fill, characterData, classData, itemData, chapterData, textData, type, rng);
			}
		}
		
		// Run through the text and modify portraits and names in text.
		
		if (textData.allowTextChanges) {
			// Build tokens for pattern
			String patternString = "(" + patternStringFromReplacements(textReplacements) + ")";
			Pattern pattern = Pattern.compile(patternString);
						
			for (int i = 0; i < textData.getStringCount(); i++) {
				String originalStringWithCodes = textData.getStringAtIndex(i, false);
				
				String workingString = new String(originalStringWithCodes);
				Matcher matcher = pattern.matcher(workingString);
				StringBuffer sb = new StringBuffer();
				while (matcher.find()) {
					String capture = matcher.group(1);
					String replacementKey = textReplacements.get(capture);
					if (replacementKey == null) {
						// Strip out any stuttering.
						String truncated = capture.substring(capture.lastIndexOf('-') + 1);
						replacementKey = textReplacements.get(truncated);
					}
					matcher.appendReplacement(sb, replacementKey);
				}
				
				matcher.appendTail(sb);
				
				textData.setStringAtIndex(i, sb.toString());
			}
		}
		
		for (GBAFEWorldMapData worldMapEvent : chapterData.allWorldMapEvents()) {
			for (GBAFEWorldMapPortraitData portrait : worldMapEvent.allPortraits()) {
				for (GBAFECharacterData slot : characterMap.keySet()) {
					GBAFECharacterData slotReference = referenceData.get(slot.getID());
					GBAFECharacterData fill = characterMap.get(slot);
					if (portrait.getFaceID() == slotReference.getFaceID()) {
						portrait.setFaceID(fill.getFaceID());
						break;
					}
				}
			}
		}
		
		return characterMap;
	}
	
	private static String patternStringFromReplacements(Map<String, String> replacements) {
		StringBuilder sb = new StringBuilder();
		for (String stringToReplace : replacements.keySet()) {
			boolean isControlCode = stringToReplace.charAt(0) == '[';
			
			if (!isControlCode) { sb.append("\\b[" + stringToReplace.charAt(0) + "-]*"); } // Removes any stuttering (ala "E-E-Eliwood!")
			sb.append(Pattern.compile(stringToReplace.replace("[",  "\\[").replace("]", "\\]"), Pattern.LITERAL));
			if (!isControlCode) { sb.append("\\b"); }
			sb.append('|');
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
	
	private static class SlotAssignment {
		GBAFECharacterData slot;
		GBAFECharacterData fill;
		
		private SlotAssignment(GBAFECharacterData slot, GBAFECharacterData fill) {
			this.slot = slot;
			this.fill = fill;
		}
	}
	
	private static List<SlotAssignment> shuffleCharactersInPool(boolean assignAll, boolean separateByGender, List<GBAFECharacterData> slots, List<GBAFECharacterData> pool, Map<GBAFECharacterData, GBAFECharacterData> characterMap, Map<Integer, GBAFECharacterData> referenceData, 
			CharacterDataLoader charData, ClassDataLoader classData, TextLoader textData, Random rng) {
		List<SlotAssignment> additions = new ArrayList<SlotAssignment>();
		
		LOGGER.log( "Slots: " + String.join(", ", slots.stream().map(character -> (String.format("%s[%s]", textData.getStringAtIndex(character.getNameIndex(), true), classData.debugStringForClass(character.getClassID())))).collect(Collectors.toList())));
		LOGGER.log( "Pool: " + String.join(", ", pool.stream().map(character -> String.format("%s[%s]", textData.getStringAtIndex(character.getNameIndex(), true), classData.debugStringForClass(character.getClassID()))).collect(Collectors.toList())));
		
		if (separateByGender) {
			List<GBAFECharacterData> femaleSlots = slots.stream().filter(character -> (charData.isFemale(character.getID()))).collect(Collectors.toList());
			List<GBAFECharacterData> femalePool = pool.stream().filter(character -> (charData.isFemale(character.getID()))).collect(Collectors.toList());
			
			List<GBAFECharacterData> maleSlots = slots.stream().filter(character -> (charData.isFemale(character.getID()) == false)).collect(Collectors.toList());
			List<GBAFECharacterData> malePool = pool.stream().filter(character -> (charData.isFemale(character.getID()) == false)).collect(Collectors.toList());
			
			additions.addAll(shuffle(femaleSlots, femalePool, referenceData, classData, textData, rng));
			additions.addAll(shuffle(maleSlots, malePool, referenceData, classData, textData, rng));
			
			if (assignAll) {
				List<GBAFECharacterData> remainingSlots = new ArrayList<GBAFECharacterData>(femaleSlots);
				remainingSlots.addAll(maleSlots);
				List<GBAFECharacterData> remainingPool = new ArrayList<GBAFECharacterData>(femalePool);
				remainingPool.addAll(malePool);
				additions.addAll(shuffle(remainingSlots, remainingPool, referenceData, classData, textData, rng));
			}
		} else {
			additions.addAll(shuffle(slots, pool, referenceData, classData, textData, rng));
		}
		
		for (SlotAssignment assignment : additions) {
			characterMap.put(assignment.slot, assignment.fill);
		}
			
		return additions;
	}
	
	private static List<SlotAssignment> shuffle(List<GBAFECharacterData> slots, List<GBAFECharacterData> pool, Map<Integer, GBAFECharacterData> referenceData, 
			ClassDataLoader classData, TextLoader textData, Random rng) {
		List<SlotAssignment> additions = new ArrayList<SlotAssignment>();
		if (slots.isEmpty()) { return additions; }
		
		LOGGER.log( "Shuffling Slots: " + String.join(", ", slots.stream().map(charData -> (String.format("%s[%s]", textData.getStringAtIndex(charData.getNameIndex(), true), classData.debugStringForClass(charData.getClassID())))).collect(Collectors.toList())));
		LOGGER.log( "Character Pool: " + String.join(", ", pool.stream().map(charData -> String.format("%s[%s]", textData.getStringAtIndex(charData.getNameIndex(), true), classData.debugStringForClass(charData.getClassID()))).collect(Collectors.toList())));
		
		List<GBAFECharacterData> promotedSlots = slots.stream().filter(slot -> (classData.isPromotedClass(slot.getClassID()))).collect(Collectors.toList());
		List<GBAFECharacterData> cantDemotePool = pool.stream().filter(fill -> (classData.isPromotedClass(fill.getClassID()) == true && classData.canClassDemote(fill.getClassID()) == false)).collect(Collectors.toList());
		List<GBAFECharacterData> promotedSlotCandidates = pool.stream().filter(fill -> (classData.isPromotedClass(fill.getClassID()) == true || classData.canClassPromote(fill.getClassID()) == true)).collect(Collectors.toList());
		
		List<GBAFECharacterData> unpromotedSlots = slots.stream().filter(slot -> (classData.isPromotedClass(slot.getClassID()) == false)).collect(Collectors.toList());
		List<GBAFECharacterData> cantPromotePool = pool.stream().filter(fill -> (classData.isPromotedClass(fill.getClassID()) == false && classData.canClassPromote(fill.getClassID()) == false)).collect(Collectors.toList());
		List<GBAFECharacterData> unpromotedSlotCandidates = pool.stream().filter(fill -> (classData.isPromotedClass(fill.getClassID()) == false || classData.canClassDemote(fill.getClassID()) == true)).collect(Collectors.toList());
		
		LOGGER.log( "\tPromoted: " + String.join(", ", promotedSlots.stream().map(charData -> (textData.getStringAtIndex(charData.getNameIndex(), true))).collect(Collectors.toList())));
		LOGGER.log( "\tCan't Demote: " + String.join(", ", cantDemotePool.stream().map(charData -> (textData.getStringAtIndex(charData.getNameIndex(), true))).collect(Collectors.toList())));
		LOGGER.log( "\tUnpromoted: " + String.join(", ", unpromotedSlots.stream().map(charData -> (textData.getStringAtIndex(charData.getNameIndex(), true))).collect(Collectors.toList())));
		LOGGER.log( "\tCan't Promote: " + String.join(", ", cantPromotePool.stream().map(charData -> (textData.getStringAtIndex(charData.getNameIndex(), true))).collect(Collectors.toList())));
		
		LOGGER.log( "\tCandidates for Promoted Slots: " + String.join(", ", promotedSlotCandidates.stream().map(charData -> (textData.getStringAtIndex(charData.getNameIndex(), true))).collect(Collectors.toList())));
		LOGGER.log( "\tCandidates for Unpromoted Slots: " + String.join(", ", unpromotedSlotCandidates.stream().map(charData -> (textData.getStringAtIndex(charData.getNameIndex(), true))).collect(Collectors.toList())));
		
		// If there are those that can't demote, prioritize them first.
		List<SlotAssignment> intermediate = shuffle(promotedSlots, cantDemotePool, referenceData, textData, rng);
		for (SlotAssignment assignment : intermediate) {
			slots.removeIf(currentSlot -> (currentSlot.getID() == assignment.slot.getID()));
			pool.removeIf(currentFill -> (currentFill.getID() == assignment.fill.getID()));
			promotedSlotCandidates.removeIf(currentFill -> (currentFill.getID() == assignment.fill.getID()));
			unpromotedSlotCandidates.removeIf(currentFill -> (currentFill.getID() == assignment.fill.getID()));
		}
		additions.addAll(intermediate);
		
		// Same for any character that can't promote.
		intermediate = shuffle(unpromotedSlots, cantPromotePool, referenceData, textData, rng);
		for (SlotAssignment assignment : intermediate) {
			slots.removeIf(currentSlot -> (currentSlot.getID() == assignment.slot.getID()));
			pool.removeIf(currentFill -> (currentFill.getID() == assignment.fill.getID()));
			promotedSlotCandidates.removeIf(currentFill -> (currentFill.getID() == assignment.fill.getID()));
			unpromotedSlotCandidates.removeIf(currentFill -> (currentFill.getID() == assignment.fill.getID()));
		}
		additions.addAll(intermediate);
		
		// Prioritize slots now using either promoted or promotable candidates in promoted slots.
		intermediate = shuffle(promotedSlots, promotedSlotCandidates, referenceData, textData, rng);
		for (SlotAssignment assignment : intermediate) {
			slots.removeIf(currentSlot -> (currentSlot.getID() == assignment.slot.getID()));
			pool.removeIf(currentFill -> (currentFill.getID() == assignment.fill.getID()));
			unpromotedSlotCandidates.removeIf(currentFill -> (currentFill.getID() == assignment.fill.getID()));
		}
		additions.addAll(intermediate);
		
		// Same for unpromoted slots.
		intermediate = shuffle(unpromotedSlots, unpromotedSlotCandidates, referenceData, textData, rng);
		for (SlotAssignment assignment : intermediate) {
			slots.removeIf(currentSlot -> (currentSlot.getID() == assignment.slot.getID()));
			pool.removeIf(currentFill -> (currentFill.getID() == assignment.fill.getID()));
		}
		additions.addAll(intermediate);
		
		// Anything else left over...
		if (!slots.isEmpty() && !pool.isEmpty()) {
			intermediate = shuffle(slots, pool, referenceData, textData, rng);
			additions.addAll(intermediate);
		}
		
		return additions;
	}
	
	private static List<SlotAssignment> shuffle(List<GBAFECharacterData> slots, List<GBAFECharacterData> candidates, Map<Integer, GBAFECharacterData> referenceData, TextLoader textData, Random rng) {
		List<SlotAssignment> additions = new ArrayList<SlotAssignment>();
		
		while (!slots.isEmpty() && !candidates.isEmpty()) {
			int slotIndex = rng.nextInt(slots.size());
			GBAFECharacterData slot = slots.get(slotIndex);
			int fillIndex = rng.nextInt(candidates.size());
			GBAFECharacterData fill = candidates.get(fillIndex);
			
			GBAFECharacterData reference = referenceData.get(fill.getID());
			
			LOGGER.log( "Assigned slot 0x" + Integer.toHexString(slot.getID()) + " (" + textData.getStringAtIndex(slot.getNameIndex(), true) + 
					") to 0x" + Integer.toHexString(reference.getID()) + " (" + textData.getStringAtIndex(reference.getNameIndex(), true) + ")");
			
			additions.add(new SlotAssignment(slot, reference));
			
			slots.remove(slotIndex);
			candidates.remove(fillIndex);
		}
		
		return additions;
	}

	private static void fillSlot(RecruitmentOptions options, ItemAssignmentOptions inventoryOptions, GBAFECharacterData slot, GBAFECharacterData fill, CharacterDataLoader characterData, ClassDataLoader classData, ItemDataLoader itemData, ChapterLoader chapterData, TextLoader textData, GameType type, Random rng) {
		// Create copy for reference, since we're about to overwrite the slot data.
		// slot is the target for the changes. All changes should be on slot.
		// fill is the source of all of the changes. Fill should NOT be modified.
		GBAFECharacterData slotReference = slot.createCopy(false);
		
		boolean shouldBePromoted = classData.isPromotedClass(slotReference.getClassID());
		boolean isPromoted = classData.isPromotedClass(fill.getClassID());
		
		GBAFEClassData slotSourceClass = classData.classForID(slotReference.getClassID());
		
		GBAFEClassData fillSourceClass = classData.classForID(fill.getClassID());
		GBAFEClassData targetClass = null;
		
		LOGGER.log( "Filling Slot [" + textData.getStringAtIndex(slotReference.getNameIndex(), true) + "](" + textData.getStringAtIndex(slotSourceClass.getNameIndex(), true) + ") with [" +
				textData.getStringAtIndex(fill.getNameIndex(), true) + "](" + textData.getStringAtIndex(fillSourceClass.getNameIndex(), true) + ")");
		
		GBAFECharacterData[] linkedSlots = characterData.linkedCharactersForCharacter(slotReference);
		
		// Used only for FE8 trainees that can promote twice.
		int additionalPromoHP = 0;
		int additionalPromoSTR = 0;
		int additionalPromoSKL = 0;
		int additionalPromoSPD = 0;
		int additionalPromoDEF = 0;
		int additionalPromoRES = 0;
		
		for (GBAFECharacterData linkedSlot : linkedSlots) {
			// Do not modify if they happen to have a different class.
			if (linkedSlot.getClassID() != slotReference.getClassID()) { continue; }
			
			// First, replace the description, and face
			// The name is unnecessary because there's a text find/replace that we apply later.
			linkedSlot.setDescriptionIndex(fill.getDescriptionIndex());
			linkedSlot.setFaceID(fill.getFaceID());
			
			linkedSlot.setIsLord(characterData.isLordCharacterID(slotReference.getID()));
			
			int targetLevel = linkedSlot.getLevel();
			int sourceLevel = fill.getLevel();
			
			LOGGER.log( "Slot level: " + Integer.toString(targetLevel) + "\tFill Level: " + Integer.toString(sourceLevel));
			
			// Handle Promotion/Demotion leveling as necessary
			if (shouldBePromoted) { targetLevel += 10; }
			if (isPromoted) { sourceLevel += 10; }
			
			int levelsToAdd = targetLevel - sourceLevel;
			
			// To make newly created pre-promotes not completely busted (since they probably had higher growths than real pre-promotes)
			// we'll subtract a few levels from their autoleveling amount.
			if (!isPromoted && shouldBePromoted) {
				LOGGER.log( "Dropping 3 additional levels for new prepromotes.");
				levelsToAdd -= 3;
			}
			
			int promoAdjustHP = additionalPromoHP;
			int promoAdjustSTR = additionalPromoSTR;
			int promoAdjustSKL = additionalPromoSKL;
			int promoAdjustSPD = additionalPromoSPD;
			int promoAdjustDEF = additionalPromoDEF;
			int promoAdjustRES = additionalPromoRES;
			
			LOGGER.log( "Adjusted Slot level: " + Integer.toString(targetLevel) + "\tAdjusted Fill Level: " + Integer.toString(sourceLevel) + "\tLevels To Add: " + Integer.toString(levelsToAdd));
			
			if (shouldBePromoted && !isPromoted) {
				LOGGER.log( "Promoting [" + textData.getStringAtIndex(fill.getNameIndex(), true) + "]");
				// Promote Fill.
				if (targetClass == null) {
					List<GBAFEClassData> promotionOptions = classData.promotionOptions(fill.getClassID());
					LOGGER.log( "Promotion Options: [" + String.join(", ", promotionOptions.stream().map(charClass -> (textData.getStringAtIndex(charClass.getNameIndex(), true))).collect(Collectors.toList())) + "]");
					if (!promotionOptions.isEmpty()) {
						targetClass = promotionOptions.get(rng.nextInt(promotionOptions.size()));
						if (!classData.isPromotedClass(targetClass.getID())) {
							// This is really only for FE8. If a trainee switches into a promoted unit, there's two promotions that need to be done.
							promoAdjustHP += targetClass.getPromoHP();
							promoAdjustSTR += targetClass.getPromoSTR();
							promoAdjustSKL += targetClass.getPromoSKL();
							promoAdjustSPD += targetClass.getPromoSPD();
							promoAdjustDEF += targetClass.getPromoDEF();
							promoAdjustRES += targetClass.getPromoRES();
							
							// Save these if we need them for later for additional linked slots after we've determined our class.
							additionalPromoHP = targetClass.getPromoHP();
							additionalPromoSTR = targetClass.getPromoSTR();
							additionalPromoSKL = targetClass.getPromoSKL();
							additionalPromoSPD = targetClass.getPromoSPD();
							additionalPromoDEF = targetClass.getPromoDEF();
							additionalPromoRES = targetClass.getPromoRES();
							
							promotionOptions = classData.promotionOptions(targetClass.getID());
							LOGGER.log( "Promotion Options: [" + String.join(", ", promotionOptions.stream().map(charClass -> (textData.getStringAtIndex(charClass.getNameIndex(), true))).collect(Collectors.toList())) + "]");
							if (!promotionOptions.isEmpty()) {
								targetClass = promotionOptions.get(rng.nextInt(promotionOptions.size()));
								levelsToAdd += 10;
							}
						}
					} else {
						targetClass = fillSourceClass;
					}
					
					if (options.classMode == ClassMode.USE_SLOT) {
						targetClass = slotSourceClass;
					}
					
					LOGGER.log( "Selected Class: " + (targetClass != null ? textData.getStringAtIndex(targetClass.getNameIndex(), true) : "None"));
				}
				
				promoAdjustHP += targetClass.getPromoHP();
				promoAdjustSTR += targetClass.getPromoSTR();
				promoAdjustSKL += targetClass.getPromoSKL();
				promoAdjustSPD += targetClass.getPromoSPD();
				promoAdjustDEF += targetClass.getPromoDEF();
				promoAdjustRES += targetClass.getPromoRES();
				
				// For some reason, some promoted class seem to have lower bases than their unpromoted variants (FE8 lords are an example). If they are lower, adjust upwards.
				if (targetClass.getBaseHP() < fillSourceClass.getBaseHP()) { promoAdjustHP += fillSourceClass.getBaseHP() - targetClass.getBaseHP(); }
				if (targetClass.getBaseSTR() < fillSourceClass.getBaseSTR()) { promoAdjustSTR += fillSourceClass.getBaseSTR() - targetClass.getBaseSTR(); }
				if (targetClass.getBaseSKL() < fillSourceClass.getBaseSKL()) { promoAdjustSKL += fillSourceClass.getBaseSKL() - targetClass.getBaseSKL(); }
				if (targetClass.getBaseSPD() < fillSourceClass.getBaseSPD()) { promoAdjustSPD += fillSourceClass.getBaseSPD() - targetClass.getBaseSPD(); }
				if (targetClass.getBaseDEF() < fillSourceClass.getBaseDEF()) { promoAdjustDEF += fillSourceClass.getBaseDEF() - targetClass.getBaseDEF(); }
				if (targetClass.getBaseRES() < fillSourceClass.getBaseRES()) { promoAdjustRES += fillSourceClass.getBaseRES() - targetClass.getBaseRES(); }
				
				setSlotClass(inventoryOptions, linkedSlot, targetClass, characterData, classData, itemData, textData, chapterData, rng);
			} else if (!shouldBePromoted && isPromoted) {
				LOGGER.log( "Demoting [" + textData.getStringAtIndex(fill.getNameIndex(), true) + "]");
				// Demote Fill.
				if (targetClass == null) {
					List<GBAFEClassData> demotionOptions = classData.demotionOptions(fill.getClassID());
					LOGGER.log( "Demotion Options: [" + String.join(", ", demotionOptions.stream().map(charClass -> (textData.getStringAtIndex(charClass.getNameIndex(), true))).collect(Collectors.toList())) + "]");
					if (!demotionOptions.isEmpty()) {
						targetClass = demotionOptions.get(rng.nextInt(demotionOptions.size()));
					} else {
						targetClass = fillSourceClass;
					}
					
					if (options.classMode == ClassMode.USE_SLOT) {
						targetClass = slotSourceClass;
					}
					
					LOGGER.log( "Selected Class: " + (targetClass != null ? textData.getStringAtIndex(targetClass.getNameIndex(), true) : "None"));
				}
				
				promoAdjustHP = fillSourceClass.getPromoHP() * -1;
				promoAdjustSTR = fillSourceClass.getPromoSTR() * -1;
				promoAdjustSKL = fillSourceClass.getPromoSKL() * -1;
				promoAdjustSPD = fillSourceClass.getPromoSPD() * -1;
				promoAdjustDEF = fillSourceClass.getPromoDEF() * -1;
				promoAdjustRES = fillSourceClass.getPromoRES() * -1;
				
				// For some reason, some promoted class seem to have lower bases than their unpromoted variants (FE8 lords are an example). If our demoted class has higher bases, adjust downwards
				if (targetClass.getBaseHP() > fillSourceClass.getBaseHP()) { promoAdjustHP -= targetClass.getBaseHP() - fillSourceClass.getBaseHP(); }
				if (targetClass.getBaseSTR() > fillSourceClass.getBaseSTR()) { promoAdjustSTR -= targetClass.getBaseSTR() - fillSourceClass.getBaseSTR(); }
				if (targetClass.getBaseSKL() > fillSourceClass.getBaseSKL()) { promoAdjustSKL -= targetClass.getBaseSKL() - fillSourceClass.getBaseSKL(); }
				if (targetClass.getBaseSPD() > fillSourceClass.getBaseSPD()) { promoAdjustSPD -= targetClass.getBaseSPD() - fillSourceClass.getBaseSPD(); }
				if (targetClass.getBaseDEF() > fillSourceClass.getBaseDEF()) { promoAdjustDEF -= targetClass.getBaseDEF() - fillSourceClass.getBaseDEF(); }
				if (targetClass.getBaseRES() > fillSourceClass.getBaseRES()) { promoAdjustRES -= targetClass.getBaseRES() - fillSourceClass.getBaseRES(); }
				
				setSlotClass(inventoryOptions, linkedSlot, targetClass, characterData, classData, itemData, textData, chapterData, rng);
			} else {
				// Transfer as is.
				if (targetClass == null) {
					if (options.classMode == ClassMode.USE_FILL) { targetClass = fillSourceClass; }
					else if (options.classMode == ClassMode.USE_SLOT) { targetClass = slotSourceClass; }
					else {
						// This shouldn't happen, but default to fill.
						targetClass = fillSourceClass;
					}
				}
				LOGGER.log( "No Promotion/Demotion Needed. Class: " + (targetClass != null ? textData.getStringAtIndex(targetClass.getNameIndex(), true) : "None"));
				setSlotClass(inventoryOptions, linkedSlot, targetClass, characterData, classData, itemData, textData, chapterData, rng);
			}
			
			int targetHPGrowth = fill.getHPGrowth();
			int targetSTRGrowth = fill.getSTRGrowth();
			int targetSKLGrowth = fill.getSKLGrowth();
			int targetSPDGrowth = fill.getSPDGrowth();
			int targetDEFGrowth = fill.getDEFGrowth();
			int targetRESGrowth = fill.getRESGrowth();
			int targetLCKGrowth = fill.getLCKGrowth();
			
			if (options.growthMode == GrowthAdjustmentMode.USE_FILL) {
				// Do nothing in this case. This is the default.
			} else if (options.growthMode == GrowthAdjustmentMode.USE_SLOT) {
				// Overwrite with slot growths.
				targetHPGrowth = slot.getHPGrowth();
				targetSTRGrowth = slot.getSTRGrowth();
				targetSKLGrowth = slot.getSKLGrowth();
				targetSPDGrowth = slot.getSPDGrowth();
				targetDEFGrowth = slot.getDEFGrowth();
				targetRESGrowth = slot.getRESGrowth();
				targetLCKGrowth = slot.getLCKGrowth();
			} else if (options.growthMode == GrowthAdjustmentMode.RELATIVE_TO_SLOT) {
				List<Integer> mappedStats = RelativeValueMapper.mappedValues(Arrays.asList(slot.getHPGrowth(), slot.getSTRGrowth(), slot.getSKLGrowth(), slot.getSPDGrowth(), slot.getDEFGrowth(), slot.getRESGrowth(), slot.getLCKGrowth()), 
						Arrays.asList(fill.getHPGrowth(), fill.getSTRGrowth(), fill.getSKLGrowth(), fill.getSPDGrowth(), fill.getDEFGrowth(), fill.getRESGrowth(), fill.getLCKGrowth()));
				targetHPGrowth = mappedStats.get(0);
				targetSTRGrowth = mappedStats.get(1);
				targetSKLGrowth = mappedStats.get(2);
				targetSPDGrowth = mappedStats.get(3);
				targetDEFGrowth = mappedStats.get(4);
				targetRESGrowth = mappedStats.get(5);
				targetLCKGrowth = mappedStats.get(6);
			}
			
			int newHP = 0;
			int newSTR = 0;
			int newSKL = 0;
			int newSPD = 0;
			int newLCK = 0;
			int newDEF = 0;
			int newRES = 0;
			
			if (options.baseMode == StatAdjustmentMode.AUTOLEVEL) {
				
				int hpGrowth = fill.getHPGrowth();
				int strGrowth = fill.getSTRGrowth();
				int sklGrowth = fill.getSKLGrowth();
				int spdGrowth = fill.getSPDGrowth();
				int defGrowth = fill.getDEFGrowth();
				int resGrowth = fill.getRESGrowth();
				int lckGrowth = fill.getLCKGrowth();
				
				if (options.autolevelMode == BaseStatAutolevelType.USE_NEW) {
					hpGrowth = targetHPGrowth;
					strGrowth = targetSTRGrowth;
					sklGrowth = targetSKLGrowth;
					spdGrowth = targetSPDGrowth;
					defGrowth = targetDEFGrowth;
					resGrowth = targetRESGrowth;
					lckGrowth = targetLCKGrowth;
				}
				
				LOGGER.log( "== Stat Adjustment from Class Bases ==");
				LOGGER.log( "HP: " + promoAdjustHP);
				LOGGER.log( "STR: " + promoAdjustSTR);
				LOGGER.log( "SKL: " + promoAdjustSKL);
				LOGGER.log( "SPD: " + promoAdjustSPD);
				LOGGER.log( "DEF: " + promoAdjustDEF);
				LOGGER.log( "RES: " + promoAdjustRES);
				
				// Adjust bases based on level difference and promotion changes.
				int hpDelta = (int)Math.floor((float)(hpGrowth / 100.0) * levelsToAdd) + promoAdjustHP;
				int strDelta = (int)Math.floor((float)(strGrowth / 100.0) * levelsToAdd) + promoAdjustSTR;
				int sklDelta = (int)Math.floor((float)(sklGrowth / 100.0) * levelsToAdd) + promoAdjustSKL;
				int spdDelta = (int)Math.floor((float)(spdGrowth / 100.0) * levelsToAdd) + promoAdjustSPD;
				int lckDelta = (int)Math.floor((float)(lckGrowth / 100.0) * levelsToAdd);
				int defDelta = (int)Math.floor((float)(defGrowth / 100.0) * levelsToAdd) + promoAdjustDEF;
				int resDelta = (int)Math.floor((float)(resGrowth / 100.0) * levelsToAdd) + promoAdjustRES;
				
				LOGGER.log( "== Base Deltas ==");
				LOGGER.log( "HP: " + Integer.toString(hpDelta));
				LOGGER.log( "STR: " + Integer.toString(strDelta));
				LOGGER.log( "SKL: " + Integer.toString(sklDelta));
				LOGGER.log( "SPD: " + Integer.toString(spdDelta));
				LOGGER.log( "DEF: " + Integer.toString(defDelta));
				LOGGER.log( "RES: " + Integer.toString(resDelta));
				LOGGER.log( "LCK: " + Integer.toString(lckDelta));
				
				// Clamp the delta to make sure we're not overflowing caps or underflowing to negative.
				// Clamp the minimum so that people aren't force to 0 base stats, but they can go down as far as 50% of their normal bases.
				// The minimum HP you can start with is 10. This is really only here for Karel, since he'd be at 0 otherwise.
				// He will have 0s in all other stats though.
				int charEffectiveHP = WhyDoesJavaNotHaveThese.clamp(fillSourceClass.getBaseHP() + fill.getBaseHP() + hpDelta, 10, targetClass.getMaxHP());
				int charEffectiveSTR = WhyDoesJavaNotHaveThese.clamp(fillSourceClass.getBaseSTR() + fill.getBaseSTR() + strDelta, 0, targetClass.getMaxSTR());
				int charEffectiveSKL = WhyDoesJavaNotHaveThese.clamp(fillSourceClass.getBaseSKL() + fill.getBaseSKL() + sklDelta, 0, targetClass.getMaxSKL());;
				int charEffectiveSPD = WhyDoesJavaNotHaveThese.clamp(fillSourceClass.getBaseSPD() + fill.getBaseSPD() + spdDelta, 0, targetClass.getMaxSPD());
				int charEffectiveLCK = WhyDoesJavaNotHaveThese.clamp(fillSourceClass.getBaseLCK() + fill.getBaseLCK() + lckDelta, 0, targetClass.getMaxLCK());
				int charEffectiveDEF = WhyDoesJavaNotHaveThese.clamp(fillSourceClass.getBaseDEF() + fill.getBaseDEF() + defDelta, 0, targetClass.getMaxDEF());
				int charEffectiveRES = WhyDoesJavaNotHaveThese.clamp(fillSourceClass.getBaseRES() + fill.getBaseRES() + resDelta, 0, targetClass.getMaxRES());
				
				newHP = charEffectiveHP - targetClass.getBaseHP();
				newSTR = charEffectiveSTR - targetClass.getBaseSTR();
				newSKL = charEffectiveSKL - targetClass.getBaseSKL();
				newSPD = charEffectiveSPD - targetClass.getBaseSPD();
				newLCK = charEffectiveLCK - targetClass.getBaseLCK();
				newDEF = charEffectiveDEF - targetClass.getBaseDEF();
				newRES = charEffectiveRES - targetClass.getBaseRES();
				
				// Add their original bases back into the new value.
				
				LOGGER.log( "== New Bases ==");
				LOGGER.log( "HP: " + Integer.toString(fillSourceClass.getBaseHP()) + " + " + Integer.toString(fill.getBaseHP()) + " -> " + Integer.toString(targetClass.getBaseHP()) + " + " + Integer.toString(newHP));
				LOGGER.log( "STR: " + Integer.toString(fillSourceClass.getBaseSTR()) + " + " + Integer.toString(fill.getBaseSTR()) + " -> " + Integer.toString(targetClass.getBaseSTR()) + " + " + Integer.toString(newSTR));
				LOGGER.log( "SKL: " + Integer.toString(fillSourceClass.getBaseSKL()) + " + " + Integer.toString(fill.getBaseSKL()) + " -> " + Integer.toString(targetClass.getBaseSKL()) + " + " + Integer.toString(newSKL));
				LOGGER.log( "SPD: " + Integer.toString(fillSourceClass.getBaseSPD()) + " + " + Integer.toString(fill.getBaseSPD()) + " -> " + Integer.toString(targetClass.getBaseSPD()) + " + " + Integer.toString(newSPD));
				LOGGER.log( "DEF: " + Integer.toString(fillSourceClass.getBaseDEF()) + " + " + Integer.toString(fill.getBaseDEF()) + " -> " + Integer.toString(targetClass.getBaseDEF()) + " + " + Integer.toString(newDEF));
				LOGGER.log( "RES: " + Integer.toString(fillSourceClass.getBaseRES()) + " + " + Integer.toString(fill.getBaseRES()) + " -> " + Integer.toString(targetClass.getBaseRES()) + " + " + Integer.toString(newRES));
				LOGGER.log( "LCK: " + Integer.toString(fillSourceClass.getBaseLCK()) + " + " + Integer.toString(fill.getBaseLCK()) + " -> " + Integer.toString(targetClass.getBaseLCK()) + " + " + Integer.toString(newLCK));
			} else if (options.baseMode == StatAdjustmentMode.MATCH_SLOT) {
				newHP = linkedSlot.getBaseHP() + slotSourceClass.getBaseHP() - targetClass.getBaseHP();
				newSTR = linkedSlot.getBaseSTR() + slotSourceClass.getBaseSTR() - targetClass.getBaseSTR();
				newSKL = linkedSlot.getBaseSKL() + slotSourceClass.getBaseSKL() - targetClass.getBaseSKL();
				newSPD = linkedSlot.getBaseSPD() + slotSourceClass.getBaseSPD() - targetClass.getBaseSPD();
				newLCK = linkedSlot.getBaseLCK() + slotSourceClass.getBaseLCK() - targetClass.getBaseLCK();
				newDEF = linkedSlot.getBaseDEF() + slotSourceClass.getBaseDEF() - targetClass.getBaseDEF();
				newRES = linkedSlot.getBaseRES() + slotSourceClass.getBaseRES() - targetClass.getBaseRES();
			} else if (options.baseMode == StatAdjustmentMode.RELATIVE_TO_SLOT) {
				newHP = linkedSlot.getBaseHP() + slotSourceClass.getBaseHP() - targetClass.getBaseHP(); // Keep HP the same logic as above.
				
				int slotSTR = linkedSlot.getBaseSTR() + slotSourceClass.getBaseSTR();
				int slotSKL = linkedSlot.getBaseSKL() + slotSourceClass.getBaseSKL();
				int slotSPD = linkedSlot.getBaseSPD() + slotSourceClass.getBaseSPD();
				int slotLCK = linkedSlot.getBaseLCK() + slotSourceClass.getBaseLCK();
				int slotDEF = linkedSlot.getBaseDEF() + slotSourceClass.getBaseDEF();
				int slotRES = linkedSlot.getBaseRES() + slotSourceClass.getBaseRES();
				
				int fillSTR = fill.getBaseSTR() + fillSourceClass.getBaseSTR();
				int fillSKL = fill.getBaseSKL() + fillSourceClass.getBaseSKL();
				int fillSPD = fill.getBaseSPD() + fillSourceClass.getBaseSPD();
				int fillLCK = fill.getBaseLCK() + fillSourceClass.getBaseLCK();
				int fillDEF = fill.getBaseDEF() + fillSourceClass.getBaseDEF();
				int fillRES = fill.getBaseRES() + fillSourceClass.getBaseRES();
				
				List<Integer> mappedStats = RelativeValueMapper.mappedValues(Arrays.asList(slotSTR, slotSKL, slotSPD, slotDEF, slotRES, slotLCK), 
						Arrays.asList(fillSTR, fillSKL, fillSPD, fillDEF, fillRES, fillLCK));
				
				newSTR = Math.max(mappedStats.get(0) - targetClass.getBaseSTR(), -1 * targetClass.getBaseSTR());
				newSKL = Math.max(mappedStats.get(1) - targetClass.getBaseSKL(), -1 * targetClass.getBaseSKL());
				newSPD = Math.max(mappedStats.get(2) - targetClass.getBaseSPD(), -1 * targetClass.getBaseSPD());
				newLCK = Math.max(mappedStats.get(5) - targetClass.getBaseLCK(), -1 * targetClass.getBaseLCK());
				newDEF = Math.max(mappedStats.get(3) - targetClass.getBaseDEF(), -1 * targetClass.getBaseDEF());
				newRES = Math.max(mappedStats.get(4) - targetClass.getBaseRES(), -1 * targetClass.getBaseRES());
			} else {
				assert false : "Invalid stat adjustment mode for random recruitment.";
			}
			
			linkedSlot.setBaseHP(newHP);
			linkedSlot.setBaseSTR(newSTR);
			linkedSlot.setBaseSKL(newSKL);
			linkedSlot.setBaseSPD(newSPD);
			linkedSlot.setBaseLCK(newLCK);
			linkedSlot.setBaseDEF(newDEF);
			linkedSlot.setBaseRES(newRES);
			
			// Transfer growths.
			linkedSlot.setHPGrowth(targetHPGrowth);
			linkedSlot.setSTRGrowth(targetSTRGrowth);
			linkedSlot.setSKLGrowth(targetSKLGrowth);
			linkedSlot.setSPDGrowth(targetSPDGrowth);
			linkedSlot.setDEFGrowth(targetDEFGrowth);
			linkedSlot.setRESGrowth(targetRESGrowth);
			linkedSlot.setLCKGrowth(targetLCKGrowth);
			
			linkedSlot.setConstitution(fill.getConstitution());
			linkedSlot.setAffinityValue(fill.getAffinityValue());
		}
	}
	
	private static void transferWeaponRanks(GBAFECharacterData target, GBAFEClassData sourceClass, GBAFEClassData targetClass, ItemDataLoader itemData, Random rng) {
		
		Map<WeaponType, Integer> rankMap = new HashMap<WeaponType, Integer>();
		
		rankMap.put(WeaponType.SWORD, sourceClass.getSwordRank());
		rankMap.put(WeaponType.LANCE, sourceClass.getLanceRank());
		rankMap.put(WeaponType.AXE, sourceClass.getAxeRank());
		rankMap.put(WeaponType.BOW, sourceClass.getBowRank());
		rankMap.put(WeaponType.ANIMA, sourceClass.getAnimaRank());
		rankMap.put(WeaponType.LIGHT, sourceClass.getLightRank());
		rankMap.put(WeaponType.DARK, sourceClass.getDarkRank());
		rankMap.put(WeaponType.STAFF, sourceClass.getStaffRank());
		
		rankMap.put(WeaponType.SWORD, target.getSwordRank());
		rankMap.put(WeaponType.LANCE, target.getLanceRank());
		rankMap.put(WeaponType.AXE, target.getAxeRank());
		rankMap.put(WeaponType.BOW, target.getBowRank());
		rankMap.put(WeaponType.ANIMA, target.getAnimaRank());
		rankMap.put(WeaponType.LIGHT, target.getLightRank());
		rankMap.put(WeaponType.DARK, target.getDarkRank());
		rankMap.put(WeaponType.STAFF, target.getStaffRank());
		
		List<Integer> rankValues = new ArrayList<Integer>(rankMap.values().stream().filter(rankValue -> (rankValue != 0)).sorted(new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return Integer.compare(o1, o2);
			}
		}).collect(Collectors.toList()));
		
		if (rankValues.isEmpty()) {
			target.setSwordRank(targetClass.getSwordRank());
			target.setLanceRank(targetClass.getLanceRank());
			target.setAxeRank(targetClass.getAxeRank());
			target.setBowRank(targetClass.getBowRank());
			target.setAnimaRank(targetClass.getAnimaRank());
			target.setLightRank(targetClass.getLightRank());
			target.setDarkRank(targetClass.getDarkRank());
			target.setStaffRank(targetClass.getStaffRank());
		
			return;
		}
		
		int targetWeaponUsage = 0;
		if (targetClass.getSwordRank() > 0) { targetWeaponUsage++; }
		if (targetClass.getLanceRank() > 0) { targetWeaponUsage++; }
		if (targetClass.getAxeRank() > 0) { targetWeaponUsage++; }
		if (targetClass.getBowRank() > 0) { targetWeaponUsage++; }
		if (targetClass.getLightRank() > 0) { targetWeaponUsage++; }
		if (targetClass.getDarkRank() > 0) { targetWeaponUsage++; }
		if (targetClass.getAnimaRank() > 0) { targetWeaponUsage++; }
		if (targetClass.getStaffRank() > 0) { targetWeaponUsage++; }
		
		while (rankValues.size() > targetWeaponUsage) {
			rankValues.remove(0); // Remove the lowest rank if we're filling less weapons than we have to work with.
		}
		
		if (targetClass.getSwordRank() > 0) {
			int randomRankValue = rankValues.get(rng.nextInt(rankValues.size()));
			target.setSwordRank(randomRankValue);
			if (rankValues.size() > 1) {
				rankValues.remove((Integer)randomRankValue);
			}
		} else { target.setSwordRank(0); }
		if (targetClass.getLanceRank() > 0) {
			int randomRankValue = rankValues.get(rng.nextInt(rankValues.size()));
			target.setLanceRank(randomRankValue);
			if (rankValues.size() > 1) {
				rankValues.remove((Integer)randomRankValue);
			}
		} else { target.setLanceRank(0); }
		if (targetClass.getAxeRank() > 0) {
			int randomRankValue = rankValues.get(rng.nextInt(rankValues.size()));
			target.setAxeRank(randomRankValue);
			if (rankValues.size() > 1) {
				rankValues.remove((Integer)randomRankValue);
			}
		} else { target.setAxeRank(0); }
		if (targetClass.getBowRank() > 0) {
			int randomRankValue = rankValues.get(rng.nextInt(rankValues.size()));
			target.setBowRank(randomRankValue);
			if (rankValues.size() > 1) {
				rankValues.remove((Integer)randomRankValue);
			}
		} else { target.setBowRank(0); }
		if (targetClass.getAnimaRank() > 0) {
			int randomRankValue = rankValues.get(rng.nextInt(rankValues.size()));
			target.setAnimaRank(randomRankValue);
			if (rankValues.size() > 1) {
				rankValues.remove((Integer)randomRankValue);
			}
		} else { target.setAnimaRank(0); }
		if (targetClass.getLightRank() > 0) {
			int randomRankValue = rankValues.get(rng.nextInt(rankValues.size()));
			target.setLightRank(randomRankValue);
			if (rankValues.size() > 1) {
				rankValues.remove((Integer)randomRankValue);
			}
		} else { target.setLightRank(0); }
		if (targetClass.getDarkRank() > 0) {
			int randomRankValue = rankValues.get(rng.nextInt(rankValues.size()));
			if (itemData.weaponRankFromValue(randomRankValue) == WeaponRank.E) {
				// Dark magic floors on D. There's no E rank dark magic.
				randomRankValue = itemData.weaponRankValueForRank(WeaponRank.D);
			}
			target.setDarkRank(randomRankValue);
			if (rankValues.size() > 1) {
				rankValues.remove((Integer)randomRankValue);
			}
		} else { target.setDarkRank(0); }
		if (targetClass.getStaffRank() > 0) {
			int randomRankValue = rankValues.get(rng.nextInt(rankValues.size()));
			target.setStaffRank(randomRankValue);
			if (rankValues.size() > 1) {
				rankValues.remove((Integer)randomRankValue);
			}
		} else { target.setStaffRank(0); }
	}
	
	private static void setSlotClass(ItemAssignmentOptions inventoryOptions, GBAFECharacterData slot, GBAFEClassData targetClass, CharacterDataLoader characterData, ClassDataLoader classData, ItemDataLoader itemData, TextLoader textData, ChapterLoader chapterData, Random rng) {
		int oldClassID = slot.getClassID();
		GBAFEClassData originalClass = classData.classForID(oldClassID);
		slot.setClassID(targetClass.getID());
		transferWeaponRanks(slot, originalClass, targetClass, itemData, rng);
		
		for (GBAFEChapterData chapter : chapterData.allChapters()) {
			GBAFEChapterItemData reward = chapter.chapterItemGivenToCharacter(slot.getID());
			if (reward != null) {
				GBAFEItemData item = null;
				GBAFEItemData[] prfWeapons = itemData.prfWeaponsForClass(targetClass.getID());
				if (prfWeapons.length > 0) {
					item = prfWeapons[rng.nextInt(prfWeapons.length)];
				} else {
					item = itemData.getRandomWeaponForCharacter(slot, false, false, characterData.isEnemyAtAnyPoint(slot.getID()), inventoryOptions.assignPromoWeapons, inventoryOptions.assignPoisonWeapons, rng);
				}
				
				if (item != null) {
					reward.setItemID(item.getID());
				}
			}
			
			for (GBAFEChapterUnitData unit : chapter.allUnits()) {
				if (unit.getCharacterNumber() == slot.getID()) {
					unit.setStartingClass(targetClass.getID());
					
					// Set Inventory.
					ClassRandomizer.validateCharacterInventory(inventoryOptions, slot, targetClass, unit, characterData.characterIDRequiresRange(slot.getID()), characterData.characterIDRequiresMelee(slot.getID()), characterData, classData, itemData, textData, false, rng);
					if (characterData.isThiefCharacterID(slot.getID())) {
						ClassRandomizer.validateFormerThiefInventory(unit, itemData);
					}
					ClassRandomizer.validateSpecialClassInventory(unit, itemData, rng);
				}
			}
		}
	}
}
