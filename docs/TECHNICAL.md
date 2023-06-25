# API keys

GitHub releases contain default API keys of the project that make all weather providers work by default (until API limits are reached).

If you want to self-build, you will need to add your own API keys in `local.properties` if you want the same behaviour:
```properties
breezy.accu.key=myapikey
breezy.baiduip.key=myapikey
breezy.atmoaura.key=myapikey
breezy.mf.jwtKey=myapikey
breezy.mf.key=myapikey
breezy.openweather.key=myapikey
breezy.openweather.oneCallVersion=3.0
```

If you don’t, it will still work (for example, Open-Meteo will work), but other providers won’t work by default and user will need to input API key in settings.


# Release management

*Instructions for members of the organization.*

1) Run tests and make release in local to check that everything looks good.
2) Update versionCode and versionName in `app/build.gradle`.
3) Write changelog in `fastlane/`.
4) Commit.
5) Tag version beginning with a `v` (example: `git tag v4.0.0-alpha -m "Version 4.0.0-alpha"`).
6) GitHub action will run and sign the release.
7) Update GitHub release notes draft and publish.