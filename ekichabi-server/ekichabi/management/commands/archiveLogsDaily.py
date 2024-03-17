from django.conf import settings
from glob import glob
import os

from django.core.management.base import BaseCommand

from zipfile import ZipFile
from datetime import datetime

def get_unarchived_ussd_logs():
    return [path for path in glob(settings.NIAFIKRA_LOG_DIR + "/*_*-*-*-*.*.*.log")]

class Command(BaseCommand):
    help = "management command to run once a day to archive logs so they don't clutter up the logs folder"

    def handle(self, *args, **options):
        path = settings.PROJECT_DIR + '/logsArchive/'
        filename = 'ussd_' + datetime.today().strftime('%Y-%m-%d-%H.%M.%S') + '.zip'
        todays_logs = get_unarchived_ussd_logs()
        if not os.path.exists(path):
            os.makedirs(path)
        with ZipFile(path + filename, 'w') as zip, ZipFile(path + 'all_ussd.zip', 'a') as masterZip:
            # writing each file one by one
            for file in todays_logs:
                zip.write(file, os.path.basename(file))
                masterZip.write(file, os.path.basename(file))
        for file in todays_logs:
            os.remove(file)