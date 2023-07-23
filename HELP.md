# Help / Frequently Asked Questions

## Background updates

### Background updates are not working

Certain manufacturers implement non-standard Android behaviors, which prevents the app from working properly.

The first thing to try is to whitelist Breezy Weather from battery optimization. From the app, go to Settings > Background updates and tap on “Disable battery optimization” (don’t worry, our background update job is optimized to be very battery-friendly, and you can change “Refresh rate” to “Never” at any time!).

If it still doesn’t work, you can find ways to circumvent aggressive manufacturer behaviors on this website: https://dontkillmyapp.com/


### I used Geometric Weather before, and the “persistent notification” method worked fine for me, can you bring it back?

If you don’t already have a widget, you can try adding one (you don’t need to have it on your main page). On some devices, this may help mimic the old “persistent notification” by avoiding app being killed for no reason, although the widget does not run anything like the old method, it just renders once and updates only on background updates or force refresh from the app.

Otherwise, this “persistent notification” method was based on a foreground service which was running every minute to check if there was something to do.

It is not battery-friendly at all. The worker method that we use just tells Android "we need something to run a task every 1 h 30, but if you are too busy to run it at that moment, you have a 10 minute margin to run it", so it’s much more efficient as Android takes care of running all jobs from all apps by itself at the moment it feels the most appropriate, instead of each app having their own foreground service.

If your manufacturer thinks it’s a good idea to not run scheduled workers but has no problem letting foreground services drain battery, then the problem is the manufacturer, not Breezy Weather, not you.

So we will not bring back/implement “persistent notification” for these reasons:
- it implies writing huge duplicate code (that was known to have duplicate run issues in Geometric Weather, btw) and maintaining it
- it is not battery-friendly

But more generally, we recommend that you follow steps from “Background updates are not working” section to find a workaround.
