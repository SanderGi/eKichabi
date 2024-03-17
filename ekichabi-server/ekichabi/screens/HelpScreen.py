from django.utils.translation import ugettext as _

from .base_screens.BaseScreen import BaseScreen
from .Buttons import back_button, home_button, exit_button


class HelpScreen(BaseScreen):
    """displays simple instructions for the user"""

    def __init__(self):
        # help_text = _("Enter %s to go back from any screen.")
        help_text = _("Enter %s to go back from any screen. Enter %s to go back to the home screen from any screen.") + " " + _("Press %s to end the session.")

        body = (help_text % (back_button, home_button, exit_button))  # help phone removed by alex
        super().__init__(body=body)

    def note(self): return "HelpScreen"
