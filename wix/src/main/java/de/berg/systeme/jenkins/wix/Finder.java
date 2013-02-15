package de.berg.systeme.jenkins.wix;

import hudson.FilePath;

import java.io.IOException;

public class Finder {
	
	public static boolean validateFiles(FilePath workspace, String relative) {
		try {
			workspace.validateRelativeDirectory(relative);
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	public static FilePath[] findFiles(FilePath workspace, String value) {
		String[] sources = null;
		FilePath[] pathes = null;
		if (value.contains(",")) {
			sources = value.split(",");
		} else if (value.contains(";")) {
			sources = value.split(";");
		} else {
			sources = new String[1];
			sources[0] = value;
		}
		pathes = new FilePath[sources.length];
		for (int i=0; i < sources.length; ++i) {
			pathes[0] = new FilePath(workspace, sources[i]);
		}
		return pathes;
	}
}
