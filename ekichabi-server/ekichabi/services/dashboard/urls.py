from django.contrib.staticfiles.storage import staticfiles_storage
from django.urls import path
from django.views.generic.base import RedirectView

from .views import DashboardView

urlpatterns = [
    path('dashboard', DashboardView.as_view()),
    path('dashboard/download_pdf', DashboardView.download_pdf, name='download_pdf'),
    path('favicon.ico', RedirectView.as_view(url=staticfiles_storage.url('favicon.ico'))),
]