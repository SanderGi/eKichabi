import random

from django.utils.translation import ugettext as _

from ekichabi.models import District

from .base_screens.MenuScreen import MenuScreen
from .MenuHierarchyScreen import MenuHierarchyScreen


# Allow user to choose if they want to filter current searching results by location
# Yes, return the screen user can choose location
# No, return list of the current searching results
class IfFilterByLocationScreen(MenuScreen):
    def __init__(self, query_set, rand100=False):
        title = _('Do you want to filter results by location?')
        if query_set.count() > 100:  # quick fix to prevent absurdly large query sets from reaching a recursion limit
            # query_reduced = query_set.order_by('?')[:100]
            rand100 = rand100 if rand100 else random.sample(range(10000), 100)
            mod = query_set.count()
            ids = query_set.values_list('id', flat=True)
            query_reduced = [ids[i % mod] for i in rand100]
            query_set = query_set.filter(id__in=query_reduced)
            title = _(
                'There was more than 100 results. Do you want to filter 100 random results by location?')
        super().__init__(
            title=title,
            items=[(_("Yes"), MenuHierarchyScreen(filters=[District], query_set=query_set)),  # for some reason the menuheirarchy screen fails at more filters. TODO: look into fixing screen code
                   (_("No"), MenuHierarchyScreen(query_set=query_set))
                   ])

    def note(self): return "IfFilterBYLocationScreen"

    @property
    def error_msg(self):
        return _("Your input is not a valid menu item. Please try again.")
