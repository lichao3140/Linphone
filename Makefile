
archs=arm armv7 x86
TOPDIR=$(shell pwd)
LINPHONE_ANDROID_VERSION=$(shell git describe --always)
ANDROID_MOST_RECENT_TARGET=$(shell android list target -c | grep -E 'android-[0-9]+' | tail -n1)
ANT_SILENT=$(shell ant -h | grep -q -- -S && echo 1 || echo 0)
PACKAGE_NAME=$(shell sed -nE 's|<property name="linphone.package.name" value="(.*)" />|\1|p' custom_rules.xml)

.PHONY: all
.NOTPARALLEL: all generate-apk generate-mediastreamer2-apk install release

all: update-project generate-apk

build: $(addsuffix -build, $(archs))

clean: java-clean

install: install-apk run-linphone

java-clean:
	ant clean

$(TOPDIR)/res/raw/rootca.pem:
	cp liblinphone-sdk/android-arm/share/linphone/rootca.pem $@

copy-libs:
	rm -rf libs-debug/armeabi
	rm -rf libs/armeabi
	if test -d "liblinphone-sdk/android-arm"; then \
		mkdir -p libs-debug/armeabi && \
		cp -f liblinphone-sdk/android-arm/lib/libgnustl_shared.so libs-debug/armeabi && \
		cp -f liblinphone-sdk/android-arm/lib/lib*-armeabi.so libs-debug/armeabi && \
		cp -f liblinphone-sdk/android-arm/lib/mediastreamer/plugins/*.so libs-debug/armeabi && \
		mkdir -p libs/armeabi && \
		cp -f liblinphone-sdk/android-arm/lib/libgnustl_shared.so libs/armeabi && \
		cp -f liblinphone-sdk/android-arm/lib/lib*-armeabi.so libs/armeabi && \
		cp -f liblinphone-sdk/android-arm/lib/mediastreamer/plugins/*.so libs/armeabi && \
		sh WORK/android-arm/strip.sh libs/armeabi/*.so; \
	fi
	if test -f "liblinphone-sdk/android-arm/bin/gdbserver"; then \
		cp -f liblinphone-sdk/android-arm/bin/gdbserver libs-debug/armeabi && \
		cp -f liblinphone-sdk/android-arm/bin/gdb.setup libs-debug/armeabi && \
		cp -f liblinphone-sdk/android-arm/bin/gdbserver libs/armeabi && \
		cp -f liblinphone-sdk/android-arm/bin/gdb.setup libs/armeabi; \
	fi
	rm -rf libs-debug/armeabi-v7a
	rm -rf libs/armeabi-v7a
	if test -d "liblinphone-sdk/android-armv7"; then \
		mkdir -p libs-debug/armeabi-v7a && \
		cp -f liblinphone-sdk/android-armv7/lib/libgnustl_shared.so libs-debug/armeabi-v7a && \
		cp -f liblinphone-sdk/android-armv7/lib/lib*-armeabi-v7a.so libs-debug/armeabi-v7a && \
		cp -f liblinphone-sdk/android-armv7/lib/mediastreamer/plugins/*.so libs-debug/armeabi-v7a && \
		mkdir -p libs/armeabi-v7a && \
		cp -f liblinphone-sdk/android-armv7/lib/libgnustl_shared.so libs/armeabi-v7a && \
		cp -f liblinphone-sdk/android-armv7/lib/lib*-armeabi-v7a.so libs/armeabi-v7a && \
		cp -f liblinphone-sdk/android-armv7/lib/mediastreamer/plugins/*.so libs/armeabi-v7a && \
		sh WORK/android-armv7/strip.sh libs/armeabi-v7a/*.so; \
	fi
	if test -f "liblinphone-sdk/android-armv7/bin/gdbserver"; then \
		cp -f liblinphone-sdk/android-armv7/bin/gdbserver libs-debug/armeabi-v7a && \
		cp -f liblinphone-sdk/android-armv7/bin/gdb.setup libs-debug/armeabi-v7a && \
		cp -f liblinphone-sdk/android-armv7/bin/gdbserver libs/armeabi-v7a && \
		cp -f liblinphone-sdk/android-armv7/bin/gdb.setup libs/armeabi-v7a; \
	fi
	rm -rf libs-debug/x86
	rm -rf libs/x86
	if test -d "liblinphone-sdk/android-x86"; then \
		mkdir -p libs-debug/x86 && \
		cp -f liblinphone-sdk/android-x86/lib/libgnustl_shared.so libs-debug/x86 && \
		cp -f liblinphone-sdk/android-x86/lib/lib*-x86.so libs-debug/x86 && \
		cp -f liblinphone-sdk/android-x86/lib/mediastreamer/plugins/*.so libs-debug/x86 && \
		mkdir -p libs/x86 && \
		cp -f liblinphone-sdk/android-x86/lib/libgnustl_shared.so libs/x86 && \
		cp -f liblinphone-sdk/android-x86/lib/lib*-x86.so libs/x86 && \
		cp -f liblinphone-sdk/android-x86/lib/mediastreamer/plugins/*.so libs/x86 && \
		sh WORK/android-x86/strip.sh libs/x86/*.so; \
	fi
	if test -f "liblinphone-sdk/android-x86/bin/gdbserver"; then \
		cp -f liblinphone-sdk/android-x86/bin/gdbserver libs-debug/x86 && \
		cp -f liblinphone-sdk/android-x86/bin/gdb.setup libs-debug/x86 && \
		cp -f liblinphone-sdk/android-x86/bin/gdbserver libs/x86 && \
		cp -f liblinphone-sdk/android-x86/bin/gdb.setup libs/x86; \
	fi

update-project:
	android update project --path . --target $(ANDROID_MOST_RECENT_TARGET)
	android update test-project --path tests -m .

update-mediastreamer2-project:
	@cd $(TOPDIR)/submodules/linphone/mediastreamer2/java && \
	android update project --path . --target $(ANDROID_MOST_RECENT_TARGET)

generate-apk: java-clean build copy-libs $(TOPDIR)/res/raw/rootca.pem update-project
	echo "version.name=$(LINPHONE_ANDROID_VERSION)" > default.properties && \
	ant debug

generate-mediastreamer2-apk: java-clean build copy-libs update-mediastreamer2-project
	@cd $(TOPDIR)/submodules/linphone/mediastreamer2/java && \
	echo "version.name=$(LINPHONE_ANDROID_VERSION)" > default.properties && \
	ant debug

quick:
	ant clean
	ant debug
	ant installd
	ant run

install-apk:
	ant installd

uninstall:
	adb uninstall $(PACKAGE_NAME)

release: java-clean build copy-libs update-project
	patch -p1 < release.patch
	cat ant.properties | grep version.name > default.properties
	ant release
	patch -Rp1 < release.patch

generate-sdk: liblinphone-android-sdk

liblinphone-android-sdk: generate-apk
	ant liblinphone-android-sdk

linphone-android-sdk: generate-apk
	ant linphone-android-sdk

mediastreamer2-sdk: generate-mediastreamer2-apk
	@cd $(TOPDIR)/submodules/linphone/mediastreamer2/java && \
	ant mediastreamer2-sdk

liblinphone_tester:
	$(MAKE) -C liblinphone_tester

run-linphone:
	ant run

run-liblinphone-tests:
	$(MAKE) -C liblinphone_tester run-all-tests

run-basic-tests: update-project
	ant partial-clean
	$(MAKE) -C tests run-basic-tests ANT_SILENT=$(ANT_SILENT)

run-all-tests: update-project
	ant partial-clean
	$(MAKE) -C tests run-all-tests ANT_SILENT=$(ANT_SILENT)

pull-transifex:
	tx pull -af

push-transifex:
	tx push -s -f --no-interactive


arm: arm-build

arm-build:
	$(MAKE) -C WORK/android-arm/cmake
	@echo "Done"

armv7: armv7-build

armv7-build:
	$(MAKE) -C WORK/android-armv7/cmake
	@echo "Done"

x86: x86-build

x86-build:
	$(MAKE) -C WORK/android-x86/cmake
	@echo "Done"


help-prepare-options:
	@echo "prepare.py was previously executed with the following options:"
	@echo "   ./prepare.py"

help: help-prepare-options
	@echo ""
	@echo "(please read the README.md file first)"
	@echo ""
	@echo "Available architectures: arm armv7 x86"
	@echo ""
	@echo "Available targets:"
	@echo ""
	@echo "   * all or generate-apk: builds all architectures and creates the linphone application APK"
	@echo "   * generate-sdk: builds all architectures and creates the liblinphone SDK"
	@echo "   * install: install the linphone application APK (run this only after generate-apk)"
	@echo "   * uninstall: uninstall the linphone application"
	@echo ""
