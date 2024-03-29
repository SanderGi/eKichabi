# Logs
Specifications for the USSD and Android log formats including tools for parsing/generating them. All logs are generated by 📜ekichabi/helper/Logs.py and stored in 📂logs/.

## USSD Specs
Ussd logs are stored in 📂logs/ and follow the filename glob pattern `./logs/*_*-*-*-*.*.*.log` (`[phone_number]_[Year]-[month]-[day]-[Hour].[Minute].[Second]` where the time stamp is the creation/start time of the session). A new log is created for every session. A session lasts until the user ends it (using the exit key -- 109) or once it times out after 3 minutes without user input.

### Example
Inputs: 1, 1, 1, 1, 1, 1, 0, 100, 3, 3, 1, ndi, 2, 1, 1, 1, 109
```
session_ID: 77978744564 from 255000000000 started 11/29/2022, 08:38:35
[Time from Start]	Action Type
[0:00:00.124257]	RENDERED SCREEN		HomeScreen
[0:00:02.858145]	MENU ITEM		Tafuta kwa kuchagua sekta
[0:00:02.858494]	INPUT RECEIVED		'1'
[0:00:02.858635]	RENDERED SCREEN		MenuHierarchyScreen - selecting a category
[0:00:04.880248]	MENU ITEM		Huduma
[0:00:04.880547]	INPUT RECEIVED		'1'
[0:00:04.880655]	RENDERED SCREEN		MenuHierarchyScreen - selecting a subsector1
[0:00:05.849723]	MENU ITEM		Mashine ya kusaga mahindi
[0:00:05.850116]	INPUT RECEIVED		'1'
[0:00:05.850251]	RENDERED SCREEN		MenuHierarchyScreen - selecting a district
[0:00:07.003338]	MENU ITEM		Bukoba
[0:00:07.003751]	INPUT RECEIVED		'1'
[0:00:07.003900]	RENDERED SCREEN		MenuHierarchyScreen - selecting a village
[0:00:08.282474]	MENU ITEM		Biashara Zote (18), au Chagua Kijiji:
[0:00:08.283007]	INPUT RECEIVED		'1'
[0:00:08.283179]	RENDERED SCREEN		MenuHierarchyScreen - selecting a business
[0:00:09.524373]	MENU ITEM		SECRET
[0:00:09.524819]	INPUT RECEIVED		'1'
[0:00:09.524979]	RENDERED SCREEN		BusinessDetailsScreen - for SECRET
[0:00:12.303110]	INPUT RECEIVED		'0'
[0:00:12.303568]	RENDERED SCREEN		BusinessDetailsScreen - for SECRET
[0:00:16.103951]	HOME PRESSED
[0:00:16.104202]	RENDERED SCREEN		HomeScreen
[0:00:18.269535]	MENU ITEM		Tafuta kwa kuandika
[0:00:18.269928]	INPUT RECEIVED		'3'
[0:00:18.270056]	RENDERED SCREEN		SearchSelectorScreen
[0:00:21.481230]	MENU ITEM		Bidhaa/Huduma
[0:00:21.481674]	INPUT RECEIVED		'3'
[0:00:21.481823]	RENDERED SCREEN		SearchProductScreen
[0:00:23.305696]	MENU ITEM		Mazao
[0:00:23.306006]	INPUT RECEIVED		'1'
[0:00:23.306170]	RENDERED SCREEN		FirstCharsScreen - searching for Crop
[0:00:26.315628]	INPUT RECEIVED		'ndi'
[0:00:26.315963]	RENDERED SCREEN		KeywordSelectScreen
[0:00:29.694585]	MENU ITEM		Ndizi
[0:00:31.144138]	INPUT RECEIVED		'2'
[0:00:31.144415]	RENDERED SCREEN		IfFilterBYLocationScreen
[0:00:39.353111]	MENU ITEM		Ndiyo
[0:00:39.353361]	INPUT RECEIVED		'1'
[0:00:39.353773]	RENDERED SCREEN		MenuHierarchyScreen - selecting a district
[0:00:46.466565]	MENU ITEM		Biashara Zote (77), au Chagua Wilaya:
[0:00:46.466827]	INPUT RECEIVED		'1'
[0:00:46.467286]	RENDERED SCREEN		MenuHierarchyScreen - selecting a business
[0:00:59.370618]	MENU ITEM		SECRET
[0:00:59.370881]	INPUT RECEIVED		'1'
[0:00:59.371265]	RENDERED SCREEN		BusinessDetailsScreen - for SECRET
[0:01:26.951186]	EXIT PRESSED
[0:01:26.951600]	RENDERED SCREEN		Kwaheri!
```

