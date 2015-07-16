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

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test internal logging capabilities.
 * @author Bjoern.Berg
 */
public class ToolsetLoggerTest {
    final ToolsetLogger lg  = ToolsetLogger.INSTANCE;
    final ToolsetLogger lg1 = ToolsetLogger.INSTANCE;
    final ToolsetLogger lg2 = ToolsetLogger.INSTANCE;
    
    PrintStream stream;
    ByteArrayOutputStream buffer;
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        assertEquals(ToolsetLogger.INSTANCE, lg);
        assertEquals(ToolsetLogger.INSTANCE, lg1);
        assertEquals(ToolsetLogger.INSTANCE, lg2);
        
        buffer = new ByteArrayOutputStream();
        stream = new PrintStream(buffer);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of init method, of class ToolsetLogger.
     */
    @Test
    public void testInit_PrintStream() {
        try {
            lg.init(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test of init method, of class ToolsetLogger.
     */
    @Test
    public void testInit_PrintStream_boolean() {
        try {
            lg.init(new PrintStream(new FileOutputStream(FileDescriptor.out)), true);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test of log method, of class ToolsetLogger.
     */
    @Test
    public void testLog_String() {
        String msg = "test";
        try {
           lg.init(stream);
           lg.log(msg);
           assertFalse(stream.checkError());
           assertTrue(buffer.size() >= msg.length());
       } catch(Exception e) {
           fail(e.getMessage());
       }
    }

    /**
     * Test of log method, of class ToolsetLogger.
     */
    @Test
    public void testLog_String_ObjectArr() {
        String msg = "This is a %s message.";
        try {
           lg.init(stream);
           lg.log(msg, "formatted");
           assertFalse(stream.checkError());
           assertTrue(buffer.size() >= msg.length());
       } catch(Exception e) {
           fail(e.getMessage());
       }
    }

    /**
     * Test of debug method, of class ToolsetLogger.
     */
    @Test
    public void testDebug_String() {
        String msg = "test";
       try {
           lg.init(stream, true);
           lg.debug(msg);
           assertFalse(stream.checkError());
           assertTrue(buffer.size() >= msg.length());
       } catch(Exception e) {
           fail(e.getMessage());
       }
    }

    /**
     * Test of debug method, of class ToolsetLogger.
     */
    @Test
    public void testDebug_String_ObjectArr() {
       String msg = "This is a %s message.";
       try {
           lg.init(stream, true);
           lg.debug(msg, "formatted");
           assertFalse(stream.checkError());
           assertTrue(buffer.size() >= msg.length());
       } catch(Exception e) {
           fail(e.getMessage());
       }
    }
    
    @Test
    public void testWith_Percent() {
    	// Strings containing a % can crash printf
    	String msg = "VarName: Variable; Value: %P";
    	try {
            lg.init(stream, true);
            lg.debug(msg);
        } catch(Exception e) {
        	// Hopefully does not throw 
        	// java.util.UnknownFormatConversionException: Conversion = 'P'
            fail(e.getMessage());
        }
    }
}
