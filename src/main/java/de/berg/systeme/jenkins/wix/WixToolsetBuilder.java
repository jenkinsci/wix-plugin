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
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.servlet.ServletException;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * WIXToolset {@link Builder}.
 *
 * <p>
 * This {@link Builder} creates an MSI package from a given source file.
 * <p>
 *
 * @author Bjoern Berg, bjoern.berg@gmx.de
 */
public class WixToolsetBuilder extends Builder {
    private static final ResourceBundle messages = ResourceBundle.getBundle("Messages");
    private final String sources;
    private final String msiOutput;
    private final String arch;
    private final ToolsetSettings settings;
    private final ToolsetLogger lg = ToolsetLogger.INSTANCE;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public WixToolsetBuilder(String sources, boolean markAsUnstable, boolean compileOnly, 
    						 boolean useUiExt, boolean useUtilExt, boolean useBalExt, 
    						 boolean useComPlusExt, boolean useDependencyExt, 
    						 boolean useDifxAppExt, boolean useDirectXExt, boolean useFirewallExt, 
    						 boolean useGamingExt, boolean useIISExt, boolean useMsmqExt, 
    						 boolean useNetfxExt, boolean usePsExt, boolean useSqlExt, 
    						 boolean useTagExt, boolean useVsExt, String msiOutput, String arch) {
        // This is only executed if the job is reconfigured
    	this.sources = sources;
        this.msiOutput = msiOutput;
        this.arch = arch;
        settings = new ToolsetSettings();
    	settings.set(Wix.MARK_UNSTABLE, markAsUnstable);
    	settings.set(Wix.COMPILE_ONLY, compileOnly);
    	settings.set(Wix.INST_PATH, getDescriptor().getInstPath());
    	settings.set(Wix.DEBUG_ENBL, getDescriptor().getEnableDebug());
    	settings.set(Wix.EXT_BAL, useBalExt);
    	settings.set(Wix.EXT_COMPLUS, useComPlusExt);
    	settings.set(Wix.EXT_DEPENDENCY, useDependencyExt);
    	settings.set(Wix.EXT_DIFXAPP, useDifxAppExt);
    	settings.set(Wix.EXT_DIRECTX, useDirectXExt);
    	settings.set(Wix.EXT_FIREWALL, useFirewallExt);
    	settings.set(Wix.EXT_GAMING, useGamingExt);
    	settings.set(Wix.EXT_IIS, useIISExt);
    	settings.set(Wix.EXT_MSMQ, useMsmqExt);
    	settings.set(Wix.EXT_NETFX, useNetfxExt);
    	settings.set(Wix.EXT_PS, usePsExt);
    	settings.set(Wix.EXT_SQL, useSqlExt);
    	settings.set(Wix.EXT_TAG, useTagExt);
    	settings.set(Wix.EXT_UI, useUiExt);
    	settings.set(Wix.EXT_UTIL, useUtilExt);
    	settings.set(Wix.EXT_VS, useVsExt);
        settings.set(Wix.MSI_PKG, msiOutput);
    }

	////////////////////////////////////////////////////////////////////////////
	// Getters to configure the frontend and fetch the data from ToolsetSettings.
	// Getters must match the names of boolean fields in config.jelly. 
        /***
	 * Helper method for reading settings by their name. If option is not found
	 * the method always returns fals.
	 * @param wixOption name of option. See {@link Wix} for details.
	 * @return boolean value; false or true.
	 */
	protected boolean getValue(String wixOption) {
	  return (settings != null) ? settings.get(wixOption, false) : false;
	}
	public boolean getMarkAsUnstable()	{ return getValue(Wix.MARK_UNSTABLE); }
	public boolean getCompileOnly()		{ return getValue(Wix.COMPILE_ONLY); }
	public boolean getUseUiExt()		{ return getValue(Wix.EXT_UI); } 
	public boolean getUseUtilExt()		{ return getValue(Wix.EXT_UTIL); } 
	public boolean getUseBalExt()		{ return getValue(Wix.EXT_BAL); } 
	public boolean getUseComPlusExt()	{ return getValue(Wix.EXT_COMPLUS); } 
	public boolean getUseDependencyExt()    { return getValue(Wix.EXT_DEPENDENCY); } 
	public boolean getUseDifxAppExt()	{ return getValue(Wix.EXT_DIFXAPP); } 
	public boolean getUseDirectXExt()	{ return getValue(Wix.EXT_DIRECTX); } 
	public boolean getUseFirewallExt()	{ return getValue(Wix.EXT_FIREWALL); } 
	public boolean getUseGamingExt()	{ return getValue(Wix.EXT_GAMING); } 
	public boolean getUseIISExt()		{ return getValue(Wix.EXT_IIS); } 
	public boolean getUseMsmqExt()		{ return getValue(Wix.EXT_MSMQ); } 
	public boolean getUseNetfxExt()		{ return getValue(Wix.EXT_NETFX); } 
	public boolean getUsePsExt()		{ return getValue(Wix.EXT_PS); } 
	public boolean getUseSqlExt()		{ return getValue(Wix.EXT_SQL); } 
	public boolean getUseTagExt()		{ return getValue(Wix.EXT_TAG); } 
	public boolean getUseVsExt()		{ return getValue(Wix.EXT_VS); }
        public String getMsiOutput()            { return msiOutput; }
        public String getArch()                 { return arch; }
	public String getSources()		{ return sources; }
	///////////////////////// End of Getter section ////////////////////////////
	
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        // At this point we also have to check the global variables again
        settings.set(Wix.INST_PATH, getDescriptor().getInstPath());
    	settings.set(Wix.DEBUG_ENBL, getDescriptor().getEnableDebug());
        
	boolean performedSuccessful = false;
        final String instPath = settings.get(Wix.INST_PATH, "");
        final boolean debugEnabled = Boolean.valueOf(settings.get(Wix.DEBUG_ENBL, "false"));
    	
        if (instPath == null || "".equals(instPath)) {
            listener.getLogger().println(messages.getString("TOOLSET_NOT_CONFIGURED"));
            performedSuccessful = false;
    	} else {
            try {
              // initialize our own logger
              listener.getLogger().println(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("Messages").getString("ENABLE_DEBUG"), new Object[] {debugEnabled}));
              ToolsetLogger.INSTANCE.init(listener.getLogger(), debugEnabled);

              // get all environment variables
              listener.getLogger().println(messages.getString("DETECTING_ENVIRONMENT_VARIABLES"));
              EnvVars envVars = build.getEnvironment(listener);

              //FilePath sourceFile = new FilePath(build.getWorkspace(), getSources());
              FilePath[] sources = build.getWorkspace().list(getSources());
              listener.getLogger().println(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("Messages").getString("FOUND_SOURCES"), new Object[] {sources.length}));

              listener.getLogger().println(messages.getString("INITIALIZING_TOOLS"));
              Toolset toolset = new Toolset(settings, envVars);
              // add architecture for compiler
              toolset.setArchitecture(arch);
              listener.getLogger().println(messages.getString("STARTING_COMPILE_PROCESS"));
              FilePath objFile = toolset.compile(sources);
              if (settings.get(Wix.COMPILE_ONLY, false)) {
                  listener.getLogger().println(messages.getString("SKIPPING_LINK"));
              } else {
                  String output = settings.get(Wix.MSI_PKG, Wix.MSI_PKG_DEFAULT_NAME);
                  output = envVars.expand(output);
                  FilePath outFile = new FilePath(build.getWorkspace(), output);
                  listener.getLogger().println(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("Messages").getString("LINKING_TO"), new Object[] {outFile}));
                  toolset.link(objFile, outFile);
              }
              build.setResult(Result.SUCCESS);
              performedSuccessful = true;
            } catch (ToolsetException e) {
                listener.getLogger().println(e);
                build.setResult(settings.get(Wix.MARK_UNSTABLE, false) ? Result.UNSTABLE : Result.FAILURE);
                performedSuccessful = true;
            } catch (NullPointerException e) {
                listener.getLogger().println(e.getMessage());
                build.setResult(settings.get(Wix.MARK_UNSTABLE, false) ? Result.UNSTABLE : Result.FAILURE);
                performedSuccessful = true;
            } catch (IOException e) {
                listener.getLogger().println(e.getMessage());
                build.setResult(settings.get(Wix.MARK_UNSTABLE, false) ? Result.UNSTABLE : Result.FAILURE);
                performedSuccessful = false;
            } catch (InterruptedException e) {
                listener.getLogger().println(e.getMessage());
                build.setResult(settings.get(Wix.MARK_UNSTABLE, false) ? Result.UNSTABLE : Result.FAILURE);
                performedSuccessful = false;
            } catch (Exception ex) {
                listener.getLogger().println(ex.getMessage());
                build.setResult(settings.get(Wix.MARK_UNSTABLE, false) ? Result.UNSTABLE : Result.FAILURE);
                performedSuccessful = false;
            }
	  }
	  return performedSuccessful;
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public final DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    /**
     * Descriptor for {@link WixToolsetBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
    	// Globals
        private String instPath;
        private boolean enableDebug;
        
        public DescriptorImpl() {
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
        public FormValidation doCheckSource(@QueryParameter String value)
        throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.error(messages.getString("PLEASE_SET_A_NAME"));
			}
            if (value.length() < 4) {
                return FormValidation.warning(messages.getString("NAME_TOO_SHORT"));
			}           
            File directory = new File(value);
            if (!directory.exists()) {
            	return FormValidation.error(messages.getString("DOES_NOT_EXIST"));
            }
            return FormValidation.ok();
        }
        
        public FormValidation doCheckMsiOutput(@QueryParameter String value)
        throws IOException, ServletException {
            if (value == null || value.length() == 0) {
                return FormValidation.ok(messages.getString("USING_DEFAULT_SETUP_MSI"));
            }
            if (!value.toLowerCase().endsWith(".msi")) {
                return FormValidation.warning(messages.getString("NOT_A_VALID_PACKAGE_NAME"));
            }
            return FormValidation.ok();
        }
        
        public FormValidation doCheckInstPath(@QueryParameter String value)
        throws IOException, ServletException {
            if (value == null || value.length() == 0) {
                return FormValidation.error(messages.getString("REQUIRED"));
            }
            // Check if directory exists
            File directory = new File(value);
            if (!directory.exists()) {
            	return FormValidation.error(messages.getString("DOES_NOT_EXIST"));
            }
            // Check if directory contains compiler
            StringBuilder sb = new StringBuilder();
            sb.append(directory)
              .append(System.getProperty("file.separator"))
              .append(Wix.COMPILER);
            File compiler = new File(sb.toString());
            if (!compiler.exists()) {
                return FormValidation.error(messages.getString("CANNOT_FIND_COMPILER_IN_DIRECTORY"));
            }
            return FormValidation.ok();
        }
        
        /*public FormValidation doFindDirectory(@QueryParameter String instPath) throws IOException, ServletException {
            String pathVariable = System.getenv("PATH");
            String wixInstDirectory = "";
            try {
                for (String directory : pathVariable.split(";")) {
                    String linker = directory + System.getProperty("file.separator") + Wix.COMPILER;
                    File fp = new File(linker);
                    if (fp.exists()) {
                        wixInstDirectory = directory;
                        break;
                    }
                }
                
                // Read variables into path
                File[] programFiles = new File[2];
                programFiles[0] = new File(System.getenv("ProgramFiles"));
                programFiles[1] = new File(System.getenv("ProgramFiles(x86)"));
                
                Finder finder = new Finder(Wix.COMPILER);
                finder.walkFileTree(programFiles);
                
                StringBuilder found = new StringBuilder();
                for (File f : finder.getResults()) {
                    found.append(f.getAbsolutePath());
                    found.append(";");
                }
                wixInstDirectory = found.toString();
            } catch (NullPointerException e) {
                return FormValidation.error("Wix Toolset Installation not found.");
            }
            
            if (wixInstDirectory.isEmpty()) {
                return FormValidation.error("Wix Toolset Installation not found.");
            } else {
                instPath = wixInstDirectory;
                return FormValidation.ok("Success: " + wixInstDirectory);
            }
        }*/

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
	     * @return 
         */
        public String getDisplayName() {
            return messages.getString("WIX_TOOLSET");
        }
        
        /**
         * Creates the ListBoxModel from enum {@link Wix.Arch}.
         * @return 
         */
        public ListBoxModel doFillArchItems() {
            ListBoxModel items = new ListBoxModel();
            for (Wix.Arch cpu : Wix.Arch.values()) {
                items.add(cpu.toString(), cpu.toString());
            }
            return items;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            instPath = formData.getString("instPath");
            enableDebug = formData.getBoolean("enableDebug");
            
            /*settings.set(Wix.INST_PATH, instPath);
            settings.set(Wix.DEBUG_ENBL, enableDebug);*/
            
            //markAsUnstable = formData.getBoolean("markAsUnstable"); // only global config
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            return super.configure(req,formData);
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

        /*public boolean getMarkAsUnstable() {
        	return markAsUnstable;
        }*/
        
        public boolean getEnableDebug() {
        	return enableDebug;
        }
    }
}