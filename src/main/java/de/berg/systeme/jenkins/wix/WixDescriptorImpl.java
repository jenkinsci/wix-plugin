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

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Descriptor for {@link WixToolsetBuilder}. Used as a singleton.
 * The class is marked as public so that it can be accessed from views.
 */
// This indicates to Jenkins that this is an implementation of an extension point.
@Extension
public final class WixDescriptorImpl extends BuildStepDescriptor<Builder> {
  /**
   * To persist global configuration information,
   * simply store it in a field and call save().
   *
   * <p>
   * If you don't want fields to be persisted, use <tt>transient</tt>.
   */
  // Globals
  private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("Messages");
  private String instPath = "";
  private boolean enableDebug = false;
  private boolean enableVars = false;
  private boolean usedOnSlave = false;
  private String rejectedVars = Wix.DEF_LOV_TO_REJECT;
  private static final ToolsetLogger lg = ToolsetLogger.INSTANCE;

  public WixDescriptorImpl() {
    super(WixToolsetBuilder.class);
    load();
  }

  /**
   * Performs on-the-fly validation of the form field 'name'.
   *
   * @param value
   *      This parameter receives the value that the user has typed.
   * @return
   *      Indicates the outcome of the validation. This is sent to the browser.
   * @throws java.io.IOException
   * @throws javax.servlet.ServletException
   */
  public FormValidation doCheckSource(@QueryParameter String value) throws IOException, ServletException {
    if (value.length() == 0) {
      return FormValidation.error(MESSAGES.getString("PLEASE_SET_A_NAME"));
    }
    if (value.length() < 4) {
      return FormValidation.warning(MESSAGES.getString("NAME_TOO_SHORT"));
    }
    File directory = new File(value);
    if (!directory.exists()) {
      return FormValidation.error(MESSAGES.getString("DOES_NOT_EXIST"));
    }
    return FormValidation.ok();
  }

  public FormValidation doCheckMsiOutput(@QueryParameter String value) throws IOException, ServletException {
	if (value == null || value.length() == 0) {
	  return FormValidation.ok(MESSAGES.getString("USING_DEFAULT_SETUP_MSI"));
	}
	if (value.contains("*")) {
	  return FormValidation.error(MESSAGES.getString("NO_PATTERNS"));
	}
	if (!value.toLowerCase().matches(".*[\\.exe|\\.msi|\\.msm]{4,4}$")) {
		return FormValidation.warning(MESSAGES.getString("PROVIDE_FILE_ENDING"));
	}
	if (value.toLowerCase().endsWith(".exe")) {
	  return FormValidation.warning(MESSAGES.getString("BALEXT_NEEDED"));
	}
	if (!value.toLowerCase().endsWith(".msi") && 
		!value.toLowerCase().endsWith(".msm")) {
	  return FormValidation.warning(MESSAGES.getString("NOT_A_VALID_PACKAGE_NAME"));
	}
	return FormValidation.ok();
  }

  public FormValidation doCheckInstPath(@QueryParameter String value) throws IOException, ServletException {
    if (value == null || value.length() == 0) {
      return FormValidation.warning(MESSAGES.getString("EXPECTING_IN_PATH"));
    }
    // Check if directory exists
    File directory = new File(value);
    if (!directory.exists()) {
      return FormValidation.error(MESSAGES.getString("DOES_NOT_EXIST"));
    }
    // Check if directory contains compiler
    StringBuilder sb = new StringBuilder();
    sb.append(directory).append(System.getProperty("file.separator")).append(Wix.COMPILER);
    File compiler = new File(sb.toString());
    if (!compiler.exists()) {
      return FormValidation.error(MESSAGES.getString("CANNOT_FIND_COMPILER_IN_DIRECTORY"));
    }
    return FormValidation.ok();
  }
  
  public FormValidation doCheckRejectedVarsList(@QueryParameter String value) throws IOException, ServletException {
    if (value.contains(" ") || value.contains(";")) {
      return FormValidation.error("Use ',' to separate environment variables.");
    }
    
    return FormValidation.ok();
  }

  @SuppressWarnings("rawtypes")
  public boolean isApplicable(Class<? extends AbstractProject> aClass) {
    // Indicates that this builder can be used with all kinds of project types
    return true;
  }

  /**
   * This human readable name is used in the configuration screen.
   * @return
   */
  public String getDisplayName() {
    return MESSAGES.getString("WIX_TOOLSET");
  }

  /**
   * Creates the ListBoxModel from enum {@link Wix.Arch}.
   * @return
   */
  public ListBoxModel doFillArchItems() {
    ListBoxModel items = new ListBoxModel();
    for (Wix.Arch cpu : Wix.Arch.values()) {
      // key and value are identical
      items.add(cpu.toString(), cpu.toString());
    }
    return items;
  }

  @Override
  public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
    try {
        // Needed settings
        this.instPath = formData.getString("instPath");
        this.enableDebug = formData.getBoolean("enableDebug");
        this.usedOnSlave = formData.getBoolean("usedOnSlave");
        
        // Optional field for Variable rejection
        JSONObject varList = formData.getJSONObject("enableVars");
        if (varList != null && varList.size() > 0) {
            this.enableVars = true;
            this.rejectedVars = varList.optString("rejectedVarsList", "");
        } else {
            this.enableVars = false;
        }
    } catch (Exception e) {
        lg.severe(e);
    } finally {
        save();
    }
    
    return super.configure(req, formData);
  }

  /**
   * This method returns true if the global configuration says we should speak French.
   *
   * The method name is bit awkward because global.jelly calls this method to determine
   * the initial state of the checkbox by the naming convention.
   * @return
   */
  public String getInstPath() {
      return instPath;
  }

  public boolean getEnableDebug() {
      return enableDebug;
  }
  
  public String getRejectedVarsList() {
    return rejectedVars;
  }
  
  public boolean getEnableVars() {
    return enableVars;
  }

	public boolean getUsedOnSlave() {
		return usedOnSlave;
	}
  
}
