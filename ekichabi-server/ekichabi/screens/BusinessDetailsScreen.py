from django.utils.translation import ugettext as _

from ekichabi.models import Phone
from ekichabi.helper.Utils import bcolors
from ekichabi.whitelist.ABGroupManager import get_or_set_test_group

from .base_screens.BaseScreen import BaseScreen
from .Buttons import back_button, forward_button
from .LookupErrorScreen import LookupErrorScreen

import re

"""Displays the details of a given business and display disclaimerScreen when
the user use if for first time"""


class BusinessDetailsScreen(BaseScreen):

    def __init__(self, business, has_next=True):
        # for logging note
        self.business_name = business.name
        self.business = business
        self._has_next = has_next

        # get businesss info into dict for formatting convenience
        field_names = {
            'name': business.name,
            'category': business.category,

            'location': str(business.district) + '-' + str(business.subvillage).replace(' - ','-'), # district - village - subvillage

            'keywords': business.description(),

            'number': business.number1,

            # ivy changed this: add owner
            'owner': business.owner,

            'line': 2 * "-"
        }

        body = _('%(name)s\n' +
                 '%(line)s\n' +
                 '%(keywords)s\n' +\
                 # ivy changed this add owner
                 'Owner: %(owner)s\n' +\
                 'Located in: %(location)s\n' +\
                 'Phone: %(number)s')

        button = ('%s. ') % forward_button + _('next, %s. back') % back_button if self._has_next else _('%s. back') % back_button
        cutoff = 160 - len(button) - 1

        # handling too long business screens by only doing the necessary abbreviations:
        if len(body % field_names) > cutoff:
            for key in field_names:
                field_names[key] = re.sub('\s+', ' ', str(field_names[key])) # remove duplicate spaces
            field_names['location'] = re.sub('\s*-\s*', '-', field_names['location'])
            if len(body % field_names) > cutoff:
                self._no_line = True
                body = "\n".join([s for s in body.split('\n') if s != '%(line)s']) # remove the line
            if len(body % field_names) > cutoff:
                field_names['keywords'] = re.sub('\s*\)\s*','',re.sub('\s*[\(,]\s*','/',str(field_names['keywords']))) # replace commas and parentheses by /
            if len(body % field_names) > cutoff:
                button = re.sub('\.\s+',')', button).replace(', ', ' ') # remove unecessary space from button
                cutoff = 160 - len(button) - 1
            if len(body % field_names) > cutoff:
                body = body.replace(': ', ':') # remove unnecessary syntax whitespace
            if len(body % field_names) > cutoff:
                if self.has_next:
                    button = _('<-%s') % back_button + ', ' + ('%s->') % forward_button # further simplify button
                cutoff = 160 - len(button) - 1
            parts = str(business.subvillage).split(' - ')
            if len(body % field_names) > cutoff and parts[0] == parts[1]:
                field_names['location'] = str(business.district) + '-' + parts[0] # remove subvillage if it's the same as the village
            if len(body % field_names) > cutoff and len(field_names['keywords']) > 33:
                field_names['keywords'] = field_names['keywords'][:max(30, len(body % field_names) - cutoff - 3)].strip() + '...' # cut keywords down to at most 30 characters + ... (whatever is necessary)

        self.body = body % field_names + "\n" + button

        if len(self.body) > 160:
            print(bcolors.WARNING + 'This BusinessDetailsScreen is > 160 characters:\n' + bcolors.ENDC + self.body)

    def input(self, input, session, context):
        if input == forward_button and self.next_screen:
            return self.next_screen
        else:
            return LookupErrorScreen()

    def render(self, session, context):
        info = session.session_info()
        phone_number = info["number"]
        result = Phone.objects.filter(name=phone_number)
        if result.count() == 1:
            if get_or_set_test_group(phone_number) == 'B': # B group doesn't get to see owner
                parts = self.body.split('\n')
                ownerline = 2 if getattr(self, '_no_line', False) else 3
                parts = parts[:ownerline] + parts[ownerline+1:]
                return '\n'.join(parts)
            else:
                return self.body
        else: # add disclaimer screen
            phone = Phone(name=phone_number)
            phone.save()
            temp = self.next_screen
            self.next_screen = BusinessDetailsScreen(self.business, has_next=self._has_next)
            self.next_screen.next_screen = temp
            title = _('All businesses have been visited in person to collect contact information, but please use discretion when transacting with unknown individuals')
            button = ('%s. ') % forward_button + _('next')
            return title + "\n" + button

    def note(self):
        return "BusinessDetailsScreen - for {}".format(self.business_name)
