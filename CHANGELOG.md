ChangeLog
=========
Changelog between all revisions of WiX Toolset plugin for Jenkins.

Changes between Wix Toolset plugin 1.7 and 1.8
----------------------------------------------
Released on: 2014-09-26

### Minor changes
* Added french translation. Thanks to _mildis_.
* Added new help file for MSI Output.
* MSI Output can be also an executable (*.exe) if used together with bootstrapper.
* Some minor refactoring.

Changes between Wix Toolset plugin 1.6 and 1.7
----------------------------------------------
Released on: 2014-08-06

Versions prior to 1.7 had problems with whitespaces in the installation directory of WiX Toolset.
The Builder for executing the command is changed to wrap the commands candle.exe and light.exe with double quotes.


Changes between Wix Toolset plugin 1.5 and 1.6
----------------------------------------------
Released on: 2014-07-18

Version 1.5 contained a critical error which made it impossible to build any setup with WiX Toolset. 
This error was produced while resolving a translated message.


Changes between Wix Toolset plugin 1.4 and 1.5
----------------------------------------------
### Define MSI package name
It is now possible to define a filename for the MSI package. If this *Advanced Setting* is left blank the MSI package name defaults to *setup.msi*.
Environment variables (as long as defined) are expanded to their value. A package name like setup-${BUILD_NUMBER}.msi results for e.g. to setup-40.msi.

### Define defaults for architecture
Set architecture defaults for package, components, etc. values: x86, x64, or ia64 (default: x86)

### Minor changes
* On the system configuration page the plugin is now called Wix Toolset instead of Windows Installer Builder.
* Extended validation of installation directory. Checks now if directory contains compiler candle.exe und linker light.exe.
* English and german messages depending on your system settings.


Changes between Wix Toolset plugin 1.3 and 1.4
----------------------------------------------
### Environment variables passed as parameters
Environment variables are passed to Compiler candle.exe and light.exe as parameters as long as they are not defined as automatically
rejected or contain directory entries with spaces. There is a list of automatically rejected variables:
* Path
* CommonProgramFiles
The list will be updated in future versions to avoid problems during compilation or linking.
Environment variables containing a '=' in its name are also automatically rejected.

### Ant-style definition for source files
Since version 1.3 Wix Toolset plugin was not able to resolve Ant-style file patterns like **/*.wxs to all WiX source files in a project.
This is now possible with 1.4. All found source files are compiled to a single object file and linked into a single MSI package.
The MSI package is named like the first source file resolved by the Ant pattern or a list of named source files. E.g. for the definition of source files are as follows:
* **/*.wxs will find all source files inside the build project
* **/*v13*.wxs will compile and link all files containing v13 in their filenames.
* source1.wxs, source2.wxs will compile and link only source1.wxs and source2.wxs

### Major refactoring
A lot of code was refactored to release faster and improve the plugin.


Changes between Wix Toolset plugin 1.2 and 1.3
----------------------------------------------
### Bugfix: Saving settings
All settings are now saved after reconfiguring a job or restarting Jenkins.

### Added descriptions
Some options now have their own descriptions accessible via ?.


Wix Toolset plugin  1.2
-----------------------
### Official release
Version 1.2 was the official release after internal tests with Version 1.1