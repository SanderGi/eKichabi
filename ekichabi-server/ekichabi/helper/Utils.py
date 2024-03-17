import pickle
import codecs
from django.conf import settings
import csv

def not_iterable(obj):
    """ Return True if obj is a string or non iterable"""
    return hasattr(obj, "rstrip") or not (hasattr(obj, "__getitem__") or hasattr(obj, "__iter__"))

# From: http://stackoverflow.com/a/32536493/2708328


class abstract_attribute(object):
    def __get__(self, obj, type):
        # Now we will iterate over the names on the class,
        # and all its superclasses, and try to find the attribute
        # name for this descriptor
        # traverse the parents in the method resolution order
        for cls in type.__mro__:
            # for each cls thus, see what attributes they set
            for name, value in cls.__dict__.items():
                # we found ourselves here
                if value is self:
                    # if the property gets accessed as Child.variable,
                    # obj will be done. For this case
                    # If accessed as a_child.variable, the class Child is
                    # in the type, and a_child in the obj.
                    this_obj = obj if obj else type

                    raise NotImplementedError(
                        "%r does not have the attribute %r "
                        "(abstract from class %r)" %
                        (this_obj, name, cls.__name__))

        # we did not find a match, should be rare, but prepare for it
        raise NotImplementedError(
            "%s does not set the abstract attribute <unknown>", type.__name__)


# class to print colored command outputs
class bcolors:
    HEADER = '\033[95m'
    OKBLUE = '\033[94m'
    OKCYAN = '\033[96m'
    OKGREEN = '\033[92m'
    WARNING = '\033[93m'
    FAIL = '\033[91m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'
    UNDERLINE = '\033[4m'

def save_pickled(obj):
    return codecs.encode(pickle.dumps(obj), "base64").decode()

def load_pickled(pickled):
    return pickle.loads(codecs.decode(pickled.encode(), "base64"))

# standard way to load data for both Android and USSD
class CSVData:
    FILENAME = getattr(settings, "DATA_PATH") # csv file name
    ANONYMOUS_FILENAME = FILENAME.replace('census_data_trimmed.csv', 'census_data_anonymous.csv')

    NAME = 7
    OWNER = 19
    CATEGORY = 20

    WARD = 39
    DISTRICT = 6
    VILLAGE = 14
    SUBVILLAGE = 38

    # swahili names
    SUBSECTOR1 = 36
    SUBSECTOR2 = 37

    # english names
    SUBSECTOR1_EN = 34
    SUBSECTOR2_EN = 35

    CROP1 = 3
    CROP2 = 4
    CROP3 = 5

    LIVESTOCK1 = 16

    SPECIALTY1 = 30
    SPECIALTY2 = 31
    SPECIALTY3 = 32
    SPECIALTY4 = 33

    INPUT1 = 11
    INPUT2 = 12
    INPUT3 = 13

    CARRIER1 = 21
    NUMBER1 = 17

    CARRIER2 = 22
    NUMBER2 = 18

    @classmethod
    def getReader(cls, anonymous=False, ignore_header=True):
        # reading csv file
        with open(cls.ANONYMOUS_FILENAME if anonymous else cls.FILENAME, 'r') as csvfile:
            # creating a csv reader object
            csvreader = csv.reader(csvfile)

            # extracting field names through first row -- ignore header
            if ignore_header:
                next(csvreader)

            # extracting each data row one by one
            for row in csvreader:
                yield row