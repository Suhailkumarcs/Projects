NotebookApp - Build (JAR + Launch4j EXE + Inno Setup Installer)
App name: notebook

------------------------------------------------------------
0) Prerequisites
------------------------------------------------------------
1) Install Java (you already have):
   - javac and java should work.

2) Install these tools (only once on your machine):
   - Launch4j (for EXE launcher)
   - Inno Setup 6.x (for installer)

Note:
- This project is set up for Windows.
- Inno Setup script will bundle a JRE using Inno Setup's JRE support.

------------------------------------------------------------
1) Copy source code into this folder
------------------------------------------------------------
You already have these files on Desktop:
- notebook.java
- project.java

Copy them into:
- NotebookApp/src/
So final paths should be:
- NotebookApp/src/notebook.java
- NotebookApp/src/project.java

------------------------------------------------------------
2) Build JAR + compile
------------------------------------------------------------
Open VS Code terminal in:
- NotebookApp/
Run:
- build.bat
This will produce:
- NotebookApp/dist/notebook.jar

------------------------------------------------------------
3) Create EXE using Launch4j
------------------------------------------------------------
1) Open Launch4j GUI.
2) Load config file:
   - NotebookApp/launch4j/launch4j.xml
3) Ensure it points to:
   - inputJar: NotebookApp/dist/notebook.jar
4) Set output:
   - NotebookApp/dist/notebook.exe
5) Click "Build wrapper".

------------------------------------------------------------
4) Create installer using Inno Setup
------------------------------------------------------------
1) Open Inno Setup.
2) Open script:
   - NotebookApp/installer/setup.iss
3) Build.
This produces:
- NotebookApp/dist/notebook_setup.exe

------------------------------------------------------------
5) Test on your machine
------------------------------------------------------------
1) Run installer.
2) Check installed folder under Program Files.
3) Launch from Start Menu shortcut.

------------------------------------------------------------
Important note about "har system" (all systems)
------------------------------------------------------------
Inno Setup is configured to bundle a JRE so the installer can run on systems
without Java installed.

If Inno Setup build fails due to missing JRE files, download the required
JRE/Java runtime for bundling and set its path in setup.iss.

------------------------------------------------------------
Troubleshooting
------------------------------------------------------------
- If the EXE shows "Main class not found": ensure Launch4j mainClass is "project".
- If JAR runs but EXE fails: rebuild wrapper and confirm jar path.
- If installer fails: open setup.iss in Inno Setup and check "SourceDir" variables.

