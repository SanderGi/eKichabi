from django.utils.translation import ugettext as _

from ekichabi.models import Category, District, Subsector, Subvillage, Village

from .base_screens.MenuScreen import MenuScreen
from .HelpScreen import HelpScreen
from .MenuHierarchyScreen import MenuHierarchyScreen
from .SearchSelectorScreen import SearchSelectorScreen


class HomeScreen(MenuScreen):
    """ Home screen for the application """

    def __init__(self):
        super().__init__(
            title=_("Welcome to eKichabi!\n") + _("Select an option:"),
            items=[
                (_('Search by category'),
                    MenuHierarchyScreen([Category, Subsector, District, Village, Subvillage])),
                (_('Browse by location'),
                    MenuHierarchyScreen([District, Village, Subvillage, Category, Subsector])),
                (_('Search'), SearchSelectorScreen()),
                (_('Instructions'), HelpScreen())
            ])

    def note(self): return "HomeScreen"

    @property
    def error_msg(self):
        return _("Your input is not a valid menu item. Please try again.")
