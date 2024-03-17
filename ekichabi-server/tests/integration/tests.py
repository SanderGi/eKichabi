from types import ModuleType
import sys
import datetime
import importlib
import inspect
import json
import os
import random

from django.conf import settings
from django.core.management import call_command
from django.test import Client, SimpleTestCase, TestCase, override_settings

import ekichabi.helper.GetApp as startmodule
from ekichabi.management.commands.reset_db import import_data


# automatically overwrite settings, only works for post constructor stuff for some reason
@override_settings(OVERWRITE_SCREENS=False, LANGUAGE_CODE="sw", NIAFIKRA_LANG="sw")
class TestTestEnvironment(SimpleTestCase):
    """Make sure settings are set up for testing"""

    def test_language(self):
        """settings language should be swahilli"""
        print(
            "===== Method: " + str(inspect.stack()[0][3]) + " ====="
        )  # name of test method
        self.assertEqual(settings.LANGUAGE_CODE, settings.NIAFIKRA_LANG)
        self.assertEqual(settings.NIAFIKRA_LANG, "sw")

    def test_regeneratingscreens(self):
        """For testing purposes, tests should determine when screens are generated"""
        print(
            "===== Method: " + str(inspect.stack()[0][3]) + " ====="
        )  # name of test method
        self.assertFalse(settings.OVERWRITE_SCREENS)


class TestAndroid(SimpleTestCase):
    """Test android integration endpoints"""

    databases = "__all__"

    def test_date(self):
        """date is in the right format"""
        print(
            "===== Method: " + str(inspect.stack()[0][3]) + " ====="
        )  # name of test method
        c = Client()
        response = c.get("/date/")
        self.assertEqual(response.status_code, 200)
        try:
            datetime.datetime.strptime(response.content.decode("utf-8"), "%Y/%m/%d")
        except ValueError:
            self.fail("the format of the date is not YYYY/MM/DD")

    def test_businessisjson(self):
        """business json is correctly returned in json format"""
        print(
            "===== Method: " + str(inspect.stack()[0][3]) + " ====="
        )  # name of test method

        c = Client()
        response = c.post(
            path="/business/",
            data="username=SECRET&password=SECRET",
            content_type="application/x-www-form-urlencoded",
        )

        self.assertEqual(response.status_code, 200)
        try:
            s = json.JSONEncoder()
            s.encode(response.content.decode("utf-8"))
        except:
            self.fail("android business json is not valid json")

    def test_business(self):
        """Business json matches expected value"""
        print(
            "===== Method: " + str(inspect.stack()[0][3]) + " ====="
        )  # name of test method

        c = Client()
        response = c.post(
            path="/business/",
            data="username=SECRET&password=SECRET",
            content_type="application/x-www-form-urlencoded",
        )
        self.assertEqual(response.status_code, 200)

        with open(
            os.path.join(os.path.dirname(__file__), "androidexpected.json"), "r"
        ) as file:
            data = file.read().rstrip()

        self.assertEqual(response.content.decode("utf-8"), data)


