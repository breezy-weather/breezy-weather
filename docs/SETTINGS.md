# Settings

## Debug

### Share crash logs

When tapping the “Share crash logs” button, a text file will be generated in the cache directory of the app. This file has a header containing useful information for developers followed by unmodified logs coming directly from the Android system. The cache directory belongs to the app and doesn't need any special permission to write in it on an unmodified Android system. Since it is a temporary file, make sure to make a copy if you need future access to the file.

Refer to the Android documentation if you need to know:
- how long the Android system keeps debug logs
- how Android retention works for app-specific cache files
- how Android protects app-specific cache files
