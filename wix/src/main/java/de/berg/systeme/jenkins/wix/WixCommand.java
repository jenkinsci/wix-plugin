package de.berg.systeme.jenkins.wix;

import java.io.File;

/***
 * {@link WixCommand} is a wrapper for the different tools of the WiX Toolset for
 * arranging program arguments and the files on which the tools should work.
 * 
 * @author Bjoern Berg, bjoern.berg@gmx.de
 * @version 1.0
 *
 */
public class WixCommand {
	private StringBuffer cmd = new StringBuffer();
	
	/***
	 * @deprecated use {@link #WixCommand(File, ToolsetSettings, String)}
	 * Constructor for a command without any arguments.
	 * @param executable the executable to call.
	 */
	public WixCommand(File executable) {
		this(executable, (Object[])null);
	}
	
	/***
	 * @deprecated use {@link #WixCommand(File, ToolsetSettings, String)}
	 * Constructor for a command with variable arguments.
	 * @param executable the executable to call.
	 * @param args program arguments like switches and files.
	 */
	public WixCommand(File executable, Object...args) {
		createCommand(executable, args);
	}
	
	public WixCommand(File executable, ToolsetSettings settings, String params) {
		createCommand(executable, params, settings);
	}
	
	/***
	 * @deprecated use {@link #createCommand(File, String, ToolsetSettings)}
	 * Constructs a command with the given arguments.
	 * @param executable the executable to call.
	 * @param args variable program arguments.
	 */
	private void createCommand(File executable, Object...args) {
		cmd.append(executable.getAbsolutePath());
		// Iterate over args if exist
		if (args != null) {
			for (Object obj : args) {
				if (obj instanceof String) {
					cmd.append(" ");
					cmd.append(obj);
				}
			}
		}
	}
	
	private void createCommand(File executable, String params, ToolsetSettings settings) {
		cmd.append(executable.getAbsolutePath());
		cmd.append(" ");
		cmd.append(params);
		appendExtension(settings, Wix.EXT_BAL);
		appendExtension(settings, Wix.EXT_COMPLUS);
		appendExtension(settings, Wix.EXT_DEPENDENCY);
		appendExtension(settings, Wix.EXT_DIFXAPP);
		appendExtension(settings, Wix.EXT_DIRECTX);
		appendExtension(settings, Wix.EXT_FIREWALL);
		appendExtension(settings, Wix.EXT_GAMING);
		appendExtension(settings, Wix.EXT_IIS);
		appendExtension(settings, Wix.EXT_MSMQ);
		appendExtension(settings, Wix.EXT_NETFX);
		appendExtension(settings, Wix.EXT_PS);
		appendExtension(settings, Wix.EXT_SQL);
		appendExtension(settings, Wix.EXT_TAG);
		appendExtension(settings, Wix.EXT_UI);
		appendExtension(settings, Wix.EXT_UTIL);
		appendExtension(settings, Wix.EXT_VS);
	}
	
	private void appendExtension(ToolsetSettings settings, String extension) {
		if (settings.get(extension, false)) {
			cmd.append(" -ext ");
			cmd.append(extension);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return cmd.toString();
	}
}
