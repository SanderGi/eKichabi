
import abc

from ekichabi.helper.Utils import abstract_attribute


class UssdHttpBase(object):
    """ Base class to define the USSD HTTP Python Object

        Attributes:

            session_id (int,str): USSD session id for lookup in Session Cache
            service_code (str): USSD call code
            phone_number (str): phonenumber initiating USSD session

            input (str): last text sent over session
    """

    __metaclass__ = abc.ABCMeta

    session_id = abstract_attribute()
    service_code = abstract_attribute()
    phone_number = abstract_attribute()
    commands = abstract_attribute()
    input = abstract_attribute()

    INPUT_APPEND = True

    def __init__(self, session_id, service_code, phone_number, text):
        """ Create a USSD ojbect from variables from an HTTP session"""
        self.session_id = session_id
        self.service_code = service_code
        self.phone_number = phone_number
        self.text = text

        self.clean()
        self.set_commands_and_input()

    def clean(self):
        """ Overide this function to clean data on creation """
        pass

    def set_commands_and_input(self):
        """ Split text on * and set input to last command """
        self.commands = self.text.split('*')
        if self.commands == ['']:
            self.commands = []
            self.input = ''
        else:
            self.input = self.commands[-1]

    @abc.abstractmethod
    def send(self, text, has_next=False):
        """ Send a ussd screen over transport """
        pass

    def __len__(self):
        return len(self.commands)

    def __str__(self):
        return "{0.service_code} on {0.phone_number} id {0.session_id}".format(self)


class con_end_mixin(object):
    """ mixin for CON/END USSD syntax
         - driver subclass should overide request
    """

    def send(self, text, has_next=False):
        """ Send a ussd screen over transport """
        if has_next:
            return self.response('CON', text)
        else:
            return self.response('END', text)

    def response(self, prefix, text):
        return u'{0} {1}'.format(prefix, text)
