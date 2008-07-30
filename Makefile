
# If you need to debug some classpath, includes, or command line arguments
# option, then comment out MAKEFLAGS line below, or set V=1 on the command
# line before make.
#
ifdef V
else
MAKEFLAGS=-s
endif

ifdef D
DEBUG=--debug=all
endif

.PHONY: all run test clean distclean pop debug

# --------------------------------------------------------------------
# Variable setup. You may want to set your editor to wrap to see the
# full CLASSPATH
# --------------------------------------------------------------------

-include .config

#
# if (and only if) GCJ native works then we build it; if you reach in directly
# and build a deep native target without GCJ being present, that's just silly
# and you deserve the breakage :).
#
ifdef GCJ
all: build/classes-dist build/native
else
all: build/classes-dist
endif

# [this  will be called by the above include if .config is missing.
# we don't call ./configure automatically to allow scope for
# manual configuration and overrides]
.config: src/accounts/client/ObjectiveAccounts.java
	echo
	echo "You need to run ./configure to check prerequisites"
	echo "and setup preferences before you can build accounts."
	( if [ ! -x configure ] ; then chmod +x configure ; echo "I just made it executable for you." ; fi )
	echo
	exit 1

# Variables we expect to be set in .config are:
#	JAVAGNOME_JARS
#	JUNIT_JARS
#	JNI_PATH
#	JAVAC[_CMD]	[expected to be 9 chars wide]
#	JAVA[_CMD]	[expected to be 9 chars wide]

# [This is just a quick sanity check]
build/config: build/dirs .config
	@echo "CHECK     build system configuration"
	( if [ ! "$(JAVAGNOME_JARS)" ] ; then echo "Sanity check failed. Run ./configure" ; exit 1 ; fi )
	touch $@

# [compose final classpath]
#CLASSPATH=/home/andrew/workspace/libgtk-java/gtk2.6.jar:$(JAVAGNOME_JARS)
CLASSPATH=$(JAVAGNOME_JARS):$(DB4O_JARS)
JNI_PATH=$(JAVAGNOME_LIBS)

SOURCES_DIST=$(shell find src -name '*.java')
SOURCES_LIBS=$(shell find lib -name '*.java')
SOURCES_TESTS=$(shell find tests -name '*.java')

# [we now go to the bother of listing the .class targets individually in order
# to allow us to use gcj, which doesn't compile all the things it needs to 
# (as javac does) even though it has to finds things by scanning. This
# can considerably slow a javac build depending on the order which classes
# are encountered; oh well]
CLASSES_DIST=$(shell echo $(SOURCES_DIST) | sed -e's/\.java/\.class/g' -e's/src/tmp\/classes/g')
CLASSES_LIBS=$(shell echo $(SOURCES_LIBS) | sed -e's/\.java/\.class/g' -e's/lib/tmp\/classes/g')
CLASSES_TESTS=$(shell echo $(SOURCES_TESTS) | sed -e's/\.java/\.class/g' -e's/tests/tmp\/classes/g')

# [same thing, but this time the individual .o targets. There's no native unti
# test wrapper; _TESTS just picks up the test harness sources]
#NATIVE_DIST=$(shell echo $(SOURCES_DIST) | sed -e's/\.java/\.o/g' -e's/src/tmp\/native/g')
#NATIVE_LIBS=$(shell echo $(SOURCES_LIBS) | sed -e's/\.java/\.o/g' -e's/lib/tmp\/native/g')
#NATIVE_TESTS=$(shell echo $(SOURCES_TESTS) | perl -p -e's/ /\n/g' | grep 'objective/ui' | sed -e's/\.java/\.o/g' -e's/tests/tmp\/native/g')

#
# convenience target: setup pre-reqs
#
build/dirs:
	@echo "MKDIR     preping temporary files and build directories"
	-test -d build || mkdir build
	-test -d tmp/classes || mkdir -p tmp/classes
	-test -d tmp/native || mkdir -p tmp/native
	touch $@

