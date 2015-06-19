package de.berg.systeme.jenkins.wix;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ToolsetSettingsTest {
	protected Properties p;
	protected ToolsetSettings ts;

	@Before
	public void setUp() throws Exception {
		p = new Properties();
		p.setProperty(Wix.COMPILE_ONLY, "false");
		p.setProperty(Wix.INST_PATH, "/hallo/welt");
		p.setProperty("Level", "1.0");
		ts = new ToolsetSettings(p);
	}

	@After
	public void tearDown() throws Exception {
		p = null;
	}

	@Test
	public void testGetStringString() {
		assertEquals("/hallo/welt", ts.get(Wix.INST_PATH, "/hallo/du"));
		assertEquals("/hallo/du", ts.get(Wix.EXT_BAL, "/hallo/du"));
	}

	@Test
	public void testGetStringBoolean() {
		assertEquals(false, ts.get(Wix.COMPILE_ONLY, true));
		// can be parsed but evaluates to false
		assertEquals(false, ts.get(Wix.INST_PATH, true));
	}

	@Test
	public void testGetStringFloat() {
		assertEquals(1.0, ts.get("Level", 2.0), 0);
		assertEquals(2.0, ts.get("Meins", 2.0), 0);
	}
}
