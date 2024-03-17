from django.utils.translation import ugettext as _

from .base_screens.BaseScreen import BaseScreen
from .Buttons import back_button, exit_button


class LookupErrorScreen(BaseScreen):
    def __init__(self):
        error_msg = _('Your query could not be found.\n' +
                      'Please check spelling and try again, ' +
                      'enter %s to go back or press %s to end the session.') % (back_button, exit_button)
        super(LookupErrorScreen, self).__init__(
            body=error_msg,
        )

    def note(self): return "LookupErrorScreen"
