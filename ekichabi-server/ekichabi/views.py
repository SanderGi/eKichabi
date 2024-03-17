import abc

from django.conf import settings
from django.http import HttpResponse
from django.utils.decorators import method_decorator
from django.views.decorators.csrf import csrf_exempt
from django.views.generic import View

from ekichabi.helper.GetApp import get_app
from ekichabi.helper.Sessions import Session
from ekichabi.whitelist.WhitelistManager import check_whitelisted, get_permission_denied_msg


class NiafikraRequest():
    def __init__(self, session_id, phone_number, user_input):
        self.session_id = session_id
        self.phone_number = phone_number
        self.user_input = user_input

    def send_back_response(self, screen_content, has_next):
        response = HttpResponse(screen_content)
        if has_next is True:
            response['Session'] = 'C'
        else:
            response['Session'] = 'Q'
        return response

    @classmethod
    def from_request(cls, request):
        session_id = session_id = request.GET.get('sessionid')
        phone_number = request.GET.get('msisdn')
        user_input = cls.sanitize_input(request.GET.get('input', ''))
        return cls(session_id, phone_number, user_input)

    @classmethod
    def sanitize_input(cls, input):
        # remove all the unnessary special characters are added by either Niafikra or the user
        user_input = input.strip('*# ')

        # Strips away the service code if it is set by Niafikra
        service_code = getattr(settings, 'NIAFIKRA_SERVICE_CODE', '')
        if user_input.startswith(service_code):
            user_input = user_input[len(service_code):]

        return user_input


@method_decorator(csrf_exempt, name='dispatch')
class UssdDriver(View):
    __metaclass__ = abc.ABCMeta
    start_app = get_app()
    permission_denied_msg = get_permission_denied_msg()

    def get(self, request):

        # converts requests to Niafikra format
        niafikra_request = NiafikraRequest.from_request(request)

        # first connection for the session
        if Session.is_new_session(niafikra_request.session_id):
            # reject non whitelisted phone numbers
            if not check_whitelisted(niafikra_request.phone_number):
                return niafikra_request.send_back_response(self.permission_denied_msg, False)

        session = Session.get_session(niafikra_request, self.start_app)
        has_ended = session.process_user_input(niafikra_request.user_input)
        result = niafikra_request.send_back_response(
            session.render_screen_content(), not has_ended)
        session.save_session()
        return result
