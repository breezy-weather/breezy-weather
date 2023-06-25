# Release management

1) Run tests and make release in local to check that everything looks good.
2) Update versionCode and versionName in `app/build.gradle`.
3) Write changelog in `fastlane/`.
4) Commit.
5) Tag version beginning with a `v` (example: `git tag v4.0.0-alpha -m "Version 4.0.0-alpha"`).
6) GitHub action will run and sign the release.
7) Update GitHub release notes draft and publish.