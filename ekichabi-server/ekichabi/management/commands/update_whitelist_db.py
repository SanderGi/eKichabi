from django.core.management.base import BaseCommand

from ekichabi.models import Whitelist
from ekichabi.whitelist.GetWhiteList import (get_current_whitelist_from_db,
                                             get_newest_whitelist)


class Command(BaseCommand):
    help = 'Load new phone numbers in whitelist csv into the db, removing outdated phone numebrs'

    def handle(self, *args, **options):
        newest_whitelist = get_newest_whitelist()
        current_whitelist = get_current_whitelist_from_db()
        new_phone_nums_count = 0
        # Only add in new phone numbers to the database
        for phone_num in newest_whitelist:
            if not phone_num in current_whitelist:
                new_whitelist_entry = Whitelist(phone_num=phone_num)
                new_whitelist_entry.save()
                new_phone_nums_count += 1
            else:
                current_whitelist.remove(phone_num)
        # All the phone numbers are the ones not found in the newest list, remove them one by one from db
        old_phone_nums_count = len(current_whitelist)
        for phone_num in current_whitelist:
            Whitelist.objects.get(phone_num=phone_num).delete()

        print(new_phone_nums_count, "new phone numbers are whitelisted, ",
              old_phone_nums_count, "old phone numbers are deleted")
