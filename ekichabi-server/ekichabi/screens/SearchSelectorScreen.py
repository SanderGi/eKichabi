from django.utils.translation import ugettext as _

from .base_screens.MenuScreen import MenuScreen
from .FirstCharsScreen import FirstCharsScreen
from .SearchLocationScreen import SearchLocationScreen
from .SearchProductScreen import SearchProductScreen


class SearchSelectorScreen(MenuScreen):
    """docstring for SearchSelectorScreen"""
    # Ivy changed this

    def __init__(self):
        super(SearchSelectorScreen, self).__init__(
            title=_('What would you like to search for?'),
            items=[(_('Business name'), FirstCharsScreen("Business")),
                   (_('Location'), SearchLocationScreen()),
                   (_('Product/Service'), SearchProductScreen()),
                   (_('Owner name'), FirstCharsScreen("Owner"))
                   ])

    def note(self): return "SearchSelectorScreen"

    @property
    def error_msg(self):
        return _("Your input is not a valid menu item. Please try again.")
