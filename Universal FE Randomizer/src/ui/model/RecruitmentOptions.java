package ui.model;

import java.util.List;

import fedata.general.FEBase.GameType;
import util.recordkeeper.RecordKeeper;

public class RecruitmentOptions implements RecordableOption {

	public enum ClassMode {
		USE_FILL, USE_SLOT
	}
	
	public final ClassMode classMode;
	
	public final boolean allowCrossGender;
	public final boolean includeLords;
	public final boolean includeThieves;
	public final boolean includeSpecial;
	
	public final boolean includeExtras;
	public final boolean keepDescriptions;

	public RecruitmentOptions(ClassMode classMode, boolean lords, boolean thieves,
							  boolean special, boolean crossGender, boolean includeExtras, boolean keepDescriptions) {
		this.classMode = classMode;
		
		this.includeLords = lords;
		this.includeThieves = thieves;
		this.includeSpecial = special;
		
		this.allowCrossGender = crossGender;
		this.includeExtras = includeExtras;
		this.keepDescriptions = keepDescriptions;
	}

	@Override
	public void record(RecordKeeper rk, GameType type) {
		StringBuilder sb = new StringBuilder();

		if (allowCrossGender) {
			sb.append("Allow Cross-Gender<br>");
		}

		switch (classMode) {
		case USE_FILL:
			sb.append("Use Fill Class<br>");
			break;
		case USE_SLOT:
			sb.append("Use Slot Class<br>");
			break;
		}

		if (includeLords) {
			sb.append("<br>Include Lords");
		}
		if (includeThieves) {
			sb.append("<br>Include Thieves");
		}
		if (includeSpecial) {
			sb.append("<br>Include Special Characters");
		}

		rk.addHeaderItem("Randomize Recruitment", sb.toString());
	}
}
