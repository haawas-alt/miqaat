# Miqaat release rules. Shrink and optimise, but do not rename: crash logs users send us must stay readable.
-dontobfuscate
-keepattributes SourceFile,LineNumberTable,*Annotation*

# adhan is plain Java with no reflection; keep its public API to be safe (small library).
-keep class com.batoulapps.adhan.** { *; }

# Settings/enum names are persisted by name (Method.valueOf etc.).
-keepclassmembers enum com.usman.miqaat.data.** { *; }

# Raw audio resources are looked up by name at runtime (resources.getIdentifier) – see AzaanService.narrate.
# shrinkResources cannot see those; keep every raw resource.
