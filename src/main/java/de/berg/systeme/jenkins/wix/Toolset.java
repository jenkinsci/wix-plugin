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
import java.io.IOException;
import java.util.ResourceBundle;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;

/***
 * <p>Toolset checks the existence for the WIX Toolset on the buildsystem. If not available
 * an exception is thrown.</p>
 * <p>It is used to compile and link wsx-files in a batch mannor triggered by the {@link WixToolsetBuilder}.</p>
 * 
 * @author Bjoern Berg, bjoern.berg@gmx.de
 * @version 2.0
 *
 */
public final class Toolset {
    private static final ResourceBundle messages = ResourceBundle.getBundle("Messages");
    // Environment variables
    private EnvVars envVars;
    // Command for compiler
    private WixCommand candle;
    // Command for linker
    private WixCommand light;
    // Logging instance
    private final ToolsetLogger lg = ToolsetLogger.INSTANCE;
    // global setting
    private ToolsetSettings settings;
    // Windows slave mode
    private boolean usedOnSlave = false;
    
    @SuppressWarnings("rawtypes")
    public Toolset(AbstractBuild build, Launcher launcher, ToolsetSettings properties) throws ToolsetException {
    	try {
			// initialize globals
			this.settings   = properties;
			this.envVars    = build.getEnvironment();
			// initialize commands
			this.candle     = new Candle(launcher, this.settings, this.envVars);
			this.light      = new Light(launcher, this.settings, this.envVars);
			usedOnSlave		= properties.get(Wix.USED_ON_SLAVE, false);
			
			this.candle.addWorkspace(build.getWorkspace());
			this.light.addWorkspace(build.getWorkspace());
			
			// check
			if (usedOnSlave) {
				lg.log("Wix Toolset plugin is running in slave mode.");
				lg.log("Do not test if toolset is installed.");
			} else {
				lg.log(this.candle.exists() ? messages.getString("COMPILER_FOUND") : messages.getString("COMPILER_NOT_FOUND"));
				lg.log(this.light.exists() ? messages.getString("LINKER_FOUND") : messages.getString("LINKER_NOT_FOUND"));
			}
		} catch (IOException e) {
			lg.severe(e);
		} catch (InterruptedException e) {
			lg.severe(e);
		}
    }
    
    /**
     * Replaces the file extension of a given file and returns a new FilePath
     * with the new extension.
     * @param input given file to replace extension.
     * @param fext extension to replace (starting with .dot).
     * @param fext_new new extension (starting with .dot).
     * @return new FilePath.
     */
    private FilePath replaceExtension(FilePath input, String fext, String fext_new) {
        String filename = input.getRemote();
        filename = filename.replace(fext, fext_new);
        return new FilePath(new File(filename));
    }
    
    /**
     * Checks if the file path or file is valid.
     * @param fp file path.
     * @param fext file extension.
     * @return true if file path is valid.
     */
    private boolean isValid(FilePath fp, String fext) {
        boolean valid = true;
        try {
            valid = fp.getRemote().endsWith(fext) && fp.exists();
        } catch (IOException ex) {
            lg.severe(ex);
            valid = false;
        } catch (InterruptedException ex) {
            lg.severe(ex);
            valid = false;
        }
        return valid;
    }
    
    public void setArchitecture(String cpu) {
        Wix.Arch arch = (cpu == null || cpu.isEmpty()) ? Wix.Arch.x86 : Wix.Arch.valueOf(cpu);
        ((Candle)candle).setArch(arch);
    }
	
    /***
     * Compiles the given source file.
     * @param file source file to compile.
     * @throws ToolsetException throws an exception if process fails.
     */
    public FilePath compile(FilePath file) throws ToolsetException, Exception {
        FilePath[] input = {file};
        return compile(input);
    }
    
    /**
     * Compiles a set of source files.
     * @param input set of source files.
     * @return compiled object file.
     * @throws Exception
     * @throws ToolsetException 
     */
    public FilePath compile(FilePath[] input) throws Exception, ToolsetException {
        // we use the first input name for the output file name
        FilePath output = replaceExtension(input[0], ".wxs", ".wixobj");
        return compile(input, output);
    }
    
    /**
     * Compiles a set of source files into a given object file.
     * @param input set of source files.
     * @param output object file.
     * @return object file if successful. Otherwise null.
     * @throws Exception
     * @throws ToolsetException 
     */
    public FilePath compile(FilePath[] input, FilePath output) throws Exception, ToolsetException {
        // add every source file
        for (FilePath fp : input) {
            if (isValid(fp, ".wxs")) {
                lg.debug(messages.getString("ADDING_SOURCE_FILE"), fp.getRemote());
                candle.addSourceFile(fp);
            } else {
                lg.log(messages.getString("NO_VALID_SOURCE_FILE"), fp.getRemote());
            }
        }
        // add output file
        candle.setOutputFile(output);
        
        candle.createCommand();
        lg.debug(messages.getString("EXECUTING_COMMAND"), candle.toString());
        if (candle.execute()) {
            lg.log(messages.getString("COMPILING_SUCCESSFUL"));
        } else {
            lg.log(messages.getString("COMPILING_FAILED"));
            throw new ToolsetException(messages.getString("COMPILING_FAILED"));
        }
        return candle.getOutputFile();
    }
    
    /***
     * Links the given object file into an MSI package.
     * @param file object file.
     * @return MSI package file.
     * @throws Exception
     * @throws ToolsetException 
     */
    public FilePath link(FilePath file) throws Exception, ToolsetException {
        FilePath[] input = {file};
        return link(input);
    }
    
    /**
     * Links a set of given object files into an MSI package.
     * @param input set of object files.
     * @return MSI package file.
     * @throws Exception
     * @throws ToolsetException 
     */
    public FilePath link(FilePath[] input) throws Exception, ToolsetException {
        FilePath output = replaceExtension(input[0], ".wixobj", ".msi");
        return link(input, output);
    }
    
    /**
     * Links a set of given object files into an MSI package.
     * @param input set of object files.
     * @param output name of MSI package.
     * @return MSI package file.
     * @throws Exception
     * @throws ToolsetException 
     */
    public FilePath link(FilePath[] input, FilePath output) throws Exception, ToolsetException {
        // add every object file
        for (FilePath fp : input) {
            if (isValid(fp, ".wixobj")) {
                lg.debug(messages.getString("ADDING_OBJECT_FILE"), fp.getRemote());
                light.addSourceFile(fp);
            } else {
                lg.log(messages.getString("NO_VALID_OBJECT_FILE"), fp.getRemote());
            }
        }
        // add output file
        light.setOutputFile(output);
        
        light.createCommand();
        lg.debug(messages.getString("EXECUTING_COMMAND"), light.toString());
        if (light.execute()) {
            lg.log(messages.getString("LINKING_SUCCESSFUL"));
        } else {
            lg.log(messages.getString("LINKING_FAILED"));
            throw new ToolsetException(messages.getString("LINKING_FAILED"));
        }
        
        return light.getOutputFile();
    }
    
    /**
     * Wrapper method for {@link #link(hudson.FilePath[], hudson.FilePath) link}.
     * @param input input FilePath
     * @param output output FilePath
     * @return FilePath to output file used to link to (can be different from defined output)
     * @throws Exception
     * @throws ToolsetException 
     */
    public FilePath link(FilePath input, FilePath output) throws Exception, ToolsetException {
        FilePath[] arr = {input};
        return link(arr, output);
    }
}
