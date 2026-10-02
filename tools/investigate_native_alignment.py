#!/usr/bin/env python3
"""Rebuild pinned AndroidX native code for investigation; never publish or patch an ELF."""
import hashlib, json, os, pathlib, subprocess, urllib.request
ROOT = pathlib.Path(os.environ.get("MIQAAT_GRAPHICS_BUILD_DIR", "native-investigation"))
SOURCE = ROOT / "source"
SOURCE.mkdir(parents=True, exist_ok=True)
UPSTREAM = "794e3806700833665f48f56f7dd3581642a6057f"
FILES = {
  "Conic.cpp": "e22e2b2c0643d377f21bc92a18458867618296a2",
  "Conic.h": "2bd34419cd410e0c38391ecf580e068f0fc59191",
  "Path.h": "f25d708782be3f13fb77fef6098fdd26e57b10b6",
  "PathIterator.cpp": "cab9c5d1eb14317ee973cf6198a452393fc94fca",
  "PathIterator.h": "414dbdd1d62f4a50fa30d49143edad60ea6d959e",
  "libandroidx.graphics.path.map": "d1310e52849b7f49928acbd74384c3c7d507a602",
  "pathway.cpp": "d3a7d75a447274e8941f327a50593493f495115c",
  "scalar.h": "0342d138393b2bc579e5055fd3ee09e80a6f390d",
  "math/TVecHelpers.h": "ef7e182d804f20d3ecb3b3f5c69409b335ee8d57",
  "math/compiler.h": "d6e18aaae05a3be3e4fe766b38bb1c3046240b40",
  "math/vec2.h": "ae67a2563332513d7b3fe071494d004dc39ae1af"
}
for name, expected in FILES.items():
    url = f"https://raw.githubusercontent.com/androidx/androidx/{UPSTREAM}/graphics/graphics-path/src/main/cpp/{name}"
    with urllib.request.urlopen(url, timeout=60) as response:
        data = response.read()
    digest = hashlib.sha1(b"blob " + str(len(data)).encode() + b"\0" + data).hexdigest()
    if digest != expected:
        raise RuntimeError(f"Upstream source integrity mismatch: {name}")
    path = SOURCE / name
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(data)
(SOURCE / "CMakeLists.txt").write_text("""cmake_minimum_required(VERSION 3.22.1)
project(miqaat_graphics_path_alignment LANGUAGES CXX)
add_library(androidx.graphics.path SHARED Conic.cpp PathIterator.cpp pathway.cpp)
target_compile_features(androidx.graphics.path PRIVATE cxx_std_17)
target_compile_options(androidx.graphics.path PRIVATE -O3 -fno-exceptions -fno-rtti -fvisibility=hidden -fvisibility-inlines-hidden)
target_link_options(androidx.graphics.path PRIVATE "-Wl,--version-script=${CMAKE_CURRENT_SOURCE_DIR}/libandroidx.graphics.path.map" "-Wl,-z,max-page-size=16384" "-Wl,-z,common-page-size=16384")
target_link_libraries(androidx.graphics.path PRIVATE android m)
""")
sdk = pathlib.Path(os.environ["ANDROID_HOME"])
ndk = sdk / "ndk" / "28.2.13676358"
if not ndk.exists():
    managers = sorted((sdk / "cmdline-tools").glob("*/bin/sdkmanager"))
    if not managers:
        raise RuntimeError("Install Android NDK 28.2.13676358 with SDK Manager")
    subprocess.run([str(managers[-1]), "ndk;28.2.13676358"], check=True)
readelf = ndk / "toolchains/llvm/prebuilt/linux-x86_64/bin/llvm-readelf"
results = []
for abi in ("arm64-v8a", "armeabi-v7a", "x86", "x86_64"):
    build = ROOT / abi
    subprocess.run(["cmake", "-S", str(SOURCE), "-B", str(build),
        f"-DCMAKE_TOOLCHAIN_FILE={ndk}/build/cmake/android.toolchain.cmake",
        f"-DANDROID_ABI={abi}", "-DANDROID_PLATFORM=android-26",
        "-DANDROID_STL=c++_static", "-DCMAKE_BUILD_TYPE=Release"], check=True)
    subprocess.run(["cmake", "--build", str(build), "--parallel", "2"], check=True)
    library = build / "libandroidx.graphics.path.so"
    output = subprocess.check_output([str(readelf), "-lW", str(library)], text=True)
    (build / "program-headers.txt").write_text(output)
    headers = [line.split() for line in output.splitlines() if line.strip().startswith(("LOAD ", "GNU_RELRO "))]
    loads = [h for h in headers if h[0] == "LOAD"]
    relros = [h for h in headers if h[0] == "GNU_RELRO"]
    assert loads and relros, f"{abi}: required ELF headers absent"
    assert all(int(h[-1], 16) >= 16384 for h in loads), f"{abi}: LOAD alignment"
    assert all((int(h[2], 16) + int(h[5], 16)) % 16384 == 0 for h in relros), f"{abi}: RELRO boundary"
    results.append({"abi": abi, "sha256": hashlib.sha256(library.read_bytes()).hexdigest(), "load_and_relro_16kb": True})
(ROOT / "RESULTS.json").write_text(json.dumps({"upstream": UPSTREAM, "ndk": "28.2.13676358", "libraries": results, "status": "Experimental rebuild only; not integrated, device-tested or released"}, indent=2))

# Preserve the published managed API, manifest, resources and consumer rules exactly.
# Only the four native libraries change, by compiling pinned unmodified source.
import io, zipfile
with urllib.request.urlopen("https://dl.google.com/dl/android/maven2/androidx/graphics/graphics-path/1.1.0/graphics-path-1.1.0.aar", timeout=60) as response:
    original = response.read()
original_hash = hashlib.sha256(original).hexdigest()
assert original_hash == "d031370d45ba4129a175fe9de913328a512b01a3c267e0eaf6b9b759f35842ae", "Published graphics-path AAR integrity mismatch"
out = ROOT / "graphics-path-1.1.0-miqaat-16kb.aar"
replaced = []
with zipfile.ZipFile(io.BytesIO(original)) as source, zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED) as dest:
    for item in source.infolist():
        data = source.read(item.filename)
        if item.filename.startswith("jni/") and item.filename.endswith("/libandroidx.graphics.path.so"):
            abi = item.filename.split("/")[1]
            data = (ROOT / abi / "libandroidx.graphics.path.so").read_bytes()
            replaced.append(abi)
        dest.writestr(item, data)
assert set(replaced) == {"arm64-v8a", "armeabi-v7a", "x86", "x86_64"}
with zipfile.ZipFile(io.BytesIO(original)) as source, zipfile.ZipFile(out) as dest:
    assert set(source.namelist()) == set(dest.namelist())
    for name in source.namelist():
        if not name.startswith("jni/"):
            assert source.read(name) == dest.read(name), f"Managed artifact changed: {name}"
(ROOT / "AAR-PROVENANCE.json").write_text(json.dumps({
    "original_maven_coordinate": "androidx.graphics:graphics-path:1.1.0",
    "original_sha256": original_hash,
    "rebuilt_sha256": hashlib.sha256(out.read_bytes()).hexdigest(),
    "upstream_source": UPSTREAM,
    "managed_entries_identical": True,
    "changed_native_abis": replaced
}, indent=2))
