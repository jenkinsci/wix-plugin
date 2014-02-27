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
import java.util.LinkedList;
import java.util.List;

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
        super(Wix.COMPILER, settings, vars);
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
    public String toString() {
        return cmd.toString();
    }
    
    /**
     * helper to build command and append include directives.
     */
    private void appendIncludes() {
        for (FilePath path : includePaths) {
            cmd.append("-I \"");
            cmd.append(path.getRemote());
            cmd.append("\" ");
        }
    }

    @Override
    protected void createCommand() throws ToolsetException {
        // the candle.exe command on command line looks like:
        // candle.exe [-?] [-nologo] [-out outputFile] sourceFile [sourceFile ...]
        clean();    // create a new StringBuffer
        check();    // check if cmd was still created
        
        cmd.append(exec.getAbsolutePath());     // candle.exe
        cmd.append(" ");
        cmd.append("-arch ");
        cmd.append(arch.name());                // architecture
        cmd.append(" ");
        appendExtensions();                     // append extensions
        appendParameters();                     // append parameters
        appendIncludes();                       // append include paths
        // no we are coming to true/false parameters
        cmd.append(nologo ? "-nologo " : "");
        cmd.append(verbose ? "-v " : "");
        cmd.append(wxall ? "-wxall " : "");
        // add output file
        cmd.append("-out \"");
        cmd.append(outputFile.getRemote());
        cmd.append("\" ");
        // append all available source files
        appendSources();
    }
}
