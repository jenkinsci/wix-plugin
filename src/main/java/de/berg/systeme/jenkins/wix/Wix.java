package de.berg.systeme.jenkins.wix;

/***
 * Pre-defined keys.
 * 
 * @author Bjoern Berg, bjoern.berg@gmx.de
 *
 */
public interface Wix {
    // --- accessors for ToolsetSettings
    public static final String INST_PATH = "installation.path";
    public static final String DEBUG_ENBL = "debug";
    public static final String COMPILE_ONLY = "compile.only";
    public static final String MARK_UNSTABLE = "mark.unstable";
    public static final String USED_ON_SLAVE = "used.on.slave";
    public static final String ENBL_ENV_AS_PARAM = "env.as.param";
    public static final String LOV_REJECTED = "sys.env";
    public static final String DEF_LOV_TO_REJECT = "Path,CommonProgramFiles";
    public static final String EXT_BAL = "WixBalExtension";
    public static final String EXT_UI = "WixUIExtension";
    public static final String EXT_UTIL = "WixUtilExtension";
    public static final String EXT_COMPLUS = "WixComPlusExtension";
    public static final String EXT_DEPENDENCY = "WixDependencyExtension";
    public static final String EXT_DIFXAPP = "WixDifxAppExtension";
    public static final String EXT_DIRECTX = "WixDirectXExtension";
    public static final String EXT_FIREWALL = "WixFirewallExtension";
    public static final String EXT_GAMING = "WixGamingExtension";
    public static final String EXT_IIS = "WixIIsExtension";
    public static final String EXT_MSMQ = "WixMsmqExtension";
    public static final String EXT_NETFX = "WixNetFxExtension";
    public static final String EXT_PS = "WixPSExtension";
    public static final String EXT_SQL = "WixSqlExtension";
    public static final String EXT_TAG = "WixTagExtension";
    public static final String EXT_VS = "WixVSExtension";
    public static final String MSI_PKG = "MsiPackage";
    
    // --- predefined links
    public static final String COMPILER = "candle.exe";
    public static final String LINKER = "light.exe";
    public static final String MSI_PKG_DEFAULT_NAME = "setup.msi";
    
    /**
     * preferred architecture for candle.
     */
    public enum Arch {
        x86, x64, ia64
    }
}