@override_settings(OVERWRITE_SCREENS=False, LANGUAGE_CODE="sw", NIAFIKRA_LANG="sw")
class TestScreens(TestCase):
    """Tests the screens by making requests and comparing the output with expected output"""

    @classmethod
    def setUpTestData(cls):
        print(
            "setUpTestData: Run once to set up non-modified data for all class methods."
        )
        call_command("generate_screens")

    def setUp(self):
        print("\nsetUp: Run once for every test method to setup clean data.")

    def test_homescreen(self):
        """Make sure the homescreen is correct"""
        print(
            "===== Method: " + str(inspect.stack()[0][3]) + " ====="
        )  # name of test method

        status_code, response = Navigation.visitHomescreen(
            Simulator.generateUniqueSessionId()
        )
        self.assertEqual(status_code, 200)
        self.assertEqual(
            response,
            {
                "action": "con",
                "text": "Karibu eKichabi!\nChagua ya ziada:\n1. Tafuta kwa kuchagua\n2. Tafuta kwa kuandika\n3. Maelekezo",
            },
        )

    def test_menuerror(self):
        """Invalid menu item on homescreen"""
        print(
            "===== Method: " + str(inspect.stack()[0][3]) + " ====="
        )  # name of test method

        id = Simulator.generateUniqueSessionId()
        Navigation.visitHomescreen(id)
        status_code, response = Navigation.makeinput(id, 6)
        self.assertEqual(status_code, 200)
        self.assertEqual(
            response,
            {
                "action": "con",
                "text": "Hoja yako haikupatikana.\nTafadhali angalia tahajia na ujaribu tena, weka 99 ili kurudi nyuma au ubonyeze kitufe chochote ili kukatisha kipindi.",
            },
        )

    def test_menuhierarchyerror(self):
        """Invalid menu item on category search screen"""
        print(
            "===== Method: " + str(inspect.stack()[0][3]) + " ====="
        )  # name of test method

        id = Simulator.generateUniqueSessionId()
        Navigation.visitHomescreen(id)
        Navigation.makeinput(id, 1)
        status_code, response = Navigation.makeinput(id, 800)
        self.assertEqual(status_code, 200)
        self.assertEqual(
            response,
            {
                "action": "con",
                "text": "Hoja yako haikupatikana.\nTafadhali angalia tahajia na ujaribu tena, weka 99 ili kurudi nyuma au ubonyeze kitufe chochote ili kukatisha kipindi.",
            },
        )

    def test_backbutton(self):
        """Visit random screens and see if 99 gives the previous screen"""
        print(
            "===== Method: " + str(inspect.stack()[0][3]) + " ====="
        )  # name of test method

        for ___ in range(10):
            id = Simulator.generateUniqueSessionId()
            status_code, response = Navigation.visitHomescreen(id)
            print("----- starting on homescreen -----")
            for __ in range(4):
                input = random.randint(1, 3)
                print("~~~ input: " + str(input))
                status_code, res = Navigation.makeinput(id, input)
                if not status_code != 200 or res["action"] != "con":
                    print("!!! end/invalid screen found !!!")
                    break

                status_code, response_back = Navigation.makeinput(id, 99)
                self.assertEqual(status_code, 200)
                self.assertEqual(response, response_back)

                status_code, response = Navigation.makeinput(id, input)
                self.assertEqual(status_code, 200)

    def test_whitelist(self):
        status_code, response = Simulator.post(
            {
                "url": "/ussd/",
                "serviceCode": "*100",
                "phoneNumber": "null",
                "sessionId": Simulator.generateUniqueSessionId(),
                "text": str(input),
            }
        )
        self.assertEqual(status_code, 200)
        self.assertEqual(response["action"], "end")

    def test_searchcategory_compareoutput(self):
        """Go through some inputs on search category and compare outputs with expected outputs"""
        print(
            "===== Method: " + str(inspect.stack()[0][3]) + " ====="
        )  # name of test method

        # select category screen
        id = Simulator.generateUniqueSessionId()
        Navigation.visitHomescreen(id)
        status_code, response = Navigation.makeinput(id, 1)
        self.assertEqual(status_code, 200)
        self.assertEqual(
            response,
            {
                "action": "con",
                "text": "SECRET",
            },
        )

        # select district screen
        status_code, response = Navigation.makeinput(id, 1)
        self.assertEqual(status_code, 200)
        self.assertEqual(
            response,
            {
                "action": "con",
                "text": "SECRET",
            },
        )

        # select village
        status_code, response = Navigation.makeinput(id, 1)
        self.assertEqual(status_code, 200)
        self.assertEqual(
            response,
            {
                "action": "con",
                "text": "SECRET",
            },
        )

        # All Businesses
        status_code, response = Navigation.makeinput(id, 1)
        self.assertEqual(status_code, 200)
        self.assertEqual(
            response,
            {
                "action": "con",
                "text": "SECRET",
            },
        )

        # Select A Business
        status_code, response = Navigation.makeinput(id, 1)
        self.assertEqual(status_code, 200)
        self.assertEqual(
            response,
            {
                "action": "con",
                "text": "SECRET",
            },
        )

        # Business screen
        status_code, response = Navigation.makeinput(id, 1)
        if "SECRET" in response["text"]:
            # Disclaimer screen that shows up the first time a user views a(ny) business
            status_code, response = Navigation.makeinput(id, 0)
        self.assertEqual(status_code, 200)
        self.assertEqual(
            response,
            {
                "action": "con",
                "text": "SECRET",
            },
        )

        # end
        for i in range(8):
            status_code, response = Navigation.makeinput(id, 0)
        self.assertEqual(status_code, 200)
        self.assertEqual(response, {"action": "end", "text": "Imekamilika"})


class Navigation:
    def visitHomescreen(id):
        return Simulator.post(
            {
                "url": "/ussd/",
                "serviceCode": "*100",
                "phoneNumber": "255000000000",
                "sessionId": id,
                "text": "",
            }
        )

    def makeinput(id, input):
        return Simulator.post(
            {
                "url": "/ussd/",
                "serviceCode": "*100",
                "phoneNumber": "255000000000",
                "sessionId": id,
                "text": str(input),
            }
        )


class Simulator:
    def generateUniqueSessionId():
        """Random (hopefully unique) id in the format for session ids"""
        return random.randint(10000000, 999999999)

    def post(data):
        """Like the simulator but for running offline requests in testing"""
        c = Client()
        url = data["url"]

        payload = {
            "sessionid": data.get("sessionId", ""),
            "msisdn": data.get("phoneNumber", ""),
            "input": data.get("text", ""),
        }

        response = c.get(url, data=payload)
        action = "con" if response.headers.get("Session", "Q") == "C" else "end"
        text = response.content.decode("utf-8")
        return response.status_code, {"action": action, "text": text}
