# python imports
import csv
import datetime
import os
import sys
import unicodedata
from collections import defaultdict

from django.conf import settings

# django imports
from django.contrib.auth.models import User
from django.core.management import ManagementUtility
from django.core.management.base import BaseCommand, CommandError
from django.db import transaction
from ekichabi.helper.Utils import CSVData

# local imports
from ...models import Business, Category, District, Subsector, Subvillage, Village


class Command(BaseCommand):
    help = "Delete database, migrate models, and load sample data from a provided csv"

    def add_arguments(self, parser):
        parser.add_argument("csvpath", nargs="+")

    def handle(self, *args, **options):
        # delete old database
        self.stdout.write("Deleting old sqlite file")
        db_name = settings.DATABASES["default"]["NAME"]

        try:
            os.remove(db_name)

        except OSError:
            self.stdout.write("Couldn't find {} to remove".format(db_name))

        # Need this to recreate the db file
        utility = ManagementUtility(["reset_db.py", "check"])
        utility.execute()

        # migrate new models
        self.stdout.write("Migrating new models")
        utility = ManagementUtility(["reset_db.py", "migrate"])
        utility.execute()

        # set date and time - requires 'constance'
        ##config.CURRENT_DATE = datetime.date.today()

        # import new data and set up admin user
        self.stdout.write(self.style.MIGRATE_HEADING("Importing data"))
        with transaction.atomic():
            import_data(options["csvpath"][0])
            set_up_admin()

        self.stdout.write(
            self.style.SUCCESS(
                "All finished; successfully initialized new database with entries"
            )
        )


class importedBusiness(object):
    """super simple container for an imported business from csv"""

    def __init__(self, row):
        row = list(map(clean_ascii, row))

        self.name = row[CSVData.NAME]
        self.owner = row[CSVData.OWNER]
        self.category = row[CSVData.CATEGORY]

        self.ward = row[CSVData.WARD]
        self.district = row[CSVData.DISTRICT]
        self.village = row[CSVData.VILLAGE]
        self.subvillage = row[CSVData.SUBVILLAGE]

        # currently these get the swahili names
        self.subsector1 = row[CSVData.SUBSECTOR1]
        self.subsector2 = row[CSVData.SUBSECTOR2]

        self.crop1 = row[CSVData.CROP1]
        self.crop2 = row[CSVData.CROP2]
        self.crop3 = row[CSVData.CROP3]

        self.livestock1 = row[CSVData.LIVESTOCK1]

        self.specialty1 = row[CSVData.SPECIALTY1]
        self.specialty2 = row[CSVData.SPECIALTY2]
        self.specialty3 = row[CSVData.SPECIALTY3]
        self.specialty4 = row[CSVData.SPECIALTY4]

        self.input1 = row[CSVData.INPUT1]
        self.input2 = row[CSVData.INPUT2]
        self.input3 = row[CSVData.INPUT3]

        self.carrier1 = row[CSVData.CARRIER1]
        self.number1 = row[CSVData.NUMBER1]

        self.carrier2 = row[CSVData.CARRIER2]
        self.number2 = row[CSVData.NUMBER2]

    def __str__(self):
        return "{} in {} - {}".format(self.name, self.village, self.subvillage)


class importedLocations(object):
    """super simple class to manage the hierarchy of district > village > subvillage.
    data is stored in a dict:
         keys are the district names, and their values are additional dicts,
         whose keys are village names and values are sets of subvillage names.
    """

    def __init__(self):
        self.districts = defaultdict(lambda: defaultdict(set))

    def add(self, district, village, subvillage):
        # if district not in self.districts:
        #    self.districts[district] = dict()

        # if village not in self.districts[district]:
        #    self.districts[district] = dict()

        self.districts[district][village].add(subvillage)

    def __contains__(self, item):
        return item in self.districts

    def __iter__(self):
        """iterate over (village, dict(subvillages)) tuples"""
        return iter(self.districts.items())


def import_data(csvpath, ignore_header=True):
    # can have handle() change ignore header if needed later

    print("Importing data into database from {}".format(csvpath))

    businesses = set()
    locations = importedLocations()
    categories = {}

    # for convenient printing
    created_counts = {
        "businesses": 0,
        "districts": 0,
        "villages": 0,
        "subvillages": 0,
        "subsector": 0,
    }

    businessreader = CSVData.getReader(ignore_header)

    # import businesses into class defined above
    for row in businessreader:
        businesses.add(importedBusiness(row))

    for b in businesses:
        # get villages and subvillages
        locations.add(b.district, b.village, b.subvillage)
        if b.category not in categories:
            categories[b.category] = set()
        categories[b.category].add(b.subsector1)
        categories[b.category].add(b.subsector2)

    # print all category and subsector in csv format
    """for c in categories:
        print(c)
        for subsector in categories[c]:
            print("," + subsector)
        print("")"""

    # we've got our data, now we can finally add django models
    # add districts, villages and subvillages
    print("\tCreating districts, villages and subvillages")
    for district_name, village_dict in locations:
        district = District(name=district_name)
        district.save()
        created_counts["districts"] += 1

        for village_name, subvillages in iter(village_dict.items()):
            village = Village(name=village_name, district=district)
            village.save()
            created_counts["villages"] += 1

            for subvillage_name in subvillages:
                subvillage = Subvillage(name=subvillage_name, village=village)
                subvillage.save()
                created_counts["subvillages"] += 1

    # finally, add businesses
    print("\tCreating businesses")
    for b in businesses:
        category, _ = Category.objects.get_or_create(name=b.category)

        district = District.objects.filter(name=b.district)[0]
        village = Village.objects.filter(name=b.village, district=district)[0]
        subvillage = Subvillage.objects.filter(name=b.subvillage, village=village)[0]

        # Ivy changed this
        subsector1, _ = Subsector.objects.get_or_create(
            name=b.subsector1, category=category
        )
        subsector2, _ = Subsector.objects.get_or_create(
            name=b.subsector2, category=category
        )

        business = Business(
            name=b.name,
            category=category,
            district=district,
            village=village,
            subvillage=subvillage,
            # Ivy changed this
            ward=b.ward,
            number1=b.number1,
            number2=b.number2,
            # Ivy changed this
            subsector1=subsector1,
            subsector2=subsector2,
            crop1=b.crop1,
            crop2=b.crop2,
            crop3=b.crop3,
            livestock1=b.livestock1,
            specialty1=b.specialty1,
            specialty2=b.specialty2,
            specialty3=b.specialty3,
            specialty4=b.specialty4,
            input1=b.input1,
            input2=b.input2,
            input3=b.input3,
            # Ivy changed this
            owner=b.owner,
        )
        business.save()
        created_counts["businesses"] += 1

    print("Finshed importing data:")
    s = "Created {} businesses, {} villages, and {} subvillages in {} districts."
    print(
        s.format(
            created_counts["businesses"],
            created_counts["villages"],
            created_counts["subvillages"],
            created_counts["districts"],
        )
    )


def set_up_admin():
    print("Creating Admin User - username:SECRET, password:SECRET")
    User.objects.create_superuser("SECRET", email="SECRET", password="SECRET")


def clean_ascii(text):
    try:
        text = text.decode("utf-8")
    except AttributeError:
        pass
    return unicodedata.normalize("NFKD", text).encode("ascii", "ignore").decode("ascii")


if __name__ == "__main__":
    # for debugging purposes when running directly from the command line
    # this will only run the database additions, it will *not* delete the database
    import_data(sys.argv[1])
