package de.berg.systeme.jenkins.wix;

import java.util.Properties;

public class ToolsetSettings {
	private final Properties properties;
	
	public ToolsetSettings() {
		this(new Properties());
	}
	
	public ToolsetSettings(Properties properties) {
		this.properties = properties;
	}
	
	public String get(String key, String defaultValue) {
		String tmp = this.properties.getProperty(key);
		if (tmp == null) return defaultValue;
		return tmp;
	}
	
	public boolean get(String key, boolean defaultValue) {
		String tmp = this.properties.getProperty(key);
		if (tmp == null) return defaultValue;
		try {
			boolean rvalue = Boolean.parseBoolean(tmp);
			return rvalue;
		} catch (Exception e) {
			return defaultValue;
		}
	}
	
	public double get(String key, double defaultValue) {
		String tmp = this.properties.getProperty(key);
		if (tmp == null) return defaultValue;
		double rvalue;
		try {
			rvalue = Double.parseDouble(tmp);
			return rvalue;
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
	
	public void set(String key, String value) {
		this.properties.setProperty(key, value);
	}
	
	public void set(String key, boolean value) {
		set(key, String.valueOf(value));
	}

	public void set(String key, float value) {
		set(key, String.valueOf(value));
	}
}
