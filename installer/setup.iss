; Inno Setup installer for notebook (bundles JRE + launches EXE)
; App name: notebook

[Setup]
AppName=notebook
AppVersion=1.0
AppPublisher=You
DefaultDirName={autopf}\notebook
DefaultGroupName=notebook
OutputDir=dist
OutputBaseFilename=notebook_setup
Compression=lzma
SolidCompression=yes
PrivilegesRequired=admin
ArchitecturesInstallIn64BitMode=x64

[Files]
; Launch4j EXE
Source: "../dist/notebook.exe"; DestDir: "{app}"; Flags: ignoreversion

; (Optional) Include JAR if you want (not required for wrapper)
Source: "../dist/notebook.jar"; DestDir: "{app}"; Flags: ignoreversion

; JRE bundling:
; Inno Setup can bundle JRE if you provide the runtime installer.
; Replace "jre-setup.exe" with the actual JRE installer file you download.
; Put it into NotebookApp/installer/jre\ directory.
Source: "jre\\jre-setup.exe"; DestDir: "{tmp}"; Flags: deleteafterinstall

[Icons]
Name: "{group}\notebook"; Filename: "{app}\notebook.exe"

[Run]
; Install bundled JRE silently if needed.
; Note: this is handled via "Check and install JRE" step.
Filename: "{tmp}\jre-setup.exe"; Parameters: "/s"; Flags: runhidden; Check: NeedsJre

; Run app after install (optional). Comment out if not needed.
; Filename: "{app}\notebook.exe"; Flags: nowait postinstall

[Code]
function GetJavaVersion: string;
var
  Output: string;
begin
  Result := '';
  try
    { Try to read Java version from java.exe if present }
    if Exec('java', '-version', '', SW_HIDE, ewWaitUntilTerminated, Output) then
      Result := Output;
  except
    Result := '';
  end;
end;

function JavaPresent: Boolean;
var
  Ver: string;
begin
  Ver := GetJavaVersion;
  Result := Ver <> '';
end;

function NeedsJre: Boolean;
begin
  { Basic check: if java not found, install bundled JRE }
  Result := not JavaPresent;
end;

