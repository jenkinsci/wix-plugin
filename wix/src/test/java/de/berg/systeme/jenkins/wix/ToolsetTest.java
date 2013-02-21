package de.berg.systeme.jenkins.wix;

import junit.framework.Assert;

import org.junit.Test;

public class ToolsetTest {
	private static final String EXP_OBJFILE = "test.wixobj";
	private static final String SRCFILE = "test.wxs";
	private static final String EXP_MSIFILE = "test.msi";
	

	@Test
	public void testToObjectFilename() {
		String objfile = Toolset.toObjectFilename(SRCFILE);
		Assert.assertEquals(EXP_OBJFILE, objfile);
		
		objfile = Toolset.toObjectFilename(EXP_MSIFILE);
		Assert.assertFalse(EXP_OBJFILE.equalsIgnoreCase(objfile));
	}

	@Test
	public void testToMsiFilename() {
		String msifile = Toolset.toMsiFilename(SRCFILE);
		Assert.assertEquals(EXP_MSIFILE, msifile);
		
		msifile = Toolset.toMsiFilename(EXP_OBJFILE);
		Assert.assertFalse(EXP_MSIFILE.equalsIgnoreCase(msifile));
	}

	@Test
	public void testSetDefaultParameters() {
		String cmd = Toolset.setDefaultParameters(EXP_OBJFILE, SRCFILE);
		Assert.assertEquals("-nologo -out \"test.wixobj\" \"test.wxs\" -ext WixUIExtension", cmd);
		
		cmd = Toolset.setDefaultParameters(EXP_MSIFILE, EXP_OBJFILE);
		Assert.assertEquals("-nologo -out \"test.msi\" \"test.wixobj\" -ext WixUIExtension", cmd);
	}
}
