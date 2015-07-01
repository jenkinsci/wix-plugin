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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import hudson.EnvVars;
import hudson.FilePath;

/**
 *
 * @author Bjoern.Berg
 */
public class LightTest {
	private static final String DUMMY_INST_PATH = "C:\\Program Files (x86)\\WiX Toolset v3.7\\bin";
    private EnvVars vars;
    private ToolsetSettings settings;
    private Light light;
    
    
    @Before
    public void setUp() {
        vars = new EnvVars();
        settings = new ToolsetSettings();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of createCommand method, of class Light.
     */
    @Test
    public void testCreateCommand() {
        try {
        	light = new Light(settings, vars);
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
        final String CMD = "\"light.exe\" -nologo -out \"test.txt\" \"test.txt\" ";
        try {
            FilePath fp = new FilePath(new File("test.txt"));
            light = new Light(settings, vars);
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
        	light = new Light(settings, vars);
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
        final String CMD = "\"light.exe\" -ext MyExtension -dvar_key=\"var_name\" -nologo -out \"output.txt\" \"input.txt\" ";
        try {
            FilePath input = new FilePath(new File("input.txt"));
            FilePath output = new FilePath(new File("output.txt"));
            light = new Light(settings, vars);
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
    	light = new Light(settings, vars);
        assertEquals("", light.toString());
    }
    
    @Test
    public void testWithFullInstallationPath() {
    	final String CMD = String.format("\"%s\\%s", DUMMY_INST_PATH, "light.exe\" -nologo -out \"test.txt\" \"test.txt\" ");
    	settings.set(Wix.INST_PATH, DUMMY_INST_PATH);
        try {
            FilePath fp = new FilePath(new File("test.txt"));
            light = new Light(settings, vars);
            light.addSourceFile(fp);
            light.setOutputFile(fp);
            light.createCommand();
            assertEquals(CMD, light.toString());
        } catch (ToolsetException ex) {
            fail(ex.getMessage());
        }
    }
    
    @Test
    public void testIfExistsWithExecutableInPath() {
    	try { 
    		light = new Light(settings, vars);
    		assertFalse(light.exists());
    	} catch (ToolsetException ex) {
            fail(ex.getMessage());
        }
    }
    
    @Test
    public void testIfExistsWithFullPath() {
    	settings.set(Wix.INST_PATH, DUMMY_INST_PATH);
    	try { 
    		light = new Light(settings, vars);
    		assertTrue(light.exists());
    	} catch (ToolsetException ex) {
            fail(ex.getMessage());
        }
    }   
}
