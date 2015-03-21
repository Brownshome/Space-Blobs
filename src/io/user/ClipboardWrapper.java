package io.user;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

//taken from http://www.javapractices.com/topic/TopicAction.do?Id=82
public final class ClipboardWrapper {
	public final static ClipboardOwner NULL_OWNER = (clipboard, contents) -> {};

	/**
	 * Place a String on the clipboard, and make this class the
	 * owner of the Clipboard's contents.
	 */
	public static void setClipboardContents(String aString, ClipboardOwner owner){
		StringSelection stringSelection = new StringSelection(aString);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, owner == null ? NULL_OWNER : owner);
	}

	/**
	 * Place a String on the clipboard.
	 */
	public static void setClipboardContents(String aString){
		setClipboardContents(aString, null);
	}

	/**
	 * Get the String residing on the clipboard.
	 *
	 * @return any text found on the Clipboard; if none found, return an
	 * empty String.
	 */
	public static String getClipboardContents() {
		String result = "";
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		//odd: the Object param of getContents is not currently used
		Transferable contents = clipboard.getContents(null);
		boolean hasTransferableText = contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor);

		if (hasTransferableText) try {
			result = (String)contents.getTransferData(DataFlavor.stringFlavor);
		}
		catch (UnsupportedFlavorException | IOException ex){
			Console.warn("Clipboard paste failed", "IO");
			return "";
		}

		return result;
	}
}