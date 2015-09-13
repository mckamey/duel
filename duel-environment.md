DEV Environment
===============

Last updated 2014-03-27.

----

These instructions assume only a clean install of Mac OS X 10.9.1 Mavericks with `sudo` root access. It will probably work with many other operating systems and configurations but this is the expected environment used here. These software packages are best installed in the following order as there may be some dependencies between.

1 Install Latest JDK
--------------------

1. Install Java SE Development Kit

		http://www.oracle.com/technetwork/java/javase/downloads/

2. Test that it works in Terminal via:

		$ java -version

3. Edit (or create if missing) `~/.bash_profile` to set `JAVA_HOME`:

		export JAVA_HOME=$(/usr/libexec/java_home)

	This will expand the proper location of `JAVA_HOME`.

4. In the Java control panel, under the Security tab, disable Java in the browser unless you need it.

2 Install Xcode Command-Line Tools
----------------------------------

Xcode not only installs the IDE needed to build iOS apps, but it also includes the necessary command line tools which are often prerequisites to many other tools. These are the dev utilities which often come by default on other *NIX distributions.

1. Install the Xcode command line tools from Terminal:

		$ xcode-select --install

2. Choose `Install` to download and install just the command-line tools.

3. Alternatively, if the environment will eventually need to build the iOS app, then you can choose `Get Xcode` to install the full IDE.

3 Install Homebrew
------------------

Homebrew is the leading package manager for OS X. It helps install and keep track of various command line tools and their dependencies.

1. Follow the online instructions to ensure the most up to date. Generally it involves cutting and pasting the one line installation script into Terminal:

		http://brew.sh

2. Run configuration setup in Terminal:

		$ brew doctor

4 Install DiffMerge
-------------------

1. Download and install SourceGear DiffMerge (currently 4.2.0):

		http://sourcegear.com/diffmerge/downloads.php
		
	Alternatively, the previous version (3.3.2) lacks the annoying splash screen:

		http://download-us.sourcegear.com/DiffMerge/3.3.2/DiffMerge.3.3.2.1139.pkg

5 Install Hg
------------

1. Use Homebrew to install Mercurial Distributed SCM (currently 2.9):

		$ brew install hg

2. Verify Mercurial is working:

		$ hg --version

3. Create `~/.hgrc` to set up Mercurial configuration (replace `MYUSERNAME` with your Bitbucket username) 
	and enable `hg diffmerge` as a [command in Mercurial](http://mercurial.selenic.com/wiki/DiffMerge):

		# Mercurial user configuration

		[auth]
		bb.prefix = https://bitbucket.org/MYUSERNAME
		bb.username = MYUSERNAME

		[ui]
		username = MYUSERNAME
		editor = nano
		merge = diffmerge

		[extensions]
		shelve =
		hgext.extdiff =

		[extdiff]
		cmd.diffmerge = diffmerge

		[merge-tools]
		diffmerge.executable = diffmerge
		diffmerge.binary = False
		diffmerge.symlinks = False
		diffmerge.args = -merge -result=$output -t1="Local Version" -t2=$output -t3="Other Version" -caption=$output $local $base $other
		diffmerge.gui = True

6 Install Maven
---------------

1. Use Homebrew to install Maven (currently 3.1.1):

		$ brew install maven

2. Verify Maven and Java are working together:

		$ mvn --version

7 Install JRebel (optional)
---------------------------

1. Download JRebel (currently 5.5.2):

		http://www.zeroturnaround.com/jrebel/current/

2. Extract the archive and move the directory to `/usr/local/jrebel`.

3. Add `$JREBEL_OPTS` configuration to `~/.bash_profile`:

		export JREBEL_OPTS='-noverify -javaagent:/usr/local/jrebel/jrebel.jar'

4. Run the activation script:

		$ /usr/local/jrebel/bin/activate-gui.sh


8 Install Eclipse
-----------------

1. Download "Eclipse IDE for Java EE Developers, Mac OS X 64-bit" (currently Kepler 4.3.1)

		http://www.eclipse.org/downloads/packages/eclipse-ide-java-ee-developers/keplersr1

2. Extract the archive and move the `eclipse` folder into the `Applications` folder.

3. The first time Eclipse is launched, it will prompt to install JDK 6. Allow it but after verify that `JAVA_HOME` still resolves to JDK 7 in Terminal:

		$ echo $JAVA_HOME

4. Next install `JSHint Integration for Eclipse IDE` by following the online instructions:

		http://github.eclipsesource.com/jshint-eclipse/install.html

5. Run `Help` > `Check for Updates` in Eclipse.

6. Add ability to launch (more than one) Eclipse via Terminal by adding this function to `~/.bash_profile`:

		eclipse() {
			open -n /Applications/eclipse/Eclipse.app
		}

9 Configure an Eclipse Workspace
--------------------------------

1. Create new Workspace (NOTE: multiple instances of Eclipse cannot share workspaces).

2. Map `*.duel` extension to HTML Editor:

		`Preferences` >> `General` >> `Editors` >> `File Associations` >> `Add...`

		`Preferences` >> `General` >> `Content Types` >> `Text` >> `HTML` >> `Add...`

3. Map `*.merge` extension to `Text Editor`:

		`Preferences` >> `General` >> `Editors` >> `File Associations` >> `Add...`

4. `Preferences` >> `General` >> `Workspace`:

	- Build automatically
	- Refresh on access

5. `Import...` >> `Maven` >> `Existing Maven Projects`:

	- Import your Maven projects.

6. `Build paths` >> `Use as Source Folder`:

	- `.../target/generated-sources/duel`

----

10 Optional Tools
-----------------

- Mou Markdown Editor

		http://mouapp.com

	All the documentation is written in Markdown. This simple editor can also export as HTML or PDF.

- BBEdit

		http://www.barebones.com/products/bbedit/

	A nice GUI editor and it has some command line hooks to launch it from scripts.
