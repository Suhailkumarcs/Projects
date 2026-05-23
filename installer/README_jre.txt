JRE bundling note for setup.iss

In setup.iss I referenced:
  installer/jre/jre-setup.exe

You must download a Windows Java Runtime installer that supports silent install
and rename/copy it to:
  NotebookApp/installer/jre/jre-setup.exe

Then update Parameters in setup.iss [Run] section if your installer uses different
silent flags.

Examples of common silent flags (depends on the installer you download):
- /s
- /quiet
- /silent

If you want, tell me which JRE installer you download (name) and I will adjust
setup.iss accordingly.

