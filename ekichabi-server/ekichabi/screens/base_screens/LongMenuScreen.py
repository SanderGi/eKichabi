from ekichabi.helper.Logs import log_corresponding_menu_item

from ..Buttons import back_button, forward_button
from .InputScreen import InputScreen
from .MenuScreen import MenuItem, MenuScreen


class LongMenuScreen(MenuScreen):
    """ adds items until 160 chars are taken up, then puts
        the rest in a submenu titled 'next'. """
    # gettext doesn't work in import time, btw remember to redo import
    cutoff_len = 160  # a margin of error here

    def __init__(self, title="Select One", items=None, show_back=True, shift=0, overflow_str='next, %s. back' % back_button, back_str = "%s. Back" % back_button):
        self.title_str = title
        self.menu_items = []
        self.overflow_str = overflow_str
        self.back_str = back_str

        def current_length():
            title_len = len(self.title_str) + 4
            item_len = sum(map(lambda x: len(str(x)), self.menu_items))
            numeral_len = 4 * len(self.menu_items)  # acount for the indices
            back_len = len(self.back_str) if show_back else 0
            return sum((title_len, item_len, numeral_len, back_len))

        i = 0
        while i < len(items) and current_length() + len(items[i][0]) < self.cutoff_len-len(self.overflow_str):
            # TODO: this logic isn't perfect, as if there's only one more item left,
            # we can fit it on a single screen if it keeps len < 160 chars
            self.menu_items.append(MenuItem(
                items[i], custom_index=i+shift+1))
            i += 1

        if i == len(items):
            # we can fit all on this page, so let's add the back_str if needed
            if show_back:
                self.menu_items.append(
                    MenuItem(self.back_str, hide_index=True))
                # this is a bit hacky since it doesn't use the BaseScreen
                # instead relies on sessions to catch the back key

        else:
            # we need an overflow page
            overflow_item = MenuItem(self.overflow_str,
                                     # TODO: this logic is wrong
                                     LongMenuScreen(
                                         title, items[i:], shift=i+shift, overflow_str=self.overflow_str, back_str=self.back_str),
                                     custom_index=forward_button)

            self.menu_items.append(overflow_item)

    def render(self, session, context):
        """ like render() for MenuScreen, but displays custom_index """
        # TODO: at some point this should actually get implemented, but to do so
        # would need to convert self.menu_items from list to a dict, where the
        # keys are the items that should be selected in the menu.
        # to do so would need to update MenuItem as well.
        menu = [] if self.title_str == '' else ["   %s" % self.title_str]
        for idx, item in enumerate(self.menu_items):
            if item.hide_index:
                menu_text = str(item)
            else:
                menu_text = "%s. %s" % (item.custom_index, item)
            menu.append(menu_text)
        return "\n".join(menu)

    def action(self, input, session, context):
        """ Return next menu screen or error message
            Interprets input as str and compares to custom_index """
        #index = int(input) - 1
        # if index < 0:
        #    raise IndexError('Menu option can not be less than 1')
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

        return self.menu_items[index].next_screen

    @property
    def error_msg(self):
        return "{input} is not a valid menu item. Please try again."
