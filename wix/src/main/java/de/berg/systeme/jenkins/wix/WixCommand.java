package de.berg.systeme.jenkins.wix;

import java.io.File;

/***
 * {@link WixCommand} is a wrapper for the different tools of the WiX Toolset for
 * arranging program arguments and the files on which the tools should work.
 * 
 * @author Björn Berg, bjoern.berg@gmx.de
 * @version 1.0
 *
 */
public class WixCommand {
	private String cmd = null;
	
	/***
	 * Constructor for a command without any arguments.
	 * @param executable the executable to call.
	 */
	public WixCommand(File executable) {
		this(executable, (Object[])null);
	}
	
	/***
	 * Constructor for a command with variable arguments.
	 * @param executable the executable to call.
	 * @param args program arguments like switches and files.
	 */
	public WixCommand(File executable, Object...args) {
		createCommand(executable, args);
	}
	
	/***
	 * Constructs a command with the given arguments.
	 * @param executable the executable to call.
	 * @param args variable program arguments.
	 */
	private void createCommand(File executable, Object...args) {
		cmd = executable.getAbsolutePath();
		// Iterate over args if exist
		if (args != null) {
			for (Object obj : args) {
				if (obj instanceof String) {
					cmd = cmd + " " + obj;
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return cmd;
	}
}
