from django.utils.translation import ugettext as _

from ekichabi.models import District, Subvillage, Village

from .base_screens.MenuScreen import MenuScreen
from .FirstCharsLocationScreen import FirstCharsLocationScreen


# Allow user to choose which level of location they want to search
# Return the screen that user can type to search
class SearchLocationScreen(MenuScreen):
    def __init__(self):
        super(SearchLocationScreen, self).__init__(
            title=_('What level do you want to search for'),
            items=[#(_("District"), FirstCharsLocationScreen(District)),
                   (_("Village"), FirstCharsLocationScreen(Village)),
                   (_("Subvillage"), FirstCharsLocationScreen(Subvillage))
                   ])

    def note(self): return "SearchLocationScreen"

    @property
    def error_msg(self):
        return _("Your input is not a valid menu item. Please try again.")
