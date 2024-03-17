from django.utils.translation import ugettext as _

from ekichabi.models import Business, Category, District, Subvillage, Village

from .base_screens.InputScreen import InputScreen
from .Buttons import back_button
from .MenuHierarchyScreenLazy import MenuHierarchyScreenLazy


# Allows the user to type the part of the location and then returns a list of matches
# -return detailed bussiness screen if only one matching bussiness exists
# -return the screen ask the user if they need filter by location if mutiple matching businesses exist
class FirstCharsLocationScreen(InputScreen):
    exception_type = (LookupError)

    error_msg = 'Your query could not be found.\n' +\
        'Please check spelling and try again, ' +\
        'or enter %s to go back.'

    body = 'Type the first part of a %(type)s:'

    def __init__(self, search_type, submenus=True, body=False, ommitfromhistory=False):
        """ submenus==False will send the user straight to a list of businesses """
        self.search_type = search_type
        self.body = body or _(self.body) % {'type': _(search_type.__name__)}
        self.error_msg = _(self.error_msg) % back_button
        self.submenus = submenus
        self.ommitfromhistory = ommitfromhistory
        self.again = bool(body)

    def next_filters(self):
        search_order = [District, Village, Subvillage, Category]

        if not self.submenus or self.search_type not in search_order:
            return []
        else:
            pos = search_order.index(self.search_type)
            return search_order[pos+1:]

    def one_result(self, result):

        # trying to do this with the least hard-coding of location possible
        kwarg = {self.search_type.__name__.lower(): result}
        matching_businesses = Business.objects.filter(**kwarg)
        if matching_businesses.count() > 100:
            return FirstCharsLocationScreen(self.search_type, submenus=self.submenus, body=_(
                "Too many results (%i). Enter %s to go back or enter a longer query") % (matching_businesses.count(), back_button), ommitfromhistory=True)
        return MenuHierarchyScreenLazy(self.next_filters(), query_set=matching_businesses)

    def many_results(self, results):
        # TODO: better to use QuerySet.union() here, but needs Django 1.11
        kwarg = {self.search_type.__name__.lower(): results[0]}
        matching_businesses = Business.objects.filter(**kwarg)
        for result in results[1:]:
            kwarg = {self.search_type.__name__.lower(): result}
            new_businesses = Business.objects.filter(**kwarg)
            matching_businesses = matching_businesses | new_businesses
        if matching_businesses.count() > 100:
            return FirstCharsLocationScreen(self.search_type, submenus=self.submenus, body=_(
                "Too many results (%i). Enter %s to go back or enter a longer query") % (matching_businesses.count(), back_button), ommitfromhistory=True)
        return MenuHierarchyScreenLazy([self.search_type] + self.next_filters(),
                                       matching_businesses.distinct())
        # QuerySet.distinct() removes dupes

    def action(self, input, session, context):
        results = self.search_type.objects.filter(name__icontains=input)

        if results.count() == 0:
            return FirstCharsLocationScreen(self.search_type, submenus=self.submenus, body=_("No businesses were found matching your search. Enter a new search, or press %s to go back.") % back_button, ommitfromhistory=True)
        elif results.count() == 1:
            return self.one_result(results[0])
        else:
            return self.many_results(results)

    def note(self): 
        return "FirstCharsLocationScreen - searching for {}".format(self.search_type.__name__) + (" again" if self.again else "")
