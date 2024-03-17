from ekichabi.helper.Logs import log_corresponding_menu_item
from ekichabi.helper.Utils import not_iterable

from ..LookupErrorScreen import LookupErrorScreen
from .BaseScreen import BaseScreen
from .InputScreen import InputScreen


class MenuScreen(InputScreen):

    exception_type = (ValueError, IndexError)

    @property
    def error_msg(self):
        return "{{input}} must be between 1 and {}".format(len(self.menu_items))

    def __init__(self, title="Select One", items=None):
        self.title_str = title
        self.menu_items = []
        self.add_items(items)

    def render(self, session, context):
        menu = [] if self.title_str == '' else ["%s" % self.title_str]
        for idx, item in enumerate(self.menu_items):
            menu_text = "%i. %s" % (
                idx+1, item) if not item.hide_index else str(item)
            menu.append(menu_text)
        return "\n".join(menu)

    def action(self, input, session, context):
        """ Return next menu screen or error message """
        index = int(input) - 1
        if index < 0:
            return LookupErrorScreen()
            # raise IndexError('Menu option can not be less than 1')
        if index > len(self.menu_items):
            return LookupErrorScreen()

    # send a note on what menu item was picked to the logger
        note = str(self.menu_items[index])
        log_corresponding_menu_item(session.session_info(), note)
        return self.menu_items[index].next_screen

    def add_items(self, items):
        if items is None:
            return  # Do nothing
        elif not_iterable(items):
            self.append(item)
        else:
            for item in items:
                self.append(item)

    def append(self, item):
        if not isinstance(item, MenuItem):
            item = MenuItem(item)
        self.menu_items.append(item)

    def note(self):
        """ overriding logging notes here """
        return self.title_str


class MenuItem(object):
    """ Object representing a menu option label and next_screen if selected
        custom_index parameter only supported when used within a LongMenuScreen """

    def __init__(self, label, next_screen=None, hide_index=False, custom_index=None):
        if not_iterable(label):
            self.label = str(label)
            self.next_screen = next_screen if next_screen is not None else BaseScreen.no_next(
                self.label)
        else:
            self.label = str(label[0])
            self.next_screen = label[1] if len(label) > 1 else None
            if not isinstance(self.next_screen, BaseScreen):
                self.next_screen = BaseScreen.no_next(self.next_screen)

        self.hide_index = hide_index
        self.custom_index = custom_index

    def __str__(self):
        return self.label
