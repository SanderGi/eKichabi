from django.utils.translation import ugettext as _

from ekichabi.models import Business

from .base_screens.LongMenuScreen import LongMenuScreen
from .BusinessDetailsScreen import BusinessDetailsScreen
from .Buttons import back_button

"""
    Takes a list of model Objects and uses the order of this list to define the browsing
    tree. For example: The list [Village, Category] would first prompt the user to select
    a village, and then select a category from set of categories spanned by the selected
    village, and then the user would select from a list of businesses. The first menu item
    permits the user to bypass the remainder of the hierarchy and jump straight to the list
    of businesses. Uses BusinessDetailsScreen to display business results. """

class MenuHierarchyScreen(LongMenuScreen):

    def __init__(self, filters=[], businesses=Business.objects.all()):

        # we need to refine our query set futher when there are additional filters to apply
        if len(filters) > 0:

            # retreive a set of filter-related values
            processed_filters = self.process_filters(filters)
            self.current_filter = processed_filters["current_filter"]
            translated_filter = processed_filters["translated_filter"]
            next_filters = processed_filters["next_filters"]

            # prompts for user to select a new business category based on the current filtering option
            select_category_prompt = _("Select a %(translated_filter)s:") % {
                'translated_filter': translated_filter}

            # group businesses by a new category (the current filter)
            business_categories = set()
            for business in businesses:
                business_categories.add(getattr(business, self.current_filter))

            # store all entries user can click on from the current screen
            menu_entries = []

            # loop through each distinct business category
            for business_category in business_categories:

                category_name = str(business_category)

                businesses_from_catgory = businesses.filter(
                    **{self.current_filter: business_category})

                # if there are more than one more business, render another hierarchy screen for further filtering
                if businesses_from_catgory.count() > 1:
                    next_screen = MenuHierarchyScreen(
                        next_filters, businesses_from_catgory)
                else:
                    # otherwise, if there is one business left, directly render the business screen
                    only_business = businesses_from_catgory[0]
                    next_screen = BusinessDetailsScreen(only_business)

                # append the entry to the overall list of entries
                menu_entries.append((category_name.capitalize(), next_screen))

            # reoder the menu entries based on existing configurations
            self.reorder_menu_entries(menu_entries)

            # if there's a single entry, directly skip to the next screen
            if self.need_skip_to_next_screen(menu_entries):
                self.skipTo("1")

            # otherwise, give user the option to directly list all the businesses depending on how many there are
            if not self.need_skip_to_next_screen(menu_entries) and self.show_all_business_option(businesses):
                self.append_show_business_entry(
                    businesses, menu_entries, select_category_prompt)

            # construct the menu title based on previous configurations
            menu_title = self.construct_menu_title(
                businesses, select_category_prompt)

            super().__init__(title=menu_title, items=menu_entries)

        # otherwise, there's no more filters to apply. Display all the business.
        else:

            title = _("Select a Business:")

            self.current_filter = "business"

            items = list(
                map(lambda b: (b.name, BusinessDetailsScreen(b)), businesses))
            items.sort(key=lambda i: i[0])

            # TODO: in the future, this logic should belong to the LongMenuScreen, rather than manually putting this here
            for i in range(0, len(items) - 1):
                items[i][1].next_screen = items[i + 1][1]
                items[i][1].menu = self

            if items:
                index = items[len(items) - 1][1].body.rfind("\n")
                items[len(items) - 1][1].body = items[len(items) -
                                                      1][1].body[:index] + "\n" + _("%s. Back") % back_button
                items[len(items) - 1][1].menu = self

            super().__init__(title=title, items=items)

    def note(
        self): return "MenuHierarchyScreen - selecting a {}".format(self.current_filter)

    @ property
    def error_msg(self):
        return _("Your input is not a valid menu item. Please try again.")

    def process_filters(self, filters):
        current_filter = filters[0].__name__.lower()
        if current_filter == "subsector":
            current_filter = "subsector1"
        return {
            "current_filter": current_filter,
            "translated_filter": filters[0].translated_name(),
            "next_filters": filters[1:]
        }

    def reorder_menu_entries(self, menu_entries):

        # sort the menu entries based on the menu name, in lexographical odering
        menu_entries.sort(key=lambda i: i[0])

        # if any of menu entry has no name, categorize the entry as "Others" and append it at the very end
        for menu_entry in menu_entries:
            if len(menu_entry[0]) == 0:
                empty_menu_option = (_('Others'), menu_entry[1])
                menu_entries.remove(menu_entry)
                menu_entries.append(empty_menu_option)
                break

    def append_show_business_entry(self, query_set, menu_entries, select_prompt):
        show_business_prompt = _("All businesses (%(business_count)s),\nor ") % {
            'business_count': query_set.count()} + select_prompt
        menu_entries.insert(
            0, (show_business_prompt, MenuHierarchyScreen([], query_set)))

    def need_skip_to_next_screen(self, menu_entries):
        return len(menu_entries) == 1

    def show_all_business_option(self, query_set):
        return query_set.count() < 100

    def construct_menu_title(self, query_set, select_prompt):
        return select_prompt if not self.show_all_business_option(query_set) else ""
