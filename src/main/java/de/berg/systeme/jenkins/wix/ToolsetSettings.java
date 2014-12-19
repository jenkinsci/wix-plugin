package de.berg.systeme.jenkins.wix;

import java.util.Properties;
import java.util.ResourceBundle;
import org.apache.commons.lang.StringUtils;

public class ToolsetSettings {
    private static final ResourceBundle messages = ResourceBundle.getBundle("Messages");
    private final Properties properties;

    public ToolsetSettings() {
            this(new Properties());
    }

    public ToolsetSettings(Properties properties) {
            this.properties = properties;
    }

    public String get(String key, String defaultValue) {
        String tmp = this.properties.getProperty(key);
        return (StringUtils.isEmpty(tmp)) ? defaultValue : tmp;
    }

    public boolean get(String key, boolean defaultValue) {
        boolean rvalue = defaultValue;
        String tmp = this.properties.getProperty(key);
        if (tmp != null) {
            try {
                rvalue = Boolean.parseBoolean(tmp);
            } catch (Exception e) {
                // nothing to do
            }
        }
        return rvalue;
    }

    public double get(String key, double defaultValue) {
        double rvalue = defaultValue;
        String tmp = this.properties.getProperty(key);
        if (tmp != null) {
            try {
                rvalue = Double.parseDouble(tmp);
            } catch (NumberFormatException e) {
                // nothing
            }
        }
        return rvalue;
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
