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
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Bjoern.Berg
 */
public class CandleTest {
    Candle candle;
    
    public CandleTest() {
    }
    
    @Before
    public void setUp() {
        EnvVars vars = new EnvVars();
        ToolsetSettings settings = new ToolsetSettings();
        candle = new Candle(settings, vars);
    }
    
    @After
    public void tearDown() {
    }
    
    private String onPlatform(String cmd) {
        String os = System.getProperty("os.name");
        return String.format("%s%s", os.startsWith("Windows") ? "C:\\" : "/", cmd);
    }
    
    private String onPlatform(String cmd, String args) {
        String os = System.getProperty("os.name");
        return String.format("\"%s%s\" %s", os.startsWith("Windows") ? "C:\\" : "/", cmd, args);
    }

    /**
     * Test of setArch method, of class Candle.
     */
    @Test
    public void testSetArch() {
        final String CMD = onPlatform("candle.exe", "-arch ia64 -nologo -out \"test.txt\" \"test.txt\" ");
        try {
            FilePath fp = new FilePath(new File("test.txt"));
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
        final String CMD = onPlatform("candle.exe", "-arch x86 -I \"include\" -nologo -out \"test.txt\" \"test.txt\" ");
        try {
            FilePath fp = new FilePath(new File("test.txt"));
            FilePath inc = new FilePath(new File("include"));
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
        assertEquals("", candle.toString());
    }

    /**
     * Test of createCommand method, of class Candle.
     */
    @Test
    public void testCreateCommand() {
       try {
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
        final String CMD = onPlatform("candle.exe", "-arch x86 -nologo -out \"test.txt\" \"test.txt\" ");
        try {
            FilePath fp = new FilePath(new File("test.txt"));
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
        final String CMD = onPlatform("candle.exe", "-arch x86 -ext MyExtension -dvar_key=\"var_name\" -nologo -out \"output.txt\" \"input.txt\" ");
        try {
            FilePath input = new FilePath(new File("input.txt"));
            FilePath output = new FilePath(new File("output.txt"));
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
    
}
