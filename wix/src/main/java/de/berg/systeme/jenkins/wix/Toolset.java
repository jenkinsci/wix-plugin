package de.berg.systeme.jenkins.wix;

import hudson.FilePath;
import hudson.model.BuildListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Properties;

/***
 * <p>Toolset checks the existence for the WIX Toolset on the buildsystem. If not available
 * an exception is thrown.</p>
 * <p>It is used to compile and link wsx-files in a batch mannor triggered by the {@link WixToolsetBuilder}.</p>
 * 
 * @author Björn Berg, bjoern.berg@gmx.de
 * @version 1.1
 *
 */
public final class Toolset {
	/***
	 * Name to compilers executable.
	 */
	private static final String COMPILER = "candle.exe";
	/***
	 * Name to linkers executable.
	 */
	private static final String LINKER = "light.exe";
	/***
	 * Installation directory of WIX Toolset.
	 */
	private File installationPath;
	/***
	 * Absolute path to compilers executable.
	 */
	private File CompilerExe;
	/***
	 * Absolute path to linkers executable.
	 */
	private File LinkerExe;
	/***
	 * Reference to {@link BuildListener}.
	 */
	private BuildListener listener;
	/***
	 * Signal that compiling or linking has failed.
	 */
	private boolean failed = false;
	/***
	 * Checks if debug messages are allowed.
	 */
	private boolean debugEnabled = false;
	/***
	 * Source file for compilation. Must be the absolute path.
	 */
	private FilePath sourceFile;
	
	public Toolset() throws ToolsetException {
		this(null, null);
	}
	
	public Toolset(Properties props, BuildListener listener) 
	throws ToolsetException {
		initialize(props, listener);
	}
	
	/***
	 * Initializes the toolset with needed properties.
	 * @param props Properties for toolsets configuration.
	 * @param listener reference to {@link BuildListener} for logging.
	 * @throws ToolsetException is thrown if configuration of the toolset fails.
	 */
	protected void initialize(Properties props, BuildListener listener) 
	throws ToolsetException {
		this.installationPath = new File(props.getProperty("installation.path"));
		this.debugEnabled = Boolean.valueOf(props.getProperty("debug"));
		this.listener = listener;
		this.doCheck();
	}
	
	/***
	 * Checks if the runtime environment fulfills the needed requirements.
	 * @throws ToolsetException is thrown if compiler and linker are not available or accessible.
	 */
	protected void doCheck() throws ToolsetException {
		String sep = System.getProperty("file.separator");
		CompilerExe = new File(installationPath + sep + COMPILER);
		LinkerExe = new File(installationPath + sep + LINKER);
		// Check if Executables are accessible
		if (!CompilerExe.exists()) {
			throw new ToolsetException("No Compiler found: " + CompilerExe.getAbsolutePath());
		}
		if (!CompilerExe.canExecute()) {
			throw new ToolsetException("No execution rights on " + COMPILER);
		}
		if (!LinkerExe.exists()) {
			throw new ToolsetException("No linker found: " + LinkerExe.getAbsolutePath());
		}
		if (!LinkerExe.canExecute()) {
			throw new ToolsetException("No execution rights on " + LINKER);
		}
	}
	
	/***
	 * Logs a simple message to logging instance of {@link BuildListener}.
	 * @param message message to print.
	 */
	protected void log(String message) {
		log("%s", message);
	}
	
	/***
	 * Logs a formatted message to logging instance of {@link BuildListener}.
	 * @param format the message containing formatting symbols.
	 * @param args arguments to replace formatting symbols.
	 */
	protected void log(String format, Object ...args) {
		if (this.listener != null) {
			this.listener.getLogger().printf(format, args);
			this.listener.getLogger().println();
			this.listener.getLogger().flush();
			// Analyze log for reported errors
			failed = checkForErrors(format, args);
		} else {
			// what to do now?
		}
	}
	
	/***
	 * Writes debugging messages to logging instance of {@link BuildListener}. This
	 * is only done if debugging is globally enabled for the WIX toolset in the settings
	 * of Jenkins.
	 * @param message debug message.
	 */
	protected void debug(String message) {
		debug("%s", message);
	}
	