# [these are only necessary as a defence against the system having evolved
# since it was ./configured. Java is so bad at identifying the root cause 
# being missing files that were expected that such a safety check helps
# innocent builders maintain their sanity.]
build/check-jars:
	@echo "CHECK     prerequite core jar files"
	( if [ ! "$(CLASSPATH)" ] ; then echo "\"CLASSPATH\" variable is an empty. How did you get here?" ; exit 1 ; fi )
	( for i in `echo $(CLASSPATH) | sed -e's/:/ /g'` ; do if [ ! -f $$i ] ; then echo $$i not found. ; exit 1 ; fi ; done )
	touch $@

build/check-jars-tests:
	@echo "CHECK     prerequite unit test jar files"
	( if [ ! "$(JUNIT_JARS)" ] ; then echo "\"JUNIT_JARS\" variable is an empty. How did you get here?" ; exit 1 ; fi )
	( for i in `echo $(JUNIT_JARS) | sed -e's/:/ /g'` ; do if [ ! -f $$i ] ; then echo $$i not found. ; exit 1 ; fi ; done )
	@echo "MKDIR     unit test output directory"
	-test -d tmp/unittests || mkdir -p tmp/unittests
	touch $@

# --------------------------------------------------------------------
# Source compilation
# --------------------------------------------------------------------

# [anything Java JVM runtime should depend on this target]
build/classes: build/classes-libs build/classes-dist build/classes-tests

#build/native: build/native-libs build/native-dist objective
build/native:

#
# build the third party libraries whose code we ship.
#
build/classes-libs: build/config build/check-jars $(CLASSES_LIBS)
	touch $@

tmp/classes/%.class: lib/%.java
	@echo "$(JAVAC_CMD) $<"
	$(JAVAC) -d tmp/classes -classpath tmp/classes:$(CLASSPATH):lib $<


build/native-libs: build/config build/check-jars $(NATIVE_LIBS)
	touch $@

tmp/native/%.o: lib/%.java
	@echo "$(GCJ_CMD) $<"
	if [ ! -d `dirname $@` ] ; then mkdir -p `dirname $@` ; fi
	$(GCJ) -classpath $(CLASSPATH):lib -o $@ -c $<


#
# build the sources (that are part of the distributed app)
#
build/classes-dist: build/config build/check-jars build/classes-libs $(CLASSES_DIST)
	touch $@

tmp/classes/%.class: src/%.java
	@echo "$(JAVAC_CMD) $<"
	$(JAVAC) -d tmp/classes -classpath tmp/classes:$(CLASSPATH):lib:src $<


build/native-dist: build/config build/check-jars build/native-libs $(NATIVE_DIST)
	touch $@

tmp/native/%.o: src/%.java
	@echo "$(GCJ_CMD) $<"
	if [ ! -d `dirname $@` ] ; then mkdir -p `dirname $@` ; fi
	$(GCJ) -classpath $(CLASSPATH):lib:src -o $@ -c $<


#
# build the test sources
#
build/classes-tests: build/config build/classes-dist build/check-jars-tests $(CLASSES_TESTS)
	touch $@

tmp/classes/%.class: tests/%.java
	@echo "$(JAVAC_CMD) $<"
	$(JAVAC) -d tmp/classes -classpath tmp/classes:$(CLASSPATH):$(JUNIT_JARS):lib:src:tests $<

#
# Link executable
#
objective: build/native-libs build/native-dist
	@echo "$(GCJ_LINK_CMD) $@"
	$(GCJ) \
		-Wl,-rpath=$(GCJ_LIB_PATH) \
		-Wl,-rpath=$(JAVAGNOME_LIB_PATH) \
		-L$(GCJ_LIB_PATH) \
		-L$(JAVAGNOME_LIB_PATH) \
		-lgtkjava-2.6 -lgladejava-2.10 \
		-classpath $(CLASSPATH):lib:src \
		-fjni -O \
		--main=accounts.ui.ObjectiveAccounts -o objective \
		$(NATIVE_LIBS) $(NATIVE_DIST) $(NATIVE_TESTS)

#
# JavaDoc. We only build API documentation for dist classes, not unit tests.
#
ifdef V
JAVADOC=javadoc
WGET=wget
else
JAVADOC=javadoc -quiet
WGET=wget --quiet
REDIRECT=>/dev/null
endif

