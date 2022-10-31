package random.gba.loader;

import java.util.LinkedHashMap;
import java.util.Map;

import fedata.gba.fe8.FE8Data;
import fedata.gba.fe8.PromotionBranch;
import io.FileHandler;
import util.Diff;
import util.DiffCompiler;
import util.FileReadHelper;

public class PromotionDataLoader {

	public static final String RecordKeeperCategoryKey = "Characters";

	private Map<FE8Data.CharacterClass, PromotionBranch> promotionBranches;

	public PromotionDataLoader(FileHandler handler) {
		promotionBranches = new LinkedHashMap<>();
		long baseAddress = FileReadHelper.readAddress(handler, FE8Data.PromotionBranchTablePointer);

		for (FE8Data.CharacterClass currentClass : FE8Data.CharacterClass.values()) { // These are conveniently labeled
																						// in order of class ID.
			int index = currentClass.ID;
			promotionBranches.put(currentClass,
					new PromotionBranch(handler, baseAddress + (index * FE8Data.BytesPerPromotionBranchEntry)));
		}
	}

	public void compileDiffs(DiffCompiler compiler, FileHandler handler) {
		for (PromotionBranch branch : promotionBranches.values()) {
			branch.commitChanges();
		}

		// Write the classes in order, including ones we didn't modify. Those will have
		// to be copied from the handler, since we didn't have objects made for them.
		for (FE8Data.CharacterClass characterClass : FE8Data.CharacterClass.values()) {
			PromotionBranch branch = promotionBranches.get(characterClass);
			compiler.addDiff(new Diff(branch.getAddressOffset(), branch.getData().length, branch.getData(), null));
		}
	}

	public Map<FE8Data.CharacterClass, PromotionBranch> getAllPromotionBranches() {
		return promotionBranches;
	}

}