	/***
	 * Writes debugging messages to logging instance of {@link BuildListener}. This
	 * is only done if debugging is globally enabled for the WIX toolset in the settings
	 * of Jenkins.
	 * @param message debug message with formatting symbols.
	 * @param args arguments to replace formatting symbols.
	 */
	protected void debug(String format, Object...args) {
		if (this.debugEnabled) {
			this.listener.getLogger().printf(format, args);
			this.listener.getLogger().println();
			this.listener.getLogger().flush();
		}
	}
	
	/***
	 * Checks a given message for compiler or linker errors. This is called from
	 * {@link #log(String)}, {@link #log(String, Object...)}.
	 * @param format message with formatting symbols or not.
	 * @param args formatting arguments or null.
	 * @return true if message contains the word error.
	 */
	private boolean checkForErrors(String format, Object ...args) {
		String line = String.format(format, args);
		line = line.toLowerCase();
		return line.contains("error");
	}
	
	/***
	 * Returns if the execution of compiler or linker has lead to a failure.
	 * @return true if an error has occured.
	 */
	public boolean hasFailed() {
		return failed;
	}
	
	/***
	 * Compiles the given source file.
	 * @param file source file to compile.
	 * @throws ToolsetException throws an exception if process fails.
	 */
	public void compile(FilePath file) 
	throws ToolsetException {
		this.sourceFile = file;
		if (!this.sourceFile.getRemote().endsWith(".wxs")) 
			throw new ToolsetException("No wxs file found.");
		//String objfile = this.sourceFile.getRemote().replace(".wxs", ".wsobj");
		String objfile = toObjectFilename(sourceFile.getRemote());
		//execute(CompilerExe, "-nologo", "-out", "\"", objfile, "\"", this.sourceFile.getRemote(), "-ext WixUIExtension");
		execute(CompilerExe, setDefaultParameters(objfile, sourceFile.getRemote()));
		if (hasFailed()) throw new ToolsetException("Compiling sources failed.");
	}
	
	protected synchronized static String toObjectFilename(final String sourceFile) {
		return sourceFile.replace(".wxs", ".wixobj");
	}
	
	protected synchronized static String toMsiFilename(final String sourceFile) {
		return sourceFile.replace(".wxs", ".msi");
	}
	
	protected synchronized static String setDefaultParameters(final String outfile, final String infile) {
		return String.format("-nologo -out \"%s\" \"%s\" -ext WixUIExtension", outfile, infile);
	}
	
	/***
	 * Links the compiled object files together into an MSI file.
	 * @throws ToolsetException throws an exception if process fails.
	 */
	public void link() 
	throws ToolsetException {
		if (!hasFailed()) {
			//String objfile = this.sourceFile.getRemote().replace(".wxs", ".wsobj");
			String objfile = toObjectFilename(sourceFile.getRemote());
			//String msifile = this.sourceFile.getRemote().replace(".wxs", ".msi");
			String msifile = toMsiFilename(this.sourceFile.getRemote()); 
			//execute(LinkerExe, "-nologo", "-out", msifile, objfile, "-ext WixUIExtension");
			execute(LinkerExe, setDefaultParameters(msifile, objfile));
			if (hasFailed()) throw new ToolsetException("Linking failed.");
		} else {
			throw new ToolsetException("No object files found.");
		}
	}
	
	/***
	 * Generic procedure to execute WIX Toolset executables and react on the
	 * command line output.
	 * @param executable executable to call (linker or compiler)
	 * @param args additional arguments for the executable.
	 */
	private void execute(final File executable, Object...args) {
		try {
			String line = null;
			WixCommand cmd = new WixCommand(executable, args);
			debug("Executing command: %s", cmd.toString());
			Process p = Runtime.getRuntime().exec(cmd.toString());
			BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			// Log stdout of executable
			while ((line = stdout.readLine()) != null) {
				log(line);
			}
			stdout.close();
			// Log stderr of executable
			while ((line = stderr.readLine()) != null) {
			    log(line);
			}
			stderr.close();
			// Wait for the process to end
			p.waitFor();
			int exitCode = p.exitValue();
			debug("Process finished with %d", exitCode);
			failed = (exitCode > 0);
		} catch (Exception e) {
			failed = true;
			log("Process failed: %s", e.getMessage());
		}
	}
}
