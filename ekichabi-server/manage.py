#!/usr/bin/env python
import collections.abc
import os
import sys

collections.Iterator = collections.abc.Iterable
collections.Mapping = collections.abc.Mapping
collections.Sequence = collections.abc.Sequence

from django.core.management import execute_from_command_line

if __name__ == "__main__":

    os.environ.setdefault("DJANGO_SETTINGS_MODULE", "config.settings")

    if sys.argv[1] == 'reset_db' or sys.argv[1] == 'flush' or sys.argv[1] == 'update_whitelist_db':
        os.environ["MIGRATION"] = "true"
    else:
        os.environ["MIGRATION"] = "false"
    
    os.environ.setdefault("GENERATE_SCREENS", "false")

    if sys.argv[1] == 'runserver':
        if '--use-old-screens' not in sys.argv:
            execute_from_command_line(['manage.py','generate_screens','--no-flush','true']) # generate screens when runserver
        else:
            sys.argv.remove('--use-old-screens')
            sys.argv.append('--noreload')
        if '--update-static' in sys.argv:
            execute_from_command_line(['manage.py','collectstatic','--no-input'])
            sys.argv.remove('--update-static')

    execute_from_command_line(sys.argv)
