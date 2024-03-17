from django.core.management.base import BaseCommand
from ekichabi.management.commands.reset_db import import_data
from django.core.management import execute_from_command_line
from django.conf import settings
import os
import sys
from ekichabi.helper.Utils import bcolors
from django.utils.autoreload import restart_with_reloader

class Command(BaseCommand):
    help = 'Generate saved screen pickle file in Swahili. Reloads data as well'

    def add_arguments(self, parser) -> None:
        parser.add_argument(
            '--no-flush',
            help="Prevent reloading data",
            nargs=1
        )

    def handle(self, *args, **options):
        if os.environ.get("GENERATE_SCREENS", "false") == "false":
            if not options['no_flush'] or 'true' not in options['no_flush']:
                print(bcolors.HEADER + 'Flushing db' + bcolors.ENDC)
                execute_from_command_line(['manage.py','flush', '--no-input']) # ensure models are from csv and haven't been altered
                print(bcolors.HEADER + 'Resetting db' + bcolors.ENDC)
                import_data(settings.DATA_PATH) # reset_db
                execute_from_command_line(['manage.py','update_whitelist_db']) # re-whitelist everyone
            print(bcolors.HEADER + 'Generating screens' + bcolors.ENDC)
            os.environ["GENERATE_SCREENS"] = "true"
            restart_with_reloader()
        else:
            print('Language: ' + settings.LANGUAGE_CODE)
            os.environ["GENERATE_SCREENS"] = "false"
            print(bcolors.OKGREEN + 'DONE! Updated screens have been generated.' + bcolors.ENDC)