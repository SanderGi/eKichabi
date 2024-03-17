import json

import requests
from django.http import HttpResponse
from django.shortcuts import render
from django.utils.decorators import method_decorator
from django.views.decorators.csrf import csrf_exempt
from django.views.generic import View


@method_decorator(csrf_exempt, name="dispatch")
class UssdSimulatorView(View):

    def get(self, request):
        return render(request, "index.html", context={"transport": "Niafikra"})

    def post(self, request):
        data = json.loads(request.body)
        url = data.get("url")

        payload = {
            "sessionid": data.get("sessionId", ""),
            "msisdn": data.get("phoneNumber", ""),
            "input": data.get("text", ""),
        }

        response = requests.get(url, params=payload)
        action = "con" if response.headers.get("Session", "Q") == "C" else "end"
        text = response.text
        return HttpResponse(json.dumps({"action": action, "text": text}))
