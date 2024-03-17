from abc import ABC, abstractmethod


class Screen(ABC):

    @abstractmethod
    def __init__(self, *args):
        self.validate(args)

    @abstractmethod
    def render(self):
        pass

    @abstractmethod
    def validate(self, *args):
        pass
