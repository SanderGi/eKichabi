import base64
import csv
import json
import os
from datetime import datetime

from django.conf import settings
from django.shortcuts import HttpResponse
from django.utils.decorators import method_decorator
from django.views.decorators.csrf import csrf_exempt

from ekichabi.helper.Logs import android_make_line
from ekichabi.services.android.utils import decode_Base64_actions
from ekichabi.whitelist.WhitelistManager import check_whitelisted
from ekichabi.helper.Utils import CSVData
from ekichabi.whitelist.ABGroupManager import get_or_set_test_group

# ======== Handle loading business data directly from csv in the format that the android app expects, only happens once when the server is initialized ======


def AndroidData(anonymous=False):
    rows = CSVData.getReader(anonymous)

    d = []
    for ix, row in enumerate(rows):
        d.append(
            {
                "model": "andoridData.business",
                "pk": ix + 1,
                "fields": {
                    "name": row[CSVData.NAME],
                    "category": row[CSVData.CATEGORY],
                    "district": row[CSVData.DISTRICT],
                    "village": row[CSVData.VILLAGE],
                    "subvillage": row[CSVData.SUBVILLAGE],
                    "number1": row[CSVData.NUMBER1],
                    "number2": row[CSVData.NUMBER2],
                    "subsector1_sw": row[CSVData.SUBSECTOR1],
                    "subsector2_sw": row[CSVData.SUBSECTOR2],
                    "subsector1_en": row[CSVData.SUBSECTOR1_EN],
                    "subsector2_en": row[CSVData.SUBSECTOR2_EN],
                    "owner": row[CSVData.OWNER],
                },
            }
        )
    return json.dumps(d)


data = AndroidData()
anon_data = AndroidData(anonymous=True)


@method_decorator(csrf_exempt)
def businessView(request):
    if request.method == "POST":
        username = request.POST.get("username")
        password = request.POST.get("password")
        phone_number = request.POST.get("number") or "000000000"
        if username == "SECRET" and password == "SECRET":
            b = (
                anon_data if get_or_set_test_group(phone_number) == "B" else data
            )  # android data needs a certain format. we could use the android models and reset_db, but I'm lazy lol
            return HttpResponse(b)
        else:
            return HttpResponse("Invalid user " + str(username))
    else:
        return HttpResponse("Invalid request")


# test: fetch('http://127.0.0.1:8000/business/', { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body:'username=SECRET&password=SECRET&number=SECRET' }).then(res => res.json().then(data => console.log(data)))

# ===========================================================================================================================================================
# ======= Handle getting the modify date of data so Android app only needs to get data if it changed ========================================================

# [Hans,10/6] retrieves the last modification of the file (should work with either updating or
# completely replacing it with a new file), see https://docs.python.org/3/library/os.path.html#os.path.getmtime
# [Alex, 10/10] Converted timestamp format to the YYYY/MM/DD format that used to be here.


def dateView(request):
    dirpath = os.path.dirname(os.path.abspath(__file__))
    # [TODO] path or file name might need to change in PA
    datapath = getattr(settings, "DATA_PATH", "./data/census_data_trimmed.csv")
    return HttpResponse(
        datetime.fromtimestamp(
            os.path.getmtime(os.path.join(dirpath, datapath))
        ).strftime("%Y/%m/%d")
    )


# ===========================================================================================================================================================
# ======= Check the whitelist ===============================================================================================================================


def permissionView(request):
    if request.method == "GET":
        phone_num = request.GET.get("phone_num")
        try:
            # if phone number is whitelisted, grant access
            if check_whitelisted(phone_num):
                return HttpResponse(content="access granted", status=200)
            # otherwise if it's not whitelisted, do not grant access
            else:
                return HttpResponse(content="access denied", status=403)
        except ValueError:
            # bad input
            return HttpResponse(content="access denied", status=400)


# ===========================================================================================================================================================
# ======= Collect Android Tracking Data =====================================================================================================================


@method_decorator(csrf_exempt)
def trackingView(request):
    if request.method == "POST":
        username = request.headers.get("username")
        password = request.headers.get("password")
        if username == "SECRET" and password == "SECRET":
            try:
                datadict = json.loads(request.body.decode("ascii"))
                phone_num = datadict.get("phone_num")
                if phone_num is None or len(phone_num) == 0:
                    return HttpResponse(
                        content="phone_num is incorrectly formatted", status=400
                    )
                if not check_whitelisted(phone_num):
                    return HttpResponse(content="access denied", status=403)
                bytes = base64.b64decode(datadict.get("loggedData"))
                android_make_line(phone_num, bytes)
                return HttpResponse(
                    json.dumps(
                        decode_Base64_actions(
                            datadict.get("loggedData").encode("ascii")
                        )
                    ),
                    status=200,
                )
            except:
                return HttpResponse("Invalid request", status=400)
        else:
            return HttpResponse("Invalid user " + str(username), status=403)
    else:
        return HttpResponse("Invalid request", status=400)


# Test request from browser console:
# fetch("/tracking/",
# {
#     headers: {
#       'username': 'SECRET',
#       'password': 'SECRET'
#     },
#     method: "POST",
#     body: '{"phone_num":"255000000000","loggedData":"a4miwti7TzICAA=="}'
# })
# .then(function(res){ res.text().then(s => console.log(s)) })
# .catch(function(err){ console.log(err) })
# Should return: ["SEARCH | 2022/10/29 | SUCCESSFUL | UNTRUNCATED | CLEANED | helloworld"]
