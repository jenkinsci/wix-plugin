/*
* This file is part of wix-plugin-jenkins.
* 
* Copyright (C) 2014 Berg Systeme
* 
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/

package de.berg.systeme.jenkins.wix;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.util.ArgumentListBuilder;

/**
 * Abstract class for building commands.
 * @author Bjoern.Berg
 */
public abstract class WixCommand {
    // Logger
    protected ToolsetLogger lg = ToolsetLogger.INSTANCE;
    // Exectuables absolute path
    protected File exec;
    // global settings
    protected ToolsetSettings settings;
    // StringBuffer to build command
    //protected StringBuffer cmd = new StringBuffer();
    // List of source files
    protected List<FilePath> sourceFiles = new LinkedList<FilePath>();
    // Map of environment variables
    protected Map<String, String> parameters = new HashMap<String, String>();
    // list of needed extensions
    protected List<String> extensions = new LinkedList<String>();
    // skip printing candle logo information
    protected boolean nologo = true;
    // specify output file (-o)
    protected FilePath outputFile;
    // verbose output (-v)
    protected boolean verbose = false;
    // treat all warnings as errors
    protected boolean wxall = false;
    // not accepted environment variables
    protected List<String> rejectedEnvVars = new LinkedList<String>();
    // Jenkins Launcher
    protected Launcher launcher;
    // Workspace of build job
    protected FilePath workspace;
    protected ArgumentListBuilder args;
    
    public WixCommand(Launcher launcher, String ExeName, ToolsetSettings settings, EnvVars vars) {
    	this.launcher = launcher;
    	// Bugfix:
    	// It is stated that candle and light will work, if no installation path
    	// is given, so installation path cannot be stated as given. This will
    	// also help to configure Wix on a UNIX master and used on a Windows
    	// slave machine.
    	String path = settings.get(Wix.INST_PATH, "");
    	if ( StringUtils.isEmpty(path) ) {
    		// no installation path configured
    		this.exec = new File(ExeName);
    	} else {
    		// installation path is given
    		char lastSign = path.charAt( path.length() - 1 );
    		if (lastSign == '/' || lastSign == '\\') {
    			this.exec = new File(path + ExeName);
    		} else {
    			//String sep = System.getProperty("file.separator");
    	        this.exec = new File(path + "\\" + ExeName);
    		}		
    	}
        
    	this.settings = settings;
        
        // Environment variables which are not taken into account
        String sysEnvVars = settings.get(Wix.LOV_REJECTED, Wix.DEF_LOV_TO_REJECT);
        for(String s : sysEnvVars.split(",")) {
            addRejectedEnvVar(s);
        }

        parseSettings(this.settings, vars);
    }
    
    private void addRejectedEnvVar(String envVar) {
        this.rejectedEnvVars.add(envVar.toLowerCase());
    }
    
    protected boolean isEnvVarRejected(String envVar, String value) {
        boolean reject = false;
        if(envVar.contains("=")) {
            lg.debug(envVar + ": contains illegal character.");
            reject = true;
        } else if ( rejectedEnvVars.contains(envVar.toLowerCase()) ) {
        	lg.debug(envVar + ": is marked as ignorable variable.");
        	reject = true;
        }
        return reject;
    }
    