tmp/javadoc/classpath/package-list: build/dirs-javadoc
	-test -d $(@D) || mkdir -p $(@D)
	@echo "WGET      classpath package-list"
	$(WGET) http://developer.classpath.org/doc/package-list -O $@
	touch $@

tmp/javadoc/java-gnome/package-list: build/dirs-javadoc
	-test -d $(@D) || mkdir -p $(@D)
	@echo "WGET      java-gnome package-list"
	$(WGET) http://java-gnome.sourceforge.net/docs/javadoc/package-list -O $@
	touch $@

tmp/javadoc/db4o/package-list: build/dirs-javadoc
	-test -d $(@D) || mkdir -p $(@D)
	@echo "WGET      db4o package-list"
	$(WGET) http://www.db4o.com/community/ontheroad/apidocumentation/package-list -O $@
	touch $@

build/dirs-javadoc:
	@echo "MKDIR     preping javadoc output directories"
	-test -d tmp/javadoc || mkdir -p tmp/javadoc
	-test -d doc/api || mkdir -p doc/api
	touch $@

doc: build/javadoc
build/javadoc: build/classes-dist build/dirs-javadoc \
		tmp/javadoc/classpath/package-list \
		tmp/javadoc/java-gnome/package-list \
		tmp/javadoc/db4o/package-list
	@echo "JAVADOC   lib/generic src/*"
	$(JAVADOC) \
		-d doc/api \
		-classpath tmp/classes:$(JAVAGNOME_JARS) \
		-public \
		-nodeprecated \
		-source 1.4 \
		-notree \
		-noindex \
		-nohelp \
		-version \
		-author \
		-linkoffline http://developer.classpath.org/doc tmp/javadoc/classpath \
		-linkoffline http://java-gnome.sourceforge.net/docs/javadoc tmp/javadoc/java-gnome \
		-linkoffline http://www.db4o.com/community/ontheroad/apidocumentation tmp/javadoc/db4o \
		-sourcepath lib:src \
		-subpackages "generic:accounts:country" \
		-exclude "com.db4o:generic.junit" \
		-doctitle "<h1>ObjectiveAccounts</h1><p>An accounting program written for boutique consultancies, small businesses, and not-for-profit organizations</p>" \
		-windowtitle "objective version $(VERSION)" \
		-header "<span style=\"font-family: arial; font-size: small; font-style: normal; colour: gray;\">API documentation for <a class=\"black\" href="http://research.operationaldynamics.com/projects/objective/">ObjectiveAccounts</a>, an accounting program written for boutique consultancies, small businesses, and not-for-profit organizations running Linux and Open Source software.</span>" \
		-footer "<img src=\"http://www.operationaldynamics.com/images/logo/logo-60x76.jpg\" style=\"float:left; padding-left:5px; padding-right:10px;\"><img src=\"http://www.operationaldynamics.com/images/logo/type-342x32.jpg\" align=\"right\"><br><p style=\"font-family: arial; font-size: small; font-style: normal; colour: gray; clear: right;\">Copyright &copy; 2004-2006 <a class=\"black\" href=\"http://www.operationaldynamics.com/\">Operational Dynamics</a> Consulting Pty Ltd and others. This code is made available under the terms of the GPL, and patches are accepted. On the other hand, if you wish to see a specific feature developed, we would be happy to discuss your needs and prepare a quote.</p>" \
		-group "Generic and reusable code" "generic.*" \
		-group "Main accounting program and country specific implementations" "accounts.*:country.*" \
		-breakiterator $(REDIRECT)
	touch $@

#		$(SOURCES_LIBS) $(SOURCES_DIST) $(REDIRECT)


# --------------------------------------------------------------------
# Runtime convenience targets
# --------------------------------------------------------------------

test: build/unittests

# the point is to *run* these, so we don't touch a stamp file.

build/unittests: build/classes-tests
	@echo "$(JAVA_CMD) UnitTests"
	$(JAVA) \
		-Djava.library.path=$(JNI_PATH) \
		-classpath $(CLASSPATH):$(JUNIT_JARS):tmp/classes \
		generic.junit.VerboseTestRunner UnitTests



