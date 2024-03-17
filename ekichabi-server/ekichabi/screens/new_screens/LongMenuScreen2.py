
from ..Buttons import back_button, forward_button
from .MenuScreen2 import MenuItem
from .Screen import Screen


class LongMenuScreen2(Screen):

    overflow_str = 'next, %s. back' % back_button
    back_str = "%s. Back" % back_button
    max_line = 5

    def __init__(self, title="", items=[]):
        super.__init__(self, title, items)
        self.title_str = title
        self.items = []

        if len(items) < self.max_line:
            self.items = items
            self.items.append(MenuItem(len(items) + 1, ))
            self.menu_items.append(
                MenuItem(self.back_str, hide_index=True))
            # this is a bit hacky since it doesn't use the BaseScreen
            # instead relies on sessions to catch the back key

        # else:
            # we need an overflow page
            # overflow_item = MenuItem(self.overflow_str,
            #                          # TODO: this logic is wrong
            #                          LongMenuScreen2(
            #                              title, items[i:], shift=i+shift),
            #                          custom_index=forward_button)

            # self.menu_items.append(overflow_item)

    def render(self):
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

    def current_length(self, ):

        title_len = len(self.title_str) + 4
        item_len = sum(map(lambda x: len(str(x)), self.menu_items))
        numeral_len = 4 * len(self.menu_items)  # acount for the indices
        # back_len = len(self.back_str) if show_back else 0
        # return sum((title_len, item_len, numeral_len, back_len))