    /**
     * parse settings and assign variables to command.
     * @param settings global settings.
     * @param vars environment variables.
     */
    private void parseSettings(ToolsetSettings settings, EnvVars vars) {
        // Add extensions, we use reflection to avoid a lot of code
        Field[] fields = Wix.class.getFields();
        for (Field field : fields) {
            try {
                String fieldValue = (String)field.get(String.class.newInstance());
                if (fieldValue.endsWith("Extension") && settings.get(fieldValue, false)) {
                    addExtension(fieldValue);
                }
            } catch (InstantiationException ex) {
                Logger.getLogger(WixCommand.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(WixCommand.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        // add all environment variables as parameter
        // environment variables are only added, if option is set
        if (settings.get(Wix.ENBL_ENV_AS_PARAM, false)) {
            for (Map.Entry<String,String> entry : vars.entrySet()) {
                String varName = entry.getKey();
                String value = entry.getValue();
                // contains value a directory it is better to escape everything
                if (isEnvVarRejected(varName, value)) {
                    lg.debug("Rejected Environment variable: " + varName);
                } else {
                	if ( StringUtils.isNotEmpty(value) ) {
	                    value = value.replace("\"", "\\\"");
	                    // Bugfix: - add a second backslash
	                    // fix for accidently quoted double quotes resulting in an
	                    // appended backslash in a value 
	                    char lastChar = value.charAt(value.length() - 1);
	                    if (lastChar == '\\') { value += "\\"; }
                	}
                    lg.debug("VarName: " + varName + "; Value: " + value);
                    addParameter(varName, value); 
                }
            }
        } else {
            lg.log("Environment variables are not automatically added as parameters.");
        }
    }
    
    protected void addWorkspace(FilePath workspace) {
    	this.workspace = workspace;
    }
    
    private FilePath makeRemotePath(FilePath fp) {
    	FilePath tmp = null;
    	if (this.workspace != null) {
    		tmp = this.workspace.child(fp.getRemote());
    	} else {
    		tmp = fp;
    	}
    	return tmp;
    }

    /**
     * input file.
     * @param filepath 
     */
    void addSourceFile(FilePath filepath) {
        sourceFiles.add(makeRemotePath(filepath));
    }

    /**
     * define a parameter for the preprocessor.
     * @param name name of parameter.
     * @param value value of parameter.
     */
    public void addParameter(String name, String value) {
        if (!this.parameters.containsKey(name)) {
            this.parameters.put(name, value);
        }
    }
    
    /**
     * extension assembly or "class, assembly".
     * @param extensionName 
     */
    public void addExtension(String extensionName) {
        this.extensions.add(extensionName);
    }

    /**
     * specify output file (default: write to current directory).
     * @param outputFile 
     */
    public void setOutputFile(FilePath outputFile) {
        this.outputFile = makeRemotePath(outputFile);
    }

    /**
     * verbose output.
     * @param verbose true enables verbose output (default: false)
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * treat all warnings as errors.
     * @param wxall true enables. (default: false).
     */
    public void setWxall(boolean wxall) {
        this.wxall = wxall;
    }
    
    /**
     * skip printing logo information.
     * @param nologo set to false shows logo (default: true).
     */
    public void setNologo(boolean nologo) {
        this.nologo = nologo;
    }
    
    /**
     * create command before execution. createCommand is used by execute().
     * @throws ToolsetException 
     */
    protected abstract ArgumentListBuilder createCommand() throws ToolsetException;
    
    /**
     * Checks if binary exists and toolset is properly installed.
     * @return true if binary exists.
     * @throws ToolsetException if toolset is not configured.
     */
    public boolean exists() throws ToolsetException {
    	boolean testResult = true;
    	int len = exec.getPath().length();
    	if (len <= 10) {
    		// candle.exe has 10 signs (light.exe less), if path contains not more than
    		// 10 signs we expect that we are configured running on a Windows slave
    		lg.log("Expecting executable in path.");
    		testResult = false;
    	} else {
	    	// The following can only work if full path is given
	        if (!exec.exists()) {
	            throw new ToolsetException("No binary found: " + exec.getPath());
	        }
	        if (!exec.canExecute()) {
	            throw new ToolsetException("No execution rights on " + exec.getName());
	        }
    	}
        return testResult;
    } 
    
    /**
     * checks if command is properly configured.
     * @throws ToolsetException 
     */
    protected void check() throws ToolsetException {
        if (sourceFiles.isEmpty()) {
            throw new ToolsetException("missing source files");
        } else if (outputFile == null /*|| !outputFile.exists()*/) {
            throw new ToolsetException("missing output filename");
        }
    }
    
    /**
     * executes command and parses the output checking for errors.
     * @return true if execution was successful otherwise false.
     * @throws Exception
     * @throws ToolsetException 
     */
    public boolean execute() throws Exception, ToolsetException {
        boolean success = true;
        
        try {
        	ArgumentListBuilder cmd2call = this.createCommand();
        
        	if (0 != launcher.launch().envs(parameters)
        						  .pwd(workspace)
        						  .stdout(lg.getStream())
        						  .stderr(lg.getStream())
        						  .cmds(cmd2call)
        						  .join()) {
        		success = false;
        	}
        	
        	success &= lg.hasNoErrors();
        } catch (Exception e) {
        	lg.log(e.getMessage());
        	success = false;
        }
        
        return success;
    }
    
    public String toString() {
    	String cmd = "";
    	try {
    		cmd = args.toStringWithQuote();
    	} catch (NullPointerException npe) {
    		// do nothing
    	}
    	return cmd;
    }
}
