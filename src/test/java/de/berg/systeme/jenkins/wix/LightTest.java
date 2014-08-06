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
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Bjoern.Berg
 */
public class LightTest {
    Light light;
    
    
    @Before
    public void setUp() {
        EnvVars vars = new EnvVars();
        ToolsetSettings settings = new ToolsetSettings();
        light = new Light(settings, vars);
    }
    
    @After
    public void tearDown() {
    }
    
    private String onPlatform(String cmd) {
        String os = System.getProperty("os.name");
        return String.format("\"%s\"%s", os.startsWith("Windows") ? "C:\\" : "/", cmd);
    }
    
    private String onPlatform(String cmd, String args) {
        String os = System.getProperty("os.name");
        return String.format("\"%s%s\" %s", os.startsWith("Windows") ? "C:\\" : "/", cmd, args);
    }

    /**
     * Test of createCommand method, of class Light.
     */
    @Test
    public void testCreateCommand() {
        try {
            light.createCommand();
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
        final String CMD = onPlatform("light.exe", "-nologo -out \"test.txt\" \"test.txt\" ");
        try {
            FilePath fp = new FilePath(new File("test.txt"));
            light.addSourceFile(fp);
            light.setOutputFile(fp);
            light.createCommand();
            assertEquals(CMD, light.toString());
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
            light.addSourceFile(null);
            light.setOutputFile(null);
            light.createCommand();
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
        final String CMD = onPlatform("light.exe", "-ext MyExtension -dvar_key=\"var_name\" -nologo -out \"output.txt\" \"input.txt\" ");
        try {
            FilePath input = new FilePath(new File("input.txt"));
            FilePath output = new FilePath(new File("output.txt"));
            light.addSourceFile(input);
            light.setOutputFile(output);
            light.addExtension("MyExtension");
            light.addParameter("var_key", "var_name");
            light.createCommand();
            assertEquals(CMD, light.toString());
        } catch (ToolsetException ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * Test of toString method, of class Light.
     */
    @Test
    public void testToString() {
        // without calling createCommand it must be empty
        assertEquals("", light.toString());
    }
    
}