setup: build/classes-tests setup-rm build/demo-setup
setup-rm:
	@echo "RM        demo database"
	rm -f tmp/demo.yap
	rm -f build/demo-setup

# assume, without ordering, that build/classes-tests has been called. Actually
# depending on it means that this will almost always be called when developing
# in Eclipse.
build/demo-setup: tests/demo/client/DemoBooksSetup.java
	@echo "$(JAVA_CMD) DemoBooksSetup $(DEBUG)"
	$(JAVA) \
		-Djava.library.path=$(JNI_PATH) \
		-classpath $(CLASSPATH):tmp/classes \
		demo.client.DemoBooksSetup $(DEBUG)
	@echo "$(JAVA_CMD) DemoMockTransactions $(DEBUG)"
	$(JAVA) \
		-Djava.library.path=$(JNI_PATH) \
		-classpath $(CLASSPATH):tmp/classes \
		demo.client.DemoMockTransactions $(DEBUG)
	touch $@

dump: build/classes-tests build/demo-setup
	@echo "$(JAVA_CMD) DemoOutputDump $(DEBUG)"
	$(JAVA) \
		-Djava.library.path=$(JNI_PATH) \
		-classpath $(CLASSPATH):tmp/classes \
		-DCOLUMNS=`resize | perl -n -e'print if (s/COLUMNS=(\d*);/\1/)'` \
		demo.ui.DemoOutputDump $(DEBUG)

pop: build/classes-tests build/demo-setup
	@echo "$(JAVA_CMD) DemoWindowRunner $(DEBUG)"
	$(JAVA) \
		-Djava.library.path=$(JNI_PATH) \
		-classpath $(CLASSPATH):tmp/classes \
		demo.ui.DemoWindowRunner $(DEBUG)

run:
	@echo "target disabled... use \`make pop\`"
	@exit 1

#run: build/classes-dist
#	@echo "$(JAVA_CMD) ObjectiveAccounts $(DEBUG)"
#	$(JAVA) \
#		-Djava.library.path=$(JNI_PATH) \
#		-classpath $(CLASSPATH):tmp/classes \
#		accounts.ui.ObjectiveAccounts $(DEBUG)

# --------------------------------------------------------------------
# House keeping
# --------------------------------------------------------------------

# [note that we don't remove .config here, as a) darcs doesn't pick it up
# so if it's hanging around it won't cause problems, and b) if it is removed 
# here, then `make clean all` fails]
clean:
	@echo "RM        temporary build directories"
	-rm -rf build
	-rm -rf tmp
	-rm -rf hs_err_*
	-rm -f objective

distclean: clean
	@echo "RM        build configuration information"
	-rm -f .config .config.tmp
	@echo "RM        development artifacts"
	-rm -f share/*.gladep share/*.glade.bak share/*.gladep.bak
	@echo "RM        generated documentation"
	-rm -f doc/api/*

# --------------------------------------------------------------------
# Distribution target
# --------------------------------------------------------------------

# NOTE That this will make a tarball of *only* those sources which 
# have been recorded in Darcs - so if you've locally bumped the version, 
# but not committed that change, then the wrong version number will go out!
dist: distclean
	@echo "CHECK     pristine, fully recorded tree"
	darcs what -s | perl -n -e 'if (!/^No changes!/) { print "\nFailed: you can only run make dist from a\npristene tree or one with all changes recorded\n\n" ; exit 1 }'
	@echo "PREP      files for distribution"
	darcs dist --dist-name objective-$(VERSION) >/dev/null
	mv objective-$(VERSION).tar.gz /tmp
	cd /tmp && tar xzf objective-$(VERSION).tar.gz
	cd /tmp/objective-$(VERSION) && rm -r stash && chmod +x configure
	@echo "TAR       distribution tarball"
	tar cjf objective-$(VERSION).tar.bz2 -C /tmp objective-$(VERSION)
	@echo "RM        temporary files"
	rm -r /tmp/objective-$(VERSION)*

tarball: clean
	@echo "TAR       backup tarball"
	tar cjf objective-$(VERSION)-snapshot-`date +%y%m%d`.tar.bz2 --exclude '*.tar.*' .

