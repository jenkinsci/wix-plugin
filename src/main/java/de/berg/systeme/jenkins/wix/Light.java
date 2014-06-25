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
import java.util.ResourceBundle;

/**
 * command definition for Wix Toolset Linker (light.exe)
 * @author Bjoern.Berg
 */
class Light extends WixCommand {
    private static final ResourceBundle messages = ResourceBundle.getBundle("Messages");
    /**
     * constructor.
     * @param settings global settings.
     * @param envVars environment variables.
     */
    public Light(ToolsetSettings settings, EnvVars envVars) {
        super(Wix.LINKER, settings, envVars);
    }

    /**
     * Creates the command so it can be executed.
     * @throws ToolsetException 
     */
    @Override
    protected void createCommand() throws ToolsetException {
        // the light.exe command on command line looks like:
        // light.exe [-?] [-b bindPath] [-nologo] [-out outputFile] objectFile [objectFile ...]
        clean();    // create a new StringBuffer
        check();    // check if cmd was still created
        
        cmd.append(exec.getAbsolutePath());     // candle.exe
        cmd.append(" ");
        appendExtensions();                     // append extensions
        appendParameters();                     // append parameters
        // no we are coming to true/false parameters
        cmd.append(nologo ? "-nologo " : "");
        cmd.append(verbose ? "-v " : "");
        cmd.append(wxall ? "-wxall " : "");
        // add output file
        cmd.append("-out \"");
        cmd.append(outputFile.getRemote());
        cmd.append("\" ");
        // append all available object files
        appendSources();
    }
    
    @Override
    public String toString() {
        return cmd.toString();
    }
}
