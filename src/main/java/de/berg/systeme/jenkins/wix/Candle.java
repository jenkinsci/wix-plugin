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

import java.util.LinkedList;
import java.util.List;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.util.ArgumentListBuilder;

/**
 * Wix Command definition for Windows Installer Xml Compiler (candle.exe).
 * @author Bjoern.Berg
 */
public class Candle extends WixCommand {
    // Architecture
    Wix.Arch arch = Wix.Arch.x86;           // default is x86
    // this is done by defining -I<dir> more than once
    List<FilePath> includePaths = new LinkedList<FilePath>();
    
    /**
     * constructor.
     * @param settings global settings.
     * @param vars environment variables.
     */
    public Candle(ToolsetSettings settings, EnvVars vars) {
        this(null, settings, vars);
    }
    
    /**
     * constructor.
     * @param settings global settings.
     * @param vars environment variables.
     */
    public Candle(Launcher launcher, ToolsetSettings settings, EnvVars vars) {
        super(launcher, Wix.COMPILER, settings, vars);
    }
    
    /**
     * set architecture defaults for package, components, etc. (default: x86)
     * @param arch x86, ia64, x64.
     */
    public void setArch(Wix.Arch arch) {
        this.arch = arch;
    }

    /**
     * add to include search path.
     * @param includePath path to include.
     */
    public void addIncludePath(FilePath includePath) {
        this.includePaths.add(includePath);
    }

    @Override
    protected ArgumentListBuilder createCommand() throws ToolsetException {
        // the candle.exe command on command line looks like:
        // candle.exe [-?] [-nologo] [-out outputFile] sourceFile [sourceFile ...]
        //clean();    // create a new StringBuffer
        //check();    // check if cmd was still created
    	
    	args = new ArgumentListBuilder();
    	try {
	    	args.add(exec.getPath());	// candle.exe
	    	args.add("-arch").add(arch.name());
	    	// append extensions
	    	for (String extension : extensions) {
	            args.add("-ext").add(extension);
	        }
	    	// append parameters
	    	for(String key : parameters.keySet()) {
	            String value = parameters.get(key);
	            args.addKeyValuePair("-d", key, value, true);
	        }
	    	// append includes
	    	for (FilePath path : includePaths) {
	            args.add("-I").add(path.getRemote());
	        }
	    	args.add(nologo ? "-nologo" : null);
	    	args.add(verbose ? "-v" : null);
	        args.add(wxall ? "-wxall" : null);
	        // output file
	        args.add("-out").add(outputFile.getRemote());
	        // append sources
	        for (FilePath source : sourceFiles) {
	            args.add(source.getRemote());
	        }
    	} catch (NullPointerException npe) {
    		throw new ToolsetException("Missing parameters to build statement.");
    	}
    	
    	return args;
    }
}
