# Breezy weather fixed vietnamese address

## Use case
- the app is finetuned for new administrative vietnamese address starting on july 1 2025, and since nominatim return wrong addresses and it take a very long time to fix, we can ultilize this quick patch. 

## Method

- we use LocationIQ, you provide the API key into the Setting page, start with `pk.abcdefghijk` 
- The requests from LocationIQ even though it is paid, it is INSANELY generous enough for a  weather application. (5k requests/DAY , even MONTH is still generous!)

## what bug
- Nominatim return weirdass datas, some wrong address, while the correct address is buried in some other keys. 

## Implement fix:
- LocationIQ's JSON contains guranteed commune/ward names on the `display_name` section. By REGEX, we can grab what we need and properly display. 
