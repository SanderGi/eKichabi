from django.utils.translation import ugettext as _

from .base_screens.MenuScreen import MenuScreen
from .FirstCharsScreen import FirstCharsScreen


class SearchProductScreen(MenuScreen):
    """docstring for SearchSelectorScreen"""
    # Ivy changed this

    def __init__(self):
        super().__init__(
            title=_('What would you like to search for?'),
            items=[(_('Crop'), FirstCharsScreen("Crop")),
                   (_('Agricultural Product'), FirstCharsScreen("Input")),
                   (_('Livestock'), FirstCharsScreen("Livestock")),
                   (_('Specialty'), FirstCharsScreen("Specialty")),
                   ])

    def note(self): return "SearchProductScreen"

    @property
    def error_msg(self):
        return _("Your input is not a valid menu item. Please try again.")
