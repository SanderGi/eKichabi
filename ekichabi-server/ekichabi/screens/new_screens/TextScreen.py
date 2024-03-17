from .Screen import Screen


class TextScreen(Screen):

    def __init__(self, text=""):
        super.__init__(self, text)
        self.text = text

    def render(self):
        return self.text

    def validate(self, *args):
        if not isinstance(args.text, str):
            raise ValueError("text should be a string")
