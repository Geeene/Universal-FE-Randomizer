package fedata.gcnwii.fe9.scripting;

import io.gcn.GCNCMBFileHandler;

public class NOPInstruction extends ScriptInstruction {
	
	public NOPInstruction() {
		
	}
	
	public String displayString() {
		return "NOP";
	}
	
	public byte[] rawBytes() {
		return new byte[] {0};
	}

	public byte opcode() {
		return 0;
	}
	
	public int numArgBytes() {
		return 0;
	}

	@Override
	public ScriptInstruction createWithArgs(byte[] args, GCNCMBFileHandler handler) {
		return new NOPInstruction();
	}
}
