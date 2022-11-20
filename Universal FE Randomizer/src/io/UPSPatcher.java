package io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import util.DebugPrinter;
import util.FileReadHelper;
import util.WhyDoesJavaNotHaveThese;

public class UPSPatcher {
	private static final DebugPrinter LOGGER = DebugPrinter.forKey(DebugPrinter.Key.UPS);
	public static Boolean applyUPSPatch(String patchFile, String sourceFile, String targetFile, UPSPatcherStatusListener listener) {
		try {
			InputStream stream = UPSPatcher.class.getClassLoader().getResourceAsStream(patchFile);
			Path temp = Files.createTempFile("file", "temp");
			Files.copy(stream, temp, StandardCopyOption.REPLACE_EXISTING);
			stream.close();
			
			if (listener != null) { listener.onMessageUpdate("Opening patch file..."); }
			FileHandler patchHandler = new FileHandler(temp.toString());
			if (listener != null) { listener.onMessageUpdate("Reading Magic number..."); }
			byte[] header = patchHandler.readBytesAtOffset(0, 4);
			if (!WhyDoesJavaNotHaveThese.byteArraysAreEqual(header, new byte[] {0x55, 0x50, 0x53, 0x31})) {
				return false;
			}
			
			if (listener != null) { listener.onMessageUpdate("Reading input length..."); }
			long inputLength = readVariableWidthOffset(patchHandler);
			if (listener != null) { listener.onMessageUpdate("Reading output length..."); }
			long outputLength = readVariableWidthOffset(patchHandler);
			
			if (listener != null) { listener.onMessageUpdate("Loading CRC32s..."); }
			long oldReadOffset = patchHandler.getNextReadOffset();
			long sourceCRC = FileReadHelper.readWord(patchHandler, patchHandler.getFileLength() - 12, false);
			patchHandler.setNextReadOffset(oldReadOffset);
			
			if (listener != null) { listener.onMessageUpdate("Opening source file..."); }
			FileHandler sourceHandler = new FileHandler(sourceFile);
			if (listener != null) { listener.onMessageUpdate("Opening output stream..."); }
			FileOutputStream outputStream = new FileOutputStream(targetFile);
			
			if (inputLength != sourceHandler.getFileLength()) {
				System.err.println("UPS patch failed. Input file length is incorrect.");
				outputStream.close();
				return false;
			}
			if (sourceCRC != sourceHandler.getCRC32()) {
				System.err.println("UPS patch failed. Input checksum is incorrect.");
				outputStream.close();
				return false;
			}
			
			long totalBytesWritten = 0;
			
			LOGGER.log( "Patching UPS file: " + patchFile);
			LOGGER.log( "Input Length:  " + inputLength);
			LOGGER.log( "Expected Result Length: " + outputLength);
			
			long bytesToSkip = 0;
			long lastWrittenOffset = 0;
			
			if (listener != null) { listener.onMessageUpdate("Patching..."); }
			
			while (patchHandler.getNextReadOffset() < patchHandler.getFileLength() - 12) {
				bytesToSkip = readVariableWidthOffset(patchHandler);
				if (lastWrittenOffset + bytesToSkip > outputLength) { continue; }
				
				int sourceBytesLength = 0;
				if (lastWrittenOffset + 1 < inputLength) {
					byte[] sourceBytes = sourceHandler.readBytesAtOffset(lastWrittenOffset, (int)bytesToSkip);
					outputStream.write(sourceBytes);
					totalBytesWritten += sourceBytes.length;
					sourceBytesLength = sourceBytes.length;
				}
				if (sourceBytesLength < bytesToSkip) {
					int difference = (int)bytesToSkip - sourceBytesLength;
					byte[] zeros = new byte[difference];
					for (int i = 0; i < difference; i++) {
						zeros[i] = 0;
					}
					outputStream.write(zeros);
					totalBytesWritten += zeros.length;
				}
				lastWrittenOffset += bytesToSkip;
				
				byte[] delta = patchHandler.continueReadingBytesUpToNextTerminator((int)patchHandler.getFileLength() - 12);
				if (delta == null) {
					if (totalBytesWritten < outputLength) {
						if (totalBytesWritten < inputLength) {
							long differenceRemaining = inputLength - totalBytesWritten;
							byte[] sourceBuffer = new byte[1024];
							while (differenceRemaining > 0) {
								int chunk = (int)Math.min(1024, differenceRemaining);
								differenceRemaining -= chunk;
								
								sourceHandler.readBytesAtOffset(sourceBuffer, totalBytesWritten, chunk);
								outputStream.write(sourceBuffer, 0, chunk);
								totalBytesWritten += chunk;
							}
						} else {
							long differenceRemaining = outputLength - totalBytesWritten;
							while (differenceRemaining > 0) {
								int chunk = 0;
								if (differenceRemaining > Integer.MAX_VALUE) {
									chunk = Integer.MAX_VALUE;
									differenceRemaining -= Integer.MAX_VALUE;
								} else {
									chunk = (int)differenceRemaining;
									differenceRemaining = 0;
								}
								byte[] zeros = new byte[chunk];
								for (int i = 0; i < chunk; i++) {
									zeros[i] = 0;
								}
								
								outputStream.write(zeros);
								totalBytesWritten += zeros.length;
							}
						}
					} else if (totalBytesWritten > outputLength) {
						System.err.println("Warning: Bytes written exceeds expected result size.");
					}
					break;
				}
				int deltaLength = delta.length;
				byte[] sourceBytes = lastWrittenOffset + 1 < inputLength ? sourceHandler.readBytesAtOffset(lastWrittenOffset, deltaLength) : new byte[] {};
				byte[] resultBytes = new byte[deltaLength];
				for (int i = 0; i < deltaLength; i++) {
					byte result = (byte)((delta[i] & 0xFF) ^ (i < sourceBytes.length ? (sourceBytes[i] & 0xFF) : 0));
					outputStream.write(result);
					totalBytesWritten += 1;
					resultBytes[i] = result;
					lastWrittenOffset++;
				}
			}
			
			while (totalBytesWritten < outputLength) {
				outputStream.write(0);
				totalBytesWritten++;
			}
			
			outputStream.close();
			
			long targetCRC = FileReadHelper.readWord(patchHandler, patchHandler.getFileLength() - 8, false);
			
			FileHandler resultHandler = new FileHandler(targetFile);
			long resultCRC = resultHandler.getCRC32();
			resultHandler.close();
			resultHandler = null;
			
			if (targetCRC != resultCRC) {
				System.err.println("Resulting checksum is incorrect. Expected: " + Long.toHexString(targetCRC).toUpperCase() + " Actual: " + Long.toHexString(resultCRC).toUpperCase());
				return false;
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	private static long readVariableWidthOffset(FileHandler handler) {
//		long offset = 0;
//		long shift = 0;
//		
//		for(;;) {
//			byte currentByte = handler.continueReadingNextByte();
//			if ((currentByte & 0x80) != 0) {
//				offset += ((currentByte & 0x7F) << shift) & 0xFFFFFFFFL;
//				break;
//			}
//			offset += ((currentByte | 0x80) << shift) & 0xFFFFFFFFL;
//			shift += 7;
//		}
//		
//		return offset;
		
		long offset = 0;
		long shift = 1;
		
		for (;;) {
			byte currentByte = handler.continueReadingNextByte();
			offset += ((currentByte & 0x7F) * shift) & 0xFFFFFFFFFFFFFFFFL;
			if ((currentByte & 0x80) != 0) { break; }
			shift <<= 7;
			offset += shift;
		}
		
		return offset;
	}
}