### Format
1. Top line includes the session id, phone number and creation/start date of the session
2. Second line is a header
3. The rest of the lines consist of a time since session start (ISO 8601), an action type, and some details (separated by tab characters)
    - The first one is always the homescreen
    - They come in groups (these are the only possible combinations unless an unhandled exception occured):
        - INPUT RECIEVED + RENDERED SCREEN (for search by input)
        - MENU ITEM + INPUT RECIEVED + RENDERED SCREEN (for menu screens)
        - BACK PRESSED + RENDERED SCREEN (back button -- 99)
        - HOME PRESSED + RENDERED SCREEN (home button -- 100)
        - EXIT PRESSED + RENDERED SCREEN (session end button -- 109)
    - Details for each action type is as follows:
        - INPUT RECIEVED - the input the user sent
        - MENU ITEM - the label for the menu item selected
        - RENDERED SCREEN - the string returned by the current screen class's `note()` function
        - Everything else has no details
4. The end is either an EXIT PRESSED + RENDERED SCREEN		Kwaheri! if the user actively exited or another valid action type group if the session timed out.

### Code for generation/parsing
📜scripts/parseLogs.py contains code to average the session times and action times

📜scripts/ussdLog.py contains code to calculate various metrics on the logs

📜tests/performance/perf.mjs contains code to simulate random actions

## Android Specs
Android logs are stored in 📂logs/ and follow the `./logs/Android_*.log` filename glob pattern (asterisk is the phone number associated with the user that owns this log file -- guaranteed to be whitelisted since non-whitelisted tracking requests are rejected). Android logs are stored for unknown amounts of time on the Android devices in a compact custom binary format (up to 100KiB -- aka 6250-20000 actions) before being uploaded at 15 minute intervals to the server when the device has connection. There's a strict one-phonenumber-one-file policy on the server and new logged actions are simply appended to the existing files.

### Example serverside log file
Encoded file `Android_255000000000.log` (binary nonsense that doesn't render in Gitlab's MarkDown preview)

Decoded file `Android_255000000000.log_decoded`:
```
Actions decoded: 2022/11/29-09.24.29
CONTACT | 2022/11/28 | Business pk: 8101
FILTER | 2022/11/28 | SUCCESSFUL | TRUNCATED | CLEANED | biasharawenyeujuzi
FILTER | 2022/11/28 | UNSUCCESSFUL | TRUNCATED | CLEANED | usindikajimazao
FILTER | 2022/11/28 | SUCCESSFUL | TRUNCATED | CLEANED | biasharawenyeujuzi
CONTACT | 2022/11/28 | Business pk: 1431
CALL | 2022/11/28 | Business pk: 5110
FILTER | 2022/11/28 | SUCCESSFUL | TRUNCATED | CLEANED | biasharanakuuzajumla
FAVORITE | 2022/11/28 | Business pk: 6071
UNFAVORITE | 2022/11/28 | Business pk: 2608
SEARCH | 2022/11/28 | UNSUCCESSFUL | TRUNCATED | CLEANED | hhsvjwdvligijfufdlkih
SEARCH | 2022/11/28 | UNSUCCESSFUL | TRUNCATED | RAW | weweugkwspvkuydsbiayx
FILTER | 2022/11/28 | UNSUCCESSFUL | TRUNCATED | CLEANED | bukoba
UNFAVORITE | 2022/11/28 | Business pk: 1746
UNFAVORITE | 2022/11/28 | Business pk: 6352
CALL | 2022/11/28 | Business pk: 3405
CONTACT | 2022/11/28 | Business pk: 6966
SEARCH | 2022/11/28 | UNSUCCESSFUL | TRUNCATED | RAW | iymbyojuaaytrkytnspoj
FILTER | 2022/11/28 | UNSUCCESSFUL | TRUNCATED | CLEANED | kyerwa
SEARCH | 2022/11/28 | SUCCESSFUL | TRUNCATED | RAW | zhrddsmdnoailiy
CALL | 2022/11/28 | Business pk: 1572
```

