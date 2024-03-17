import os

from django.utils.translation import gettext_lazy as _
from redislite import StrictRedis

# the setting is conditioned based on whether it is in deployment
# or development. See comment for each section below
production_env = os.getenv("PRODUCTION_ENV") == "true"
generate_screens = os.getenv("GENERATE_SCREENS") == "true"


# Project directories
PROJECT_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
EKICHABI_DIR = os.path.join(PROJECT_DIR, "ekichabi")


# Other important directories
CONFIG_DIR = os.path.join(PROJECT_DIR, "config")
DATA_DIR = os.path.join(PROJECT_DIR, "data")
SIMILATOR_DIR = os.path.join(EKICHABI_DIR, "services/simulator")
DATA_PATH = os.path.join(DATA_DIR, "census_data_trimmed.csv")
SAVED_SCREENS_PATH = os.path.join(DATA_DIR, "saved_screens.pbz2")
REDIS_PATH = os.path.join(PROJECT_DIR, "redis/cache.rdb")
DASHBOARD_PATH = os.path.join(EKICHABI_DIR, "services/dashboard")


# for internalization
LOCALE_PATHS = [os.path.join(PROJECT_DIR, "locale")]

# SECURITY WARNING: don't run with debug turned on in production!
DEBUG = not production_env

ALLOWED_HOSTS = ["*"]

# Application definition
INSTALLED_APPS = [
    "silk",
    "django.contrib.admin",
    "django.contrib.auth",
    "django.contrib.contenttypes",
    "django.contrib.messages",
    "django.contrib.staticfiles",
    "ekichabi.apps.EkichabiConfig",
]

# Development apps -- not for deployment
if not production_env:
    INSTALLED_APPS += [
        "tests.integration",  # running integration tests to ensure correctness
    ]

# Middleware section
MIDDLEWARE = [
    "django.middleware.security.SecurityMiddleware",
    "django.middleware.locale.LocaleMiddleware",
    "django.middleware.common.CommonMiddleware",
    "django.middleware.csrf.CsrfViewMiddleware",
    "django.contrib.sessions.middleware.SessionMiddleware",
    "django.contrib.auth.middleware.AuthenticationMiddleware",
    "django.contrib.messages.middleware.MessageMiddleware",
    "django.middleware.clickjacking.XFrameOptionsMiddleware",
    "silk.middleware.SilkyMiddleware",  # performance measurement
]

# Silky is a live profiling and inspection tool for the Django framework
# 10% of requests are collected for performance measurement
SILKY_INTERCEPT_PERCENT = 10
if not production_env:
    # development specific performance mearsurements (more detailed)
    SILKY_PYTHON_PROFILER = True  # profile functions
    SILKY_DYNAMIC_PROFILING = [
        {
            "module": "ekichabi.views",
            "function": "UssdDriver.get",
            "name": "handle ussd request",
        }
    ]
    SILKY_INTERCEPT_PERCENT = 100
    SILKY_META = True  # see what impact silky has

# Main urls.py file for routing is config/urls.py
ROOT_URLCONF = "config.urls"

# Loading templates
TEMPLATES = [
    {
        "BACKEND": "django.template.backends.django.DjangoTemplates",
        "DIRS": [os.path.join(EKICHABI_DIR, "services/static")],
        "APP_DIRS": True,
        "OPTIONS": {
            "context_processors": [
                "django.template.context_processors.debug",
                "django.template.context_processors.request",
                "django.contrib.auth.context_processors.auth",
                "django.contrib.messages.context_processors.messages",
            ],
        },
    },
]

# The Django app is a WSGI application
WSGI_APPLICATION = "config.wsgi.application"

# Setting up database connection based on development / deployment
DATABASES = (
    {
        "default": {
            "ENGINE": "django.db.backends.mysql",
            "NAME": "SECRET",
            "USER": "SECRET",
            "PASSWORD": "SECRET",
            "HOST": "SECRET",
            "PORT": "3306",
            "MAX_ALLOWED_PACKET": "32M",
        }
    }
    if production_env
    else {
        "default": {
            "ENGINE": "django.db.backends.mysql",
            "NAME": "SECRET",
            "USER": "SECRET",
            "PASSWORD": "SECRET",
            "HOST": "SECRET",
            "PORT": "3306",
        }
    }
)

# models.py default
DEFAULT_AUTO_FIELD = "django.db.models.AutoField"

# Password validation
# https://docs.djangoproject.com/en/3.2/ref/settings/#auth-password-validators
AUTH_PASSWORD_VALIDATORS = [
    {
        "NAME": "django.contrib.auth.password_validation.UserAttributeSimilarityValidator",
    },
    {
        "NAME": "django.contrib.auth.password_validation.MinimumLengthValidator",
    },
    {
        "NAME": "django.contrib.auth.password_validation.CommonPasswordValidator",
    },
    {
        "NAME": "django.contrib.auth.password_validation.NumericPasswordValidator",
    },
]

# Using redis as cache, specifically redislite, as it is the only supported in-memory cache for PythonAnywhere
# Issue on choice of redislite: https://www.pythonanywhere.com/forums/topic/1594/
# Doc on django-redis: https://github.com/jazzband/django-redis
REDIS = StrictRedis(REDIS_PATH)

CRON_CLASSES = [
    "ekichabi.cron.MyCronJob",
]

# Internationalization
# https://docs.djangoproject.com/en/3.2/topics/i18n/
LANGUAGES = [
    ("sw", _("Swahili")),
    ("en", _("English")),
]

# Language for the Niafikra app (sw or en dependent on the environment)
NIAFIKRA_LANG = (
    "sw" if production_env or generate_screens else "sw"
)  # set language here
LANGUAGE_CODE = NIAFIKRA_LANG


# Time zone and other settings
TIME_ZONE = "Africa/Nairobi"
USE_I18N = True  # set this true for translations
USE_L10N = True
USE_TZ = True

# Static files (CSS, JavaScript, Images)
# https://docs.djangoproject.com/en/3.2/howto/static-files/
STATIC_URL = "/static/"
STATICFILES_DIRS = (
    os.path.join(SIMILATOR_DIR, "static"),
    os.path.join(DASHBOARD_PATH, "static"),
)
STATIC_ROOT = os.path.join(EKICHABI_DIR, "services/static")

# Niafikra and Kichabi settings
NIAFIKRA_SERVICE_CODE = "SECRET"
NIAFIKRA_LOG_DIR = os.path.join(PROJECT_DIR, "logs")
USSD_TRANSPORT = "Niafikra"

# DEPLOYMENT WARNING: This should not be true in deployment
# set to true to generate screens even when the file already exists
OVERWRITE_SCREENS = generate_screens and not production_env
