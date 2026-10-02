# AndroidX graphics-path native rebuild

The published graphics-path 1.1.0 AAR's native payload fails the release gate's 16 KB RELRO-end check.
The dependency is rebuilt from unmodified Apache-2.0 AndroidX native sources at commit
794e3806700833665f48f56f7dd3581642a6057f using NDK 28.2.13676358 and explicit maximum/common page size 16384.

tools/investigate_native_alignment.py verifies Git blob hashes for all source files and the published
AAR SHA-256 d031370d45ba4129a175fe9de913328a512b01a3c267e0eaf6b9b759f35842ae.
Only four jni/*/libandroidx.graphics.path.so entries are replaced. Every non-JNI archive entry
must remain byte-identical, including classes.jar, resources, manifest and consumer rules.
The build fails if LOAD alignment or the GNU_RELRO end boundary is invalid on any ABI.

An independent cloud source rebuild passed all four ABIs:
https://github.com/haawas-alt/miqaat/actions/runs/36981684990
This proves binary alignment, not app runtime correctness. Signed APK/AAB inspection and runtime
tests on an older Android release and a 16 KB system remain separate acceptance gates.

Build prerequisites: ANDROID_HOME and the Android SDK command-line tools. The task installs the pinned
NDK when absent. Source/rebuilt checksums are emitted under app/build/vendor/graphics-path.
Upstream license: https://github.com/androidx/androidx/blob/794e3806700833665f48f56f7dd3581642a6057f/LICENSE.txt
