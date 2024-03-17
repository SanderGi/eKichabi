import os

from django.contrib import admin
from django.urls import include, path

import ekichabi.services.android.urls as android_endpoints
import ekichabi.services.simulator.urls as simulator_endpoints
import ekichabi.services.dashboard.urls as dashboard_endpoints

# initialize the root url patterns
urlpatterns = []

# if we are running migration, the ekichabi endpoint should not be included
# otherwise, we include the endpoint
if (os.environ.get('MIGRATION') != 'true'):
    from ekichabi.views import UssdDriver
    urlpatterns.append(path('ussd/', UssdDriver.as_view()))

# extend all of the service endpoints
urlpatterns.extend(android_endpoints.urlpatterns)
urlpatterns.extend(simulator_endpoints.urlpatterns)
urlpatterns.extend(dashboard_endpoints.urlpatterns)

# finally, extend any utility endpoints
utilitypatterns = [
    path('admin/', admin.site.urls),  # admin
    path('silk/', include('silk.urls', namespace='silk')),  # performance
]
urlpatterns.extend(utilitypatterns)
