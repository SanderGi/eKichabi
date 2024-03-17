from django.core.exceptions import ValidationError
from django.utils.translation import ugettext as _

from ekichabi.models import Whitelist
from ekichabi.services.android.utils import standard_format


def check_whitelisted(phone_num):
    ''' return true the given phone number is whitelisted, false otherwise
    '''

    # if phone number is not valid, raise error
    if phone_num is None or len(phone_num) == 0:
        raise ValueError("Phone number is not in a valid format")

    phone_num = standard_format(phone_num) # this is the format that numbers in the whitelist model should be in

    # filter by phone number
    matching_entries = Whitelist.objects.filter(phone_num=phone_num)
    matching_entries_cnt = matching_entries.count()

    # if there are more than one matching entries, that means there are duplicate
    # phone numbers in the database. This indicates an internal error since phone
    # number should be unique (see Model.py WhiteList class)
    if (matching_entries_cnt > 1):
        raise ValidationError(
            "Duplicate primary phone number found in Whitelist database")

    return matching_entries_cnt == 1


def get_permission_denied_msg():
    ''' Rejection message if user's phone number is not whitelisted'''

    return _("You have reached the eKichabi service. We are only sharing it with a few phone numbers currently, please check back in a couple months!")
