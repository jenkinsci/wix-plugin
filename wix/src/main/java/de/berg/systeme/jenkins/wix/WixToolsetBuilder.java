package de.berg.systeme.jenkins.wix;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

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

    private final String sources;
    private final ToolsetSettings settings;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public WixToolsetBuilder(String sources, boolean markAsUnstable, boolean compileOnly, 
    						 boolean useUiExt, boolean useUtilExt, boolean useBalExt, 
    						 boolean useComPlusExt, boolean useDependencyExt, 
    						 boolean useDifxAppExt, boolean useDirectXExt, boolean useFirewallExt, 
    						 boolean useGamingExt, boolean useIISExt, boolean useMsmqExt, 
    						 boolean useNetfxExt, boolean usePsExt, boolean useSqlExt, 
    						 boolean useTagExt, boolean useVsExt) {
    	this.sources = sources;
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
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getSources() {
        return sources;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
    	final String instPath = settings.get(Wix.INST_PATH, "");
    	if (instPath == null) {
    		listener.getLogger().println("Toolset not configured.");
    		return false;
    	}
    	try {
    		// TODO use ant style file pattern
    		FilePath sourceFile = new FilePath(build.getWorkspace(), getSources());
    		listener.getLogger().println("Found file: " + sourceFile);
    		listener.getLogger().println("Initializing tools...");
			listener.getLogger().println("Starting compile process...");
			Toolset toolset = new Toolset(settings, listener);
			toolset.compile(sourceFile);
			if (settings.get(Wix.COMPILE_ONLY, false)) {
				listener.getLogger().println("Skipping link process!");
			} else {
				listener.getLogger().println("Linking...");
				toolset.link();
			}
		} catch (ToolsetException e) {
			listener.getLogger().println(e);
			build.setResult(settings.get(Wix.MARK_UNSTABLE, false) ? Result.UNSTABLE : Result.FAILURE);
			return true;
		} catch (NullPointerException e) {
			listener.getLogger().println(e.getMessage());
			build.setResult(settings.get(Wix.MARK_UNSTABLE, false) ? Result.UNSTABLE : Result.FAILURE);
			return true;
		}
    	build.setResult(Result.SUCCESS);
        return true;
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    /**
     * Descriptor for {@link HelloWorldBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
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
        // Build
        private boolean markAsUnstable;
        
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
         */
        public FormValidation doCheckSource(@QueryParameter String value)
        throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a name");
            if (value.length() < 4)
                return FormValidation.warning("Isn't the name too short?");
            File directory = new File(value);
            if (!directory.exists()) {
            	return FormValidation.error("Does not exist.");
            }
            return FormValidation.ok();
        }
        
        public FormValidation doCheckInstPath(@QueryParameter String value)
        throws IOException, ServletException {
            if (value == null || value.length() == 0) {
                return FormValidation.error("Required.");
            }
            File directory = new File(value);
            if (!directory.exists()) {
            	return FormValidation.error("Does not exist.");
            }
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "WIX Toolset";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            //useFrench = formData.getBoolean("useFrench");
            instPath = formData.getString("instPath");
            enableDebug = formData.getBoolean("enableDebug");
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
         */
        public String getInstPath() {
        	return instPath;
        }

        public boolean getMarkAsUnstable() {
        	return markAsUnstable;
        }
        
        public boolean getEnableDebug() {
        	return enableDebug;
        }
    }
}

