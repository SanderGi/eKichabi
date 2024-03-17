class BaseScreen(object):
    """ this is the base class from which all other screens subclass """
    body = "None"
    _has_next = False
    next_screen = None
    ommitfromhistory = False
    press_next = None

    def skipTo(self, inputstr):
        self.press_next = inputstr

    def __init__(self, body=None, has_next=None, next_screen=None):
        if body is not None:
            self.body = body
        if has_next is not None:
            self._has_next = False
        if next_screen is not None:
            self.next_screen = next_screen
        else:
            self.next_screen = self.get_next_screen()

    def has_next(self, session):
        return bool(self._has_next) or self.next_screen is not None

    def render(self, session, context):
        try:
            return self.body.format(**context)
        except TypeError as e:
            return self.body

    def input(self, input, session, context):
        """ Process input for screen
            return: ScreenResult (next_screen , valid , output )
        """
        return BaseScreen("None") if self.next_screen is None else self.next_screen

    def get_next_screen(self):
        return self.next_screen

    def note(self):
        ''' this is added to the log when this screen is displayed'''
        if self.body == 'None':
            return self.__class__.__name__
        else:
            return self.body

    ########################################
    # Static Factory Methods
    ########################################

    @classmethod
    def no_next(cls, label):
        return cls("%s Has No Next Action" % label)
