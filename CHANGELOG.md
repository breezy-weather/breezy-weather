# FIXing for Locationiq method

- Modify directly all nominatim related files to supprot LocationIQ API, no more sperate service
- IN the Settings api page, say Nominatim + LocationIQ. If user put link or blank, it will use nomiatim, but if user put key starting with `pk.nUMBER` it will use LocationIQ + nominatim
- Enhance nominatim itself if possible
- With the test script , we can add a huge list of Vietnam locations with test between Locationiq and Nominatim to see how the JSON is being returned to compare
- With the result before, it seem Regex is very a bad method to filter. But all we knows is that in the key `display_name`  of the JSON API being returned from the Locationiq, always consisting of Xã ABCED, Phường ABHSUE, Đặc Khu ABDHCY... and if we can filter exactly those strings, we will display the whole in the app, such as Xã ABCED ....
- We could need the strings "Tỉnh, Thành Phố" to determine the weather, as i am thinking this could cause weird errors for the weather if we just care about the adres, then the app don't know what the fuck is this area (remember only Location iq provide as good address, but most weather service don't)Logic might be implemented in the app, but see if we can do anything.
- The current bugs happening to nominatim is:
  Most of the location is correect, however the JSON returned sometimes miss the `Xã` or `Phường` and it returning random address of Village, Ponds, whatever. The chance for Xã areas to be broken is extremely high, while Phường is less broken.
  The main issue is that the logic handling of Nominatim is bad, because keys can be as vary, sometime they are in "district"(due to new administrative change of VN, nominatim is so confused), "city_district","county" etc. A `test.py` will help us know what it returns
- Bugs we tried: we tried to put the seperate Locationiq system, however, we still don't know why it act the same as Nominatim , regardless the returned JSON doesnt even contain wrong address (100% from Nominatim - i am expecting Wards also from Nominatim so Locationiq is not even working). Maybe it has to do with the Weather location determination logic in the app, but we need to check again.
- proposed method: we enhance directly the Nominatim because we dont lose anything at all.
  