import bz2
import os
import pickle
import sys

from django.conf import settings
from django.db.utils import OperationalError
from django.utils import translation
from django.utils.translation import ugettext as _

from ekichabi.helper.Utils import bcolors
from ekichabi.screens.base_screens.BaseScreen import BaseScreen
from ekichabi.screens.HomeScreen import HomeScreen


def get_app():

    app_language = getattr(settings, 'NIAFIKRA_LANG', 'en')
    translation.activate(app_language)

    try:
        saved_screens_dir = getattr(settings, 'SAVED_SCREENS_PATH')
        if os.path.isfile(saved_screens_dir) and not settings.OVERWRITE_SCREENS:
            data = bz2.BZ2File(saved_screens_dir, 'rb')
            app = pickle.load(data)
        else:
            print(bcolors.OKBLUE +
                'OVERWRITE_SCREEN flag detected. Saving pickled screens...' + bcolors.ENDC)
            app = HomeScreen()
            sys.setrecursionlimit(40000)
            with bz2.BZ2File(saved_screens_dir, 'wb') as f:
                pickle.dump(app, f, protocol=pickle.HIGHEST_PROTOCOL)

    except OperationalError as e:
        app = BaseScreen(_("Databases Not Setup"))

    translation.deactivate()
    return app
