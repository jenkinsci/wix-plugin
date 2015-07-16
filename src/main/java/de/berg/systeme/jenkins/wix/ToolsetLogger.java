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
import java.io.OutputStream;
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
    private int errorCount;
    
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
        this.errorCount		= 0;
    }
    
    public void enableDebugLogging(boolean debug) {
        this.debugEnabled = debug;
    }
    
    /**
     * Logs an exception including stacktrace.
     * @param t Exception thrown.
     */
    public void severe (Throwable t) {
    	check();
    	stream.printf("[wix] SEVERE: %s", t.getMessage());
    	stream.println();
    	stream.println("[wix] Stacktrace follows:");
    	t.printStackTrace(stream);
    	stream.flush();
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
        String line = String.format(format, args);
        checkForErrors(line);
        stream.println("[wix] " + line);
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
            String line = String.format(format, args);
            checkForErrors(line);
            stream.println("[wix] " + line);
            stream.flush();
        }
    }
    
    /**
     * checks if output contains information about errors.
     * @param line output line from process.
     * @return true if errors are found.
     */
    private void checkForErrors(String line) {
    	// test if candle or light reported an error
        if (line.toLowerCase().matches(".*error [A-Z]{4,4}[0-9]{1,4}.*")) {
        	errorCount++;
        }
    }
    
    public boolean hasNoErrors() {
    	return errorCount == 0;
    }
    
    /**
     * Checks if print stream exists. Otherwise stdout is used for logging.
     */
    private void check() {
        if (null == stream) {
            stream = new PrintStream(new FileOutputStream(FileDescriptor.out));
        }
    }
    
    /**
     * Returns stream for injection into Jenkins ProcessLauncher.
     * @return
     */
    public OutputStream getStream() {
    	check();
    	return stream;
    }
}
