from django.urls import path

from .views import businessView, dateView, permissionView, trackingView

urlpatterns = [
    path('business/', businessView, name="business/"),
    path('date/', dateView, name="date/"),
    path('permission/', permissionView, name="permission/"),
    path('tracking/', trackingView, name="tracking/")
]
