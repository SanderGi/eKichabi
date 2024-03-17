from .Screen import Screen


class MenuScreen2(Screen):

    def __init__(self, title="", items=[]):
        super.__init__(self, title, items)
        self.title = title
        self.items = items

    def render(self):
        menu = [self.title] if len(self.title) == 0 else []
        for idx, item in enumerate(self.items):
            menu_text = "%i. %s" % (
                idx, item) if not item.hide_index else str(item)
            menu.append(menu_text)
        return "\n".join(menu)

    def validate(self, *args):
        if not isinstance(args.title, str):
            raise ValueError("title should be a string")
        for item in args.items:
            if not isinstance(item, MenuItem2):
                raise ValueError("item passed in must be a MenuItem")


class MenuItem2(object):

    def __init__(self, index, label, next_screen, hide_index=False):

        # validate arguments
        if not isinstance(index, int) or not isinstance(label, str) or isinstance(next_screen, Screen):
            raise ValueError("invalid argument to construct menu item")

        self.index = index
        self.label = label
        self.next_screen = next_screen
        self.hide_index = hide_index

    def __str__(self):
        return self.index + ". " + self.label
