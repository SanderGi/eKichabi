from django.contrib.staticfiles.storage import staticfiles_storage
from django.urls import path
from django.views.generic.base import RedirectView

from .views import UssdSimulatorView

urlpatterns = [
    path('', UssdSimulatorView.as_view()),  # main entry of the simulator
    path('favicon.ico', RedirectView.as_view(url=staticfiles_storage.url('favicon.ico'))),
]