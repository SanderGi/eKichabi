from ekichabi.whitelist.GetWhiteList import get_whitelist_csv_rows, NUMBER1, NUMBER2
from ekichabi.services.android.utils import standard_format
from random import shuffle
import csv
import os
from django.conf import settings
from math import ceil

GROUP_PATH = os.path.join(settings.PROJECT_DIR, "./data/AB_test_groups.csv")
GROUP_NAMES = ["A", "B"]


def assign_test_groups():
    """Takes each person in the whitelist and randomly assigns one of the test_groups such that the final distribution of groups follow as nearly a uniform distribution as possible. Returns the distribution."""
    users = list(get_whitelist_csv_rows())
    groups = (GROUP_NAMES * ceil(len(users) / len(GROUP_NAMES)))[: len(users)]
    shuffle(groups)

    with open(GROUP_PATH, "w", newline="") as file:
        writer = csv.writer(file)
        writer.writerow(["mobile_number1", "mobile_number2", "group"])
        for user, group in zip(users, groups):
            writer.writerow(
                [standard_format(user[NUMBER1]), standard_format(user[NUMBER2]), group]
            )

    distribution = {name: groups.count(name) for name in GROUP_NAMES}
    return distribution


def get_or_set_test_group(number):
    """Gets the test group name associated with the given number. Assigns groups if they are not already. The number must be whitelisted and part of the assigned groups, otherwise False is returned"""
    if not os.path.isfile(GROUP_PATH):
        assign_test_groups()

    number = standard_format(number)

    with open(GROUP_PATH, "r") as file:
        # creating a csv reader object
        reader = csv.reader(file)

        # ignore first row - which contains field names
        next(reader)

        for row in reader:
            if row[0] == number or row[1] == number:
                return row[2]

    return False