### Decoded format
1. Each decoded file starts with a timestamp (`YYYY/MM/DD-HH.MM.SS`) indicating when the file was last decoded
2. Each line hereafter represents an action (the user did something). Actions are as follows:
    - `FILTER | [TIMESTAMP] | [STATUS] | [TRUNCATION] | [CLEANNESS] | [FILTER-STR]`
    User searches for a business by selecting filters. Logged each time the filter changes.
    - `SEARCH | [TIMESTAMP] | [STATUS] | [TRUNCATION] | [CLEANNESS] | [SEARCH-STR]`
    User searches for a business by typing in a search word. Only logged when the user interacts with a business (any other action) or leaves the app/search-screen.
    - `CONTACT | [TIMESTAMP] | [PK]`
    User clicked 'add to contact' button on a business.
    - `OPEN BUSINESS SCREEN | [TIMESTAMP] | [PK]`
    User clicked the business to reveal the more detailed business popup.
    - `CALL | [TIMESTAMP] | [PK]`
    User clicked the call button on a business.
    - `UNFAVORITE | [TIMESTAMP] | [PK]`
    User hit the star again or clicked the unfavorite button.
    - `FAVORITE | [TIMESTAMP] | [PK]`
    User hit the star or clicked the favorite button on a business.
    - `UNRECOGNIZED ACTION | [BITS]`
    This is printed when an unhandled error occured during decoding and followed by the rest of the bits in the encoded file for inspection. Should not occur.

Key:
- `[PK]` - Primary Key of a business i.e. the row number in the census_data csv
- `[TIMESTAMP]` - YYYY/MM/DD of the performed action
- `[TRUNCATION]` - Whether or not the [SEARCH-STR] or [FILTER-STR] was truncated. Reads `TRUNCATED` if the string was longer than 21 characters and cut down to 21 characters or `UNTRUNCATED` if that was not the case. Note: due to a comparison operator being the wrong way in the Android code, this currently always prints true.
- `[STATUS]` - Whether a search or filter action was `SUCCESSFUL` (user ended up interacting with a business) or `UNSUCCESFUL` (user quit the app or decided to do something else).
- `[CLEANNESS]` - Whether the string was cleaned (non-alphabetic and non-lowercase characters removed/lowercased) since the encoding only supports alphabetic lowercase characters. `RAW` if no cleaning was necessary, `CLEANED` otherwise.
- `[SEARCH-STR]` - The string the user types into the search box
- `[FILTER-STR]` - The filter (village, district, sector, subsector, etc.) that the user selects

### Encoded format
Don't need to worry about this since the logs can be decoded by running `python manage.py decode_android_logs ./logs/Android_*.log`. If it becomes necessary to understand the encoded format, it is recommended to take a look at the encoder (📜resources/AndroidLogger.java) and decoder (📜ekichabi/services/android.utils.py) as well as the spec discussion (SECRET) for specific implementation details. But here is the high level specification:

All actions are appended to one binary file and each action consists of the following bits:
- 3 bits indicating the action type (Favoriting, Un-favoriting, Calling, Opening Business Screen, Adding to Contacts, Searchstr, Filter)
- 12 bits count of days since 2022/1/1 (good for 11 years)
- 16-108 bits for the data which is one of:
    - 16 bits indicating the business pk in the android json (for Favoriting, Un-favoriting, Calling, Opening Business Screen, Adding to Contacts)
    - 3 + 5n bits for the search str (up to 108 bits)
        - 1 bit indicating if the search was successful (user called/favorited/etc a business)
        - 1 bit indicating if some letters were truncated because the search str was too long (more than 21 characters)
        - 1 bit indicating if non-alphabetical/non-lowercase letters were removed from the search-str
        - 5 bits per letter in the searchstr (only including lowercase alphabetic chars)
    - 3 + 5n bits for the filter history (up to 108 bits)
        - same as search but with a filter str instead of a search string
- 8 bits of all zeros to indicate the end of the action
- 0-7 Filler bits to make each action line up with an integer number of bytes

#### Features and properties
- Simplest proposed spec that yields sufficient efficiency (6-20K actions in 100KiB as opposed to the 200-3225 actions with plain text)
- Streamable and can be parsed in one loop through with no need to keep previous bits in memory
- Appends easily to a binary file with no need to read the file beforehand
- Redundancy in case data gets corrupted, we messed up the coding, or we decide to change the spec in the future
- Easily extendable to a more efficient but complicated format in the future
- Limits the number of bytes an action can have to avoid a few actions using all the available storage space

### Code for parsing/generation
📜resources/AndroidLogger.java contains a copy of the AndroidLogger class used by the Android app with an extra `main` function to generate random example logs. The encoded version is automatically put in 📜resources/log.bin.

📜ekichabi/management/commands/decode_android_logs.py contains code for decoding encoded logs. Run `python manage.py decode_android_logs ./logs/Android_*.log` to decode all the Android logs in 📂logs/ or give a different path pattern to decode logs in another directory. 

### Efficiency notes
Alex ran simulations with millions of random actions and confirmed that the binary format is more compact than just zipping the actions directly. We could gain an extra 20KiB or so by further compressing the binary logs (e.g. with zip) but then the format isn't streamable/appendable.
