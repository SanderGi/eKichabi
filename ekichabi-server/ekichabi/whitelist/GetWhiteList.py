import csv
import os

from django.conf import settings

from ekichabi.models import Whitelist
from ekichabi.services.android.utils import standard_format

NUMBER1 = 1
NUMBER2 = 1


def populate_whitelist_for_admins(set):
    # demo number for internal testing
    set.add(standard_format("255000000000"))


def populate_whitelist_from_csv(set):

    # csv file name
    whitelist_csv = os.path.join(settings.PROJECT_DIR, "./data/whitelist_pretty.csv")

    # reading csv file
    with open(whitelist_csv, "r") as csvfile:
        # creating a csv reader object
        csvreader = csv.reader(csvfile)

        # ignore first row - which contains field names
        next(csvreader)

        # extracting phone number(s) out of each row
        for row in csvreader:
            if str(row[NUMBER1]):
                set.add(standard_format(str(row[NUMBER1])))  # number1
            if str(row[NUMBER2]):
                set.add(standard_format(str(row[NUMBER2])))  # number2


def get_whitelist_csv_rows():
    # csv file name
    whitelist_csv = os.path.join(settings.PROJECT_DIR, "./data/whitelist_pretty.csv")

    # reading csv file
    with open(whitelist_csv, "r") as csvfile:
        # creating a csv reader object
        csvreader = csv.reader(csvfile)

        # ignore first row - which contains field names
        next(csvreader)

        for row in csvreader:
            yield row


def get_newest_whitelist():
    whitelist_set = set()
    populate_whitelist_for_admins(whitelist_set)
    populate_whitelist_from_csv(whitelist_set)
    return whitelist_set


def get_current_whitelist_from_db():
    return set(Whitelist.objects.values_list("phone_num", flat=True))
