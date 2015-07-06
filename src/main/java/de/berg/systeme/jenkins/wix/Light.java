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
import hudson.Launcher;
import hudson.util.ArgumentListBuilder;

/**
 * command definition for Wix Toolset Linker (light.exe)
 * @author Bjoern.Berg
 */
class Light extends WixCommand {
    /**
     * constructor.
     * @param settings global settings.
     * @param envVars environment variables.
     */
    public Light(ToolsetSettings settings, EnvVars envVars) {
        this(null, settings, envVars);
    }
    
    /**
     * constructor.
     * @param settings global settings.
     * @param envVars environment variables.
     */
    public Light(Launcher launcher, ToolsetSettings settings, EnvVars envVars) {
        super(launcher, Wix.LINKER, settings, envVars);
    }

    /**
     * Creates the command so it can be executed.
     * @throws ToolsetException 
     */
    @Override
    protected ArgumentListBuilder createCommand() throws ToolsetException {
        // the light.exe command on command line looks like:
        // light.exe [-?] [-b bindPath] [-nologo] [-out outputFile] objectFile [objectFile ...]
        //clean();    // create a new StringBuffer
        //check();    // check if cmd was still created
    	
    	args = new ArgumentListBuilder();
    	try {
	    	args.add(exec.getPath());	// candle.exe
	    	// append extensions
	    	for (String extension : extensions) {
	            args.add("-ext").add(extension);
	        }
	    	// append parameters
	    	for(String key : parameters.keySet()) {
	            String value = parameters.get(key);
	            args.addKeyValuePair("-d", key, value, true);
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
