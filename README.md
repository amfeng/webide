# WebIDE 
Built by Amber Feng, Allen Chen, and Stephen Tu for the 2010 Yahoo Hack Day at UC Berkeley

WebIDE is a collaborative (real-time) browser IDE complete with syntax highlighting, compile/warning line highlighting, method list via reflection and byte-code analysis, and in-browser console (with standard input).

# Build instructions
1. Download sbt: http://code.google.com/p/simple-build-tool
2. Download source code:

    git clone git@github.com:afeng/webide.git
3. Run sbt to build:

    cd webide

    sbt update ~jetty-run
4. Now go to http://localhost:8080

Note that this requires JDK 1.6 (not just JRE, since we use javax.tools.*)
