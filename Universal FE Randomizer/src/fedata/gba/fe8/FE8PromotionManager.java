package fedata.gba.fe8;

import java.util.HashMap;
import java.util.Map;

import io.FileHandler;
import util.Diff;
import util.DiffCompiler;
import util.FileReadHelper;

public class FE8PromotionManager {

	private Map<FE8Data.CharacterClass, PromotionBranch> promotionBranches;

	public FE8PromotionManager(FileHandler handler) {
		promotionBranches = new HashMap<FE8Data.CharacterClass, PromotionBranch>();
		long address = FileReadHelper.readAddress(handler, FE8Data.PromotionBranchTablePointer);
		for (FE8Data.CharacterClass currentClass : FE8Data.CharacterClass.values()) { // These are conveniently labeled
																						// in order of class ID.
			int index = currentClass.ID;
			promotionBranches.put(currentClass,
					new PromotionBranch(handler, address + (index * FE8Data.BytesPerPromotionBranchEntry)));
		}
	}

	public Boolean hasPromotions(int baseClassID) {
		return getFirstPromotionOptionClassID(baseClassID) != 0 || getSecondPromotionOptionClassID(baseClassID) != 0;
	}

	public int getFirstPromotionOptionClassID(int baseClassID) {
		FE8Data.CharacterClass baseClass = FE8Data.CharacterClass.valueOf(baseClassID);
		if (baseClass == null) {
			return 0;
		}
		PromotionBranch branch = promotionBranches.get(baseClass);
		if (branch == null) {
			return 0;
		}
		return branch.getFirstPromotion();
	}

	public int getSecondPromotionOptionClassID(int baseClassID) {
		FE8Data.CharacterClass baseClass = FE8Data.CharacterClass.valueOf(baseClassID);
		if (baseClass == null) {
			return 0;
		}
		PromotionBranch branch = promotionBranches.get(baseClass);
		if (branch == null) {
			return 0;
		}
		return branch.getSecondPromotion();
	}

	public void setFirstPromotionOptionForClass(int baseClassID, int firstPromotionClassID) {
		FE8Data.CharacterClass baseClass = FE8Data.CharacterClass.valueOf(baseClassID);
		if (baseClass == null) {
			return;
		}
		FE8Data.CharacterClass promotedClass = FE8Data.CharacterClass.valueOf(firstPromotionClassID);
		if (promotedClass == null) {
			return;
		}

		PromotionBranch branch = promotionBranches.get(baseClass);
		if (branch == null) {
			return;
		}
		branch.setFirstPromotion(promotedClass.ID);
	}

	public void setSecondPromotionOptionForClass(int baseClassID, int secondPromotionClassID) {
		FE8Data.CharacterClass baseClass = FE8Data.CharacterClass.valueOf(baseClassID);
		if (baseClass == null) {
			return;
		}
		FE8Data.CharacterClass promotedClass = FE8Data.CharacterClass.valueOf(secondPromotionClassID);
		if (promotedClass == null) {
			return;
		}

		PromotionBranch branch = promotionBranches.get(baseClass);
		if (branch == null) {
			return;
		}
		branch.setSecondPromotion(promotedClass.ID);
	}

	public void commit() {
		for (PromotionBranch branch : promotionBranches.values()) {
			branch.commitChanges();
		}
	}

	public void compileDiffs(DiffCompiler compiler) {
		for (PromotionBranch branch : promotionBranches.values()) {
			branch.commitChanges();
			if (branch.hasCommittedChanges()) {
				Diff diff = new Diff(branch.getAddressOffset(), branch.getData().length, branch.getData(), null);
				compiler.addDiff(diff);
			}
		}
	}
}
