package de.berg.systeme.jenkins.wix;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public final class Toolset {
	private static final String COMPILER = "candle.exe";
	private static final String LINKER = "light.exe";
	
	@SuppressWarnings("rawtypes")
	private static AbstractBuild build;
	private static Launcher launcher;
	private static BuildListener listener;
	
	private static File installationPath;
	private static File CompilerExe;
	private static File LinkerExe;
	
	private static boolean failed = false;
	
	//private static FilePath[] sourceFiles;
	private static FilePath sourceFile;
	
	public static void initialize(File instPath, BuildListener listener) 
	throws ToolsetException {
		Toolset.initialize(instPath, null, null, listener);
	}
	
	@SuppressWarnings("rawtypes")
	public static void initialize(File instPath, AbstractBuild build,
				Launcher launcher, BuildListener listener) 
	throws ToolsetException {
		Toolset.installationPath = instPath;
		Toolset.listener = listener;
		doCheck();
	}
	
	protected static void doCheck() throws ToolsetException {
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
	
	protected static void log(String message) {
		log("%s", message);
	}
	
	protected static void log(String format, Object ...args) {
		Toolset.listener.getLogger().printf(format, args);
		Toolset.listener.getLogger().println();
		Toolset.listener.getLogger().flush();
		// Analyze log for reported errors
		failed = hasFailure(format, args);
	}
	
	protected static void debug(String message) {
		debug("%s", message);
	}
	
	protected static void debug(String format, Object...args) {
		// FIXME debug muss eingeschränkt werden
		Toolset.listener.getLogger().printf(format, args);
		Toolset.listener.getLogger().println();
		Toolset.listener.getLogger().flush();
	}
	
	protected static boolean hasFailure(String format, Object ...args) {
		String line = String.format(format, args);
		line = line.toLowerCase();
		return line.contains("error");
	}
	
	/*public static void compile(File file) throws ToolsetException {
		File[] files =  {file};		// create pseudo array
		compile(files);
	}*/
	
	/*public static void compile(File[] files) throws ToolsetException {
		Toolset.sourceFiles = new FilePath[files.length];
		for (int i=0; i < files.length; ++i) {
			Toolset.sourceFiles[i] = new FilePath(files[i]);
		}
		compile(Toolset.sourceFiles);
	}*/
	
	public static void compile(FilePath file) throws ToolsetException {
		//FilePath[] files = {file};
		//compile(files);
		Toolset.sourceFile = file;
		if (!Toolset.sourceFile.getRemote().endsWith(".wxs")) 
			throw new ToolsetException("No wxs file found.");
		String objfile = Toolset.sourceFile.getRemote().replace(".wxs", ".wsobj");
		execute(CompilerExe, "-nologo", "-out", objfile, Toolset.sourceFile.getRemote(), "-ext WixUIExtension");
		if (hasFailed()) throw new ToolsetException("Compiling sources failed.");
	}
	
	/*public static void compile(FilePath[] files) throws ToolsetException {
		Toolset.sourceFiles = files;
		execute(CompilerExe, "-nologo", (Object[])Toolset.sourceFiles);
		if (hasFailed()) throw new ToolsetException("Compiling sources failed.");
	}*/
	
	/*private static boolean checkForObjectFiles() {
		FilePath[] objFiles = findObjectFiles();
		return (objFiles != null && objFiles.length == sourceFiles.length);
	}*/
	
	/*private static FilePath[] findObjectFiles() {
		// List of paths
		List<String> tmpList = new LinkedList<String>();
		List<FilePath> objFiles = new LinkedList<FilePath>();
		for (FilePath f : sourceFiles) {
			String path = extractPath(f);
			if (!tmpList.contains(path)) {
				tmpList.add(path);
			}
		}
		for (String path : tmpList) {
			try {
				FilePath directory = new FilePath(new File(path));
				FilePath[] tmpObjFiles = directory.list("*.wsobj");
				for (FilePath f : tmpObjFiles) {
					objFiles.add(f);
				}
			} catch (IOException e) {
				failed = true;
				log(e.getMessage());
			} catch (InterruptedException e) {
				failed = true;
				log(e.getMessage());
			}
		}
		return objFiles.toArray(new FilePath[0]);
	}*/

	/*private static String extractPath(FilePath f) {
		String path = f.getRemote();
		debug("Extract Path: " + path);
		if (path.contains("/")) {
			path = path.substring(0, path.lastIndexOf("/"));
		} else if (path.contains("\\")) {
			path = path.substring(0, path.lastIndexOf("\\"));
		} else {
			path = ".";
		}
		debug("Extracted Path: " + path);
		return path;
	}*/
	
	public static void link() throws ToolsetException {
		if (!hasFailed()) {
			String objfile = Toolset.sourceFile.getRemote().replace(".wxs", ".wsobj");
			String msifile = Toolset.sourceFile.getRemote().replace(".wxs", ".msi");
			execute(LinkerExe, "-nologo", "-out", msifile, objfile, "-ext WixUIExtension");
			if (hasFailed()) throw new ToolsetException("Link failed.");
		} else {
			throw new ToolsetException("No OBJ files found.");
		}
	}
	
	public static boolean hasFailed() {
		return failed;
	}
	
	protected static void execute(final File EXECUTABLE, Object...args) {
		try {
			String line = null;
			WixCommand cmd = new WixCommand(EXECUTABLE, args);
			debug("Executing command: " + cmd.toString());
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
			/*Proc proc = launcher.execute("dir", build.getEnvVars(), listener.getLogger(), build.getProject().getWorkspace());
			int exitCode = proc.join();*/
			int exitCode = p.exitValue();
			log("Process finished with %d", exitCode);
			failed = (exitCode > 0);
		} catch (Exception e) {
			failed = true;
			log("Process failed: %s", e.getMessage());
		}
	}
}
