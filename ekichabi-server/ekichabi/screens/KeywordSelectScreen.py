from django.utils.translation import ugettext as _

from .base_screens.LongMenuScreen import LongMenuScreen
from .Buttons import back_button
from .base_screens.BaseScreen import BaseScreen
from ..helper.Logs import log_corresponding_menu_item

import dill

class KeywordSelectScreen(LongMenuScreen):

    def __init__(self, search='keyword', keywords=[], screen_fn=lambda key: BaseScreen(key), is_lazy=True):
        '''Menu screen to select a key that leads to a screen specified by screen_fn'''

        self.overflow_str = _('next, %s. back') % back_button
        self.back_str = _("%s. Back") % back_button

        self.screen_fn = dill.dumps(screen_fn) if is_lazy else ''
        self.is_lazy = is_lazy

        items = []
        for key in keywords:
            if is_lazy:
                items.append((key.lower().capitalize(), BaseScreen(key))) # hacky solution to get around the long menu screen auto creating a basescreen
            else:
                items.append((key.lower().capitalize(), screen_fn(key)))

        items.sort(key=lambda i: i[0])

        if len(items) == 1:
            self.skipTo("1")

        super().__init__(title=_("Select a %(filter)s:") % {'filter': search}, items=items)

    def action(self, input, session, context):
        # overwrite action function to allow lazy loading item screens
        def make_index(elt):
            idx, item = elt
            if item.custom_index is not None:
                return str(item.custom_index)
            else:
                return str(idx)

        valid_inputs = list(map(make_index, enumerate(self.menu_items)))
        index = valid_inputs.index(input)

        # send a note on what menu item was picked to the logger
        note = str(self.menu_items[index])
        log_corresponding_menu_item(session.session_info(), note)
        if self.is_lazy:
            return dill.loads(self.screen_fn)(self.menu_items[index].next_screen.body) # hacky solution to get around the long menu screen auto creating a basescreen
        else:
            return self.menu_items[index].next_screen

    def note(
        self): return "KeywordSelectScreen"

    @property
    def error_msg(self):
        return _("Your input is not a valid menu item. Please try again.")
