from django.utils.translation import ugettext as _

from ekichabi.helper.Search import (getKeysByCategory, getQuerySetByCategory)
from ekichabi.models import Business

from .base_screens.InputScreen import InputScreen
from .BusinessDetailsScreen import BusinessDetailsScreen
from .Buttons import back_button
from .IfFilterByLocationScreen import IfFilterByLocationScreen
from .KeywordSelectScreen import KeywordSelectScreen

# Allows the user to type the part of the Model provided in __init__
# and then returns a list of matches including Product/Service, Business, Owner(Search for location has its own screen)
# For Bussiness and Owner search
# -return detailed bussiness screen if only one matching bussiness exists
# -return the screen ask the user if they need filter by location if mutiple matching businesses exist
# For Product/Service search, search through categoies and subsectors to find matching type
# -return the screen ask the user if they need filter by location when only one matching type exists
# -return the screen that list all matching types that the user can choose

translations = {
		'Kukodi au Kibarua' : 'Hiring and Labor',
		'Sekta ya Kifedha' : 'Financial Services',
		'Sekta Isiyo ya Kilimo' : 'Non-Agri Services',
		'Usafirishaji' : 'Transport',
		'Kuongezea Thamani Bidhaa za Kilimo' : 'Agricultural processing',
		'Wafanyabiashara wa Rejareja' : 'Merchant/retail',
		'Wafanyabiashara wa Jumla' : 'Trading and wholesale',
		'Fundi' : 'Repairs',
		'Huduma' : "Services",
		'Wafanyabiashara wenye Ujuzi': "Skilled Trades",
	}

class FirstCharsScreen(InputScreen):
    """ Allows the user to type the part of the Model provided in __init__
        and then returns a list of matches including Product/Service, Business, Owner(Search for location
        has its own screen"""
    exception_type = (LookupError)

    error_msg = 'Your query could not be found.\n' +\
        'Please check spelling and try again, ' +\
        'or enter %s to go back.'

    body = 'Type the first part of a %(type)s:'

    def __init__(self, search_type, submenus=True, body=False, ommitfromhistory=False):
        """ submenus==False will send the user straight to a list of businesses """
        self.search_type = search_type
        self.body = body or _(self.body) % {'type': _("Agricultural Product" if search_type == "Input" else search_type)}
        self.error_msg = _(self.error_msg) % back_button
        self.submenus = submenus
        self.ommitfromhistory = ommitfromhistory
        self.again = bool(body)

    def no_results_screen(self):
        return FirstCharsScreen(self.search_type, submenus=self.submenus, body=_("No businesses were found matching your search. Enter a new search, or press %s to go back.") % back_button, ommitfromhistory=True)

    def one_results_screen(self, results):
        return BusinessDetailsScreen(results[0])

    def many_results_screen(self, results):
        return IfFilterByLocationScreen(query_set=results, rand100=self.rand100)

    def too_many_results_screen(self, results):
        return FirstCharsScreen(self.search_type, submenus=self.submenus, body=_("Too many results (%i). Enter %s to go back or enter a longer query") % (len(results), back_button), ommitfromhistory=True)

    def product_screen(self, input, session):
        keys = getKeysByCategory(input, self.search_type) or self.no_results_screen()
        def screen(key):
            results = getQuerySetByCategory(self.search_type, key)
            if results.count() == 1:
                return self.one_results_screen(results)
            else:
                return self.many_results_screen(results)
        return KeywordSelectScreen(_(self.search_type), keys, screen, is_lazy=False)

    def saved_search_screen(self, input, session, screen_fn):
        sessionkey = input + 'search'
        if sessionkey in session.data:
            screen = session.data[sessionkey]
        else:
            screen = screen_fn(input, session)
            session.data = {} # clear session data, could leave this out for more caching at the expense of more memory usage in session
            session.data[sessionkey] = screen
        return screen

    # Ivy changed this
    def action(self, input, session, context):
        input = translations.get(input, input)
        self.rand100 = session.rand100
        if self.search_type == "Business" or self.search_type == "Owner":
            def screen(input, session):
                if self.search_type == "Business":
                    results = Business.objects.filter(name__icontains=input)
                else:
                    results = Business.objects.filter(owner__icontains=input)
                if len(results) > 100:
                    return self.too_many_results_screen(results)
                if results.count() == 0:
                    return self.no_results_screen()
                elif results.count() == 1:
                    return self.one_results_screen(results)
                else:
                    return self.many_results_screen(results)
            return self.saved_search_screen(input, session, screen)
        if self.search_type in ["Crop", "Livestock", "Specialty", "Input"]:
            return self.saved_search_screen(input, session, self.product_screen)

    def note(self): 
        return "FirstCharsScreen - searching for {}".format(self.search_type) + (" again" if self.again else "")
