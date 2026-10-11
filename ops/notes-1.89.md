**Testing pre-release, not a production release. Superseded by the 1.90 audit-fix pre-releases (latest: `audit-fix-1.90-8`).**

| | |
|---|---|
| App version | 1.89-mockup-fit (versionCode 1791176728) |
| Built from | commit `cfc5b57` (tag `v1.89-mockup-fit`) |
| APK SHA-256 | `734e121bf42d4f1e913068245da3fe43c5d64828144a3fe0f90347721ddc4ebb` |
| Signing certificate SHA-256 | `6285b269623127c3e2f0e87a5967f3fe143414c97371de3284d3489984541b01` (release key, the same as v1.83 and the 1.90 pre-releases) |

Corrections to the earlier notes (edited 11 Oct 2026):
- The tag name `audit-fixes-3895c823` is kept so existing download links keep working, but it points at an older commit (`3895c823`). The APK was built from `cfc5b57`; use tag `v1.89-mockup-fit` for the exact source.
- The APK is signed with the release key, not a debug key. It installs over v1.83 and the 1.90 pre-releases without uninstalling.

Testing status: unit tests, lint and static artifact checks passed. An independent emulator audit on 5 Oct 2026 rated this build 6.0/10 and found that a pre-prayer reminder could crash the app; that is fixed in 1.90. Imam and Urdu sign-off, real-device acceptance and Play Console steps are still pending.

The in-app updater is unchanged and does not offer this build.

Backups taken before the 1.90 fixes: tags `backup-1.89-audited-2d93744` and `backup-main-pre-audit-fixes-20261005`.
