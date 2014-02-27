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

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * internal logger which uses PrintStream from Jenkins Listener.
 * You have to initialize logger with init().
 * @author Bjoern.Berg
 */
public enum ToolsetLogger {
    /**
     * Singleton approach
     */
    INSTANCE;                    
    private PrintStream stream;     // print stream
    private boolean debugEnabled;   // print debug messages or not
    
    /**
     * initializes logger with print stream. Use Jenkins Listener.getLogger().
     * @param stream print stream of Listener.getLogger().
     */
    public void init(PrintStream stream) {
        init(stream, false);
    }
    
    /**
     * initializes logger with print stream and enables or disables logging of
     * debug messages. Use Listener.getLogger() for initializing print stream.
     * @param stream print stream of Listener.getLogger().
     * @param debugEnabled true enables debug logging.
     */
    public void init(PrintStream stream, boolean debugEnabled) {
        this.stream         = stream;
        this.debugEnabled   = debugEnabled;
    }
    
    /**
     * logs a simple message.
     * @param msg message string.
     */
    public void log(String msg) {
        log("%s", msg);
    }
    
    /***
    * logs a formatted message.
    * @param format the message containing formatting symbols.
    * @param args arguments to replace formatting symbols.
    */
    public void log(String format, Object...args) {
        check();
        stream.printf(format, args);
        stream.println();
        stream.flush();
    }
    
    /**
     * logs a simple debug message.
     * @param msg debug message string.
     */
    public void debug(String msg) {
        debug("%s", msg);
    }
    
    /**
     * logs a formatted debug message.
     * @param format message with format specifiers as placeholders.
     * @param args replacements as comma-separated list for format specifiers.
     */
    public void debug(String format, Object...args) {
        if (debugEnabled) {
            check();
            stream.printf(format, args);
            stream.println();
            stream.flush();
        }
    }
    
    /**
     * Checks if print stream exists. Otherwise stdout is used for logging.
     */
    private void check() {
        if (null == stream) {
            stream = new PrintStream(new FileOutputStream(FileDescriptor.out));
        }
    }
}
