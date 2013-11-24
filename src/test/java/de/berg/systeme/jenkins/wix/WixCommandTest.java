package de.berg.systeme.jenkins.wix;

import java.io.File;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class WixCommandTest {
	private static final String EXE = "test.exe";
	private File f;
	

	@Before
	public void setUp() throws Exception {
		f = new File(EXE);
	}

	@After
	public void tearDown() throws Exception {
		f = null;
	}

	@Test
	public void testCreateCommand() {
		// With null arguments
		WixCommand cmd1 = new WixCommand(f);
		Assert.assertEquals(f.getAbsolutePath(), cmd1.toString());  
		
		// With several arguments
		final String expected2 = f.getAbsolutePath() + " param1 param2 param3";
		WixCommand cmd2 = new WixCommand(f, "param1", "param2", "param3");
		Assert.assertEquals(expected2, cmd2.toString());
	}
}
