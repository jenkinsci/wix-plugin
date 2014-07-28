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

import hudson.EnvVars;
import hudson.FilePath;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    protected StringBuffer cmd = new StringBuffer();
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

    public WixCommand(ToolsetSettings settings, EnvVars vars) {
        this(null, settings, vars);
    }
    
    public WixCommand(String ExeName, ToolsetSettings settings, EnvVars vars) {
        File installationPath = new File(settings.get(Wix.INST_PATH, ""));
        String sep = System.getProperty("file.separator");
        this.exec = new File(installationPath + sep + ExeName);
        this.settings = settings;
        
        // Environment variables which are not taken into account
        try {
            InputStream in = getClass().getResourceAsStream("/rejected.txt");
            Scanner s = new Scanner(in);
            while(s.hasNext()) {
                addRejectedEnvVar(s.next());
            }
        } catch(Exception e) {
            lg.log(e.getMessage());
        }

        parseSettings(this.settings, vars);
    }
    
    protected void addRejectedEnvVar(String envVar) {
        this.rejectedEnvVars.add(envVar.toLowerCase());
    }
    
    protected boolean isEnvVarRejected(String envVar, String value) {
        boolean reject = false;
        if(envVar.contains("=")) {
            lg.debug(envVar + ": contains illegal character.");
            reject = true;
        }
        if (!reject) {
            if(rejectedEnvVars.contains(envVar.toLowerCase())) {
               reject = true; 
            } else {
                reject = value.matches("([a-zA-Z]:)?(\\\\[a-zA-Z0-9 \\._\\-\\(\\)]+)+\\\\?");
            }
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
        for (Map.Entry<String,String> entry : vars.entrySet()) {
            String varName = entry.getKey();
            String value = entry.getValue();
            // contains value a directory it is better to escape everything
            if (isEnvVarRejected(varName, value)) {
                lg.debug("Rejected Environment variable: " + varName);
            } else {
                value = value.replace("\"", "\\\"");
                lg.debug("VarName: " + varName + "; Value: " + value);
                addParameter(varName, value); 
            }
        }
    }

    /**
     * input file.
     * @param filepath 
     */
    void addSourceFile(FilePath filepath) {
        sourceFiles.add(filepath);
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
        this.outputFile = outputFile;
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
    protected abstract void createCommand() throws ToolsetException;
    
    /**
     * Checks if binary exists and toolset is properly installed.
     * @return true if binary exists.
     * @throws ToolsetException if toolset is not configured.
     */
    public boolean exists() throws ToolsetException {
        if (!exec.exists()) {
            throw new ToolsetException("No binary found: " + exec.getAbsolutePath());
        }
        if (!exec.canExecute()) {
            throw new ToolsetException("No execution rights on " + exec.getName());
        }
        return true;
    } 
    
    protected void appendExtensions() {
        for (String extension : extensions) {
            cmd.append("-ext ");
            cmd.append(extension);
            cmd.append(" ");
        }
    }
    
    protected void appendParameters() {
        for(String key : parameters.keySet()) {
            String value = parameters.get(key);
            cmd.append("-d");
            cmd.append(key);
            cmd.append("=\"");
            cmd.append(value);
            cmd.append("\" ");
        }
    }
    
    protected void appendSources() {
        for (FilePath source : sourceFiles) {
            cmd.append("\"");
            cmd.append(source.getRemote());
            cmd.append("\" ");
        }
    }
    
    /**
     * creates new StringBuffer.
     */
    protected void clean() {
        cmd = null;
        cmd = new StringBuffer();
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
     * checks if output contains information about errors.
     * @param line output line from process.
     * @return true if errors are found.
     */
    private boolean checkForErrors(String line) {
        return line.toLowerCase().matches(".*error.*");
    }
    
    /**
     * executes command and parses the output checking for errors.
     * @return true if execution was successful otherwise false.
     * @throws Exception
     * @throws ToolsetException 
     */
    public boolean execute() throws Exception, ToolsetException {
        // false �berschreibt immer true
        boolean success = true;
        String line = null;
        
        this.createCommand();

        Process p = Runtime.getRuntime().exec(cmd.toString());
        BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        // Log stdout of executable
        while ((line = stdout.readLine()) != null) {
            lg.log(line);
            success &= checkForErrors(line);
        }
        stdout.close();
        // Log stderr of executable
        while ((line = stderr.readLine()) != null) {
            lg.log(line);
            success &= checkForErrors(line);
        }
        stderr.close();
        // Wait for the process to end
        p.waitFor();
        success &= (p.exitValue() > 0);
       
        return !success;    // inverted logic at this point
    }
}
