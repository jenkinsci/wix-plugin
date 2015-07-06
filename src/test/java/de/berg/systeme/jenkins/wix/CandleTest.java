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

import java.io.File;

import org.junit.After;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Bjoern.Berg
 */
public class CandleTest {
	private static final String DUMMY_INST_PATH = "C:\\Program Files (x86)\\WiX Toolset v3.7\\bin";
	private Candle candle;
    private EnvVars vars;
    private ToolsetSettings settings;
    
    public CandleTest() {
    }
    
    @Before
    public void setUp() {
        vars = new EnvVars();
        settings = new ToolsetSettings();
    }
    
    @After
    public void tearDown() {
    }
    

    /**
     * Test of setArch method, of class Candle.
     */
    @Test
    public void testSetArch() {
        final String CMD = "candle.exe -arch ia64 -nologo -out test.txt test.txt";
        try {
            FilePath fp = new FilePath(new File("test.txt"));
            candle = new Candle(settings, vars);
            candle.addSourceFile(fp);
            candle.setOutputFile(fp);
            candle.setArch(Wix.Arch.ia64);
            candle.createCommand();
            assertEquals(CMD, candle.toString());
        } catch (ToolsetException ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * Test of addIncludePath method, of class Candle.
     */
    @Test
    public void testAddIncludePath() {
        final String CMD = "candle.exe -arch x86 -I include -nologo -out test.txt test.txt";
        try {
            FilePath fp = new FilePath(new File("test.txt"));
            FilePath inc = new FilePath(new File("include"));
            candle = new Candle(settings, vars);
            candle.addSourceFile(fp);
            candle.setOutputFile(fp);
            candle.addIncludePath(inc);
            candle.createCommand();
            assertEquals(CMD, candle.toString());
        } catch (ToolsetException ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * Test of toString method, of class Candle.
     */
    @Test
    public void testToString() {
        // without calling createCommand it must be empty
    	candle = new Candle(settings, vars);
        assertEquals("", candle.toString());
    }

    /**
     * Test of createCommand method, of class Candle.
     */
    @Test
    public void testCreateCommand() {
       try {
    	    candle = new Candle(settings, vars);
            candle.createCommand();
            fail("must fail with missing source files.");
        } catch (ToolsetException ex) {
            // accepted
        }
    }
    
    /**
     * Test of createCommand with source files.
     */
    @Test
    public void testCreateCommand_withSources() {
        final String CMD = "candle.exe -arch x86 -nologo -out test.txt test.txt";
        try {
            FilePath fp = new FilePath(new File("test.txt"));
            candle = new Candle(settings, vars);
            candle.addSourceFile(fp);
            candle.setOutputFile(fp);
            candle.createCommand();
            assertEquals(CMD, candle.toString());
        } catch (ToolsetException ex) {
            fail(ex.getMessage());
        }
    }
    
    /**
     * Test of createCommand with null files.
     */
    @Test
    public void testCreateCommand_withNullSources() {
        try {
        	candle = new Candle(settings, vars);
            candle.addSourceFile(null);
            candle.setOutputFile(null);
            candle.createCommand();
            fail("expected exception");
        } catch (ToolsetException ex) {
            // accept
        }
    }
    
    /**
     * Test of createCommand with null files.
     */
    @Test
    public void testCreateCommand_complete() {
        final String CMD = "candle.exe -arch x86 -ext MyExtension -dvar_key=var_name -nologo -out output.txt input.txt";
        try {
            FilePath input = new FilePath(new File("input.txt"));
            FilePath output = new FilePath(new File("output.txt"));
            candle = new Candle(settings, vars);
            candle.addSourceFile(input);
            candle.setOutputFile(output);
            candle.addExtension("MyExtension");
            candle.addParameter("var_key", "var_name");
            candle.createCommand();
            assertEquals(CMD, candle.toString());
        } catch (ToolsetException ex) {
            fail(ex.getMessage());
        }
    }
    
    @Test
    public void testWithFullInstallationPath() {
    	final String CMD = String.format("\"%s\\%s", DUMMY_INST_PATH, "candle.exe\" -arch ia64 -nologo -out test.txt test.txt");
    	settings.set(Wix.INST_PATH, DUMMY_INST_PATH);
        try {
            FilePath fp = new FilePath(new File("test.txt"));
            candle = new Candle(settings, vars);
            candle.addSourceFile(fp);
            candle.setOutputFile(fp);
            candle.setArch(Wix.Arch.ia64);
            candle.createCommand();
            assertEquals(CMD, candle.toString());
        } catch (ToolsetException ex) {
            fail(ex.getMessage());
        }
    }
    
    @Test
    public void testIfExistsWithExecutableInPath() {
    	try { 
    		candle = new Candle(settings, vars);
    		assertFalse(candle.exists());
    	} catch (ToolsetException ex) {
    		// a toolset exception can be thrown on a Unix build
        }
    }
    
    @Test
    public void testIfExistsWithFullPath() {
    	settings.set(Wix.INST_PATH, DUMMY_INST_PATH);
    	try { 
    		candle = new Candle(settings, vars);
    		assertTrue(candle.exists());
    	} catch (ToolsetException ex) {
    		// a toolset exception can be thrown on a Unix build
        }
    }   
}
