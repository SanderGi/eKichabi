from django.utils.translation import ugettext as _

from ekichabi.models import Business

from .base_screens.LongMenuScreen import LongMenuScreen
from .BusinessDetailsScreen import BusinessDetailsScreen
from .Buttons import back_button

""" hopefully a more elegant solution to the complex screen classes
           which complicate defining new views.
           Takes a list of model Objects and uses the order of this list
           to define the browsing tree. For example:
           The list [Village, Category] would first prompt the user to select
           a village, and then select a category from set of categories spanned
           by the selected village, and then the user would select from a list of
           businesses.
           The first menu item permits the user to bypass the remainder of the
           hierarchy and jump straight to the list of businesses.
           Uses BusinessDetailsScreen to display business results. """


class MenuHierarchyScreen(LongMenuScreen):

    def __init__(self, filters=[], query_set=Business.objects.order_by('number1').all(), selected=""): #NOTE: Be very careful when touching the logic of this class. Everything needs to be deterministic so that the screens can be saved correctly. Querysets are weird when it comes to this!!

        self.overflow_str = _('next, %s. back') % back_button
        self.back_str = _("%s. Back") % back_button

        if len(filters) > 0:
            # we need to refine our query_set futher

            # what we are filtering on
            field_name = filters[0].__name__.lower()
            if field_name == "subsector":
                field_name = "subsector1"

            self.filter_name = field_name
            title = _("Select a %(filter)s:") % {
                'filter': filters[0].translated_name()}

            # TODO: would be nice to have this include already applied filters

            # remaining filters
            self.next_filters = filters[1:]

            # now we need to get the filters spanned by our QuerySet
            # TODO: this seems relatively inefficient, is there no built-in library do group by?
            filter_vals = set()
            for item in query_set:
                # get the fields from each business whose name is our filter
                filter_vals.add(getattr(item, field_name))

            # Ivy changed this
            # When only one matching result let the next screen be buiness detail screen
            items = []
            for item in filter_vals:
                name = str(item)
                new_query = query_set.filter(**{field_name: item}).order_by('number1')
                new_selected = selected + name + ","
                if new_query.count() > 1:
                    screen = MenuHierarchyScreen(
                        self.next_filters, new_query.all(), new_selected)
                else:
                    screen = BusinessDetailsScreen(new_query[0], False)

                items.append((name.lower().capitalize(), screen))

            items.sort(key=lambda i: i[0])

            # If any item name is NaN (due to the weird formatting of the csv data), then we categorize them as others
            for item in items:
                if len(item[0]) == 0:
                    others = (_('Others'), item[1])
                    items.remove(item)
                    items.append(others)
                    break

            if len(items) == 1:
                self.skipTo("1")

            # now offer the possiblity to jump straight to all items
            if len(query_set) < 100 and len(items) > 1:
                see_all = _("All businesses (%(num_biz)s),\nor ") % {
                    'num_biz': len(query_set)}

                # swap the order of the title and the first menu item
                see_all += title
                title = ''

                items = [(see_all, MenuHierarchyScreen([], query_set))] + items

            super().__init__(title=title, items=items, overflow_str=self.overflow_str, back_str=self.back_str)

        else:
            # we've got our businesses, now just need to display them
            title = _("Select a Business:")

            query_set = sorted(query_set.order_by('name'), key=lambda b: b.name)
            items = [(b.name, BusinessDetailsScreen(b, i < len(query_set) - 1)) for i, b in enumerate(query_set)]

            # ivy changed this: add next screen for final business screen
            for i in range(len(items) - 1):
                items[i][1].next_screen = items[i + 1][1]
                # items[i][1].menu = self

            # if items:
            #     index = items[len(items) - 1][1].body.rfind("\n")
            #     items[len(items) - 1][1].body = items[len(items) -
            #                                           1][1].body[:index] + "\n" + _("%s. Back") % back_button
                # items[len(items) - 1][1].menu = self

            self.filter_name = "business"

            super().__init__(title=title, items=items, overflow_str=self.overflow_str, back_str=self.back_str)

    def note(self): 
        return "MenuHierarchyScreen - selecting a {}".format(self.filter_name)

    @property
    def error_msg(self):
        return _("Your input is not a valid menu item. Please try again.")
