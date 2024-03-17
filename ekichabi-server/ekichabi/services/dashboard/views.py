import json
from django.http import HttpResponse
from django.shortcuts import render
from django.views.generic import View
from glob import glob
from django.core.management import execute_from_command_line
from django.conf import settings
from django.utils.decorators import method_decorator
from django.views.decorators.csrf import csrf_exempt

redis = settings.REDIS
from zipfile import ZipFile
from weasyprint import HTML

# from django.db import connection
# cursor = connection.cursor()


@method_decorator(csrf_exempt, name="dispatch")
class DashboardView(View):
    ussdfiles = [
        path for path in glob(settings.NIAFIKRA_LOG_DIR + "/*_*-*-*-*.*.*.log")
    ]
    ussdArchive = settings.PROJECT_DIR + "/logsArchive/all_ussd.zip"
    androidfiles = [path for path in glob(settings.NIAFIKRA_LOG_DIR + "/Android_*.log")]

    def download_pdf(request):
        with open(settings.PROJECT_DIR + "/data/context.json", "r") as file:
            context = json.load(file)
        html_template = render(request, "dashboard.html", context=context)
        pdf_file = HTML(string=html_template.content.decode("utf-8")).write_pdf()
        response = HttpResponse(pdf_file, content_type="application/pdf")
        response["Content-Disposition"] = 'attachment; filename="yourFileName.pdf"'

        return response

    def get(self, request):
        with open(settings.PROJECT_DIR + "/data/context.json", "r") as file:
            context = json.load(file)
        return render(request, "dashboard.html", context=context)

    def post(self, request):
        self.ussdfiles = [
            path for path in glob(settings.NIAFIKRA_LOG_DIR + "/*_*-*-*-*.*.*.log")
        ]
        self.androidfiles = [
            path for path in glob(settings.NIAFIKRA_LOG_DIR + "/Android_*.log")
        ]
        with ZipFile(self.ussdArchive, "r") as archive:
            password = request.headers.get("password")
            if password == "SECRET":
                resource = str(request.headers.get("resource"))
                if resource.startswith("file"):
                    path = resource[4:]
                    if path in self.ussdfiles or path in self.androidfiles:
                        if path in self.androidfiles:
                            execute_from_command_line(
                                ["manage.py", "decode_android_logs", path]
                            )
                            path += "_decoded"
                        text_file = open(path, "r")
                        data = text_file.read()
                        text_file.close()
                        return HttpResponse(json.dumps(data), status=200)
                    elif path in archive.namelist():
                        data = (
                            str(archive.read(path))[2:-1]
                            .replace("\\n", "\n")
                            .replace("\\t", "\t")
                        )
                        return HttpResponse(json.dumps(data), status=200)
                    else:
                        return HttpResponse("Access to resource denied", status=403)
                else:
                    return HttpResponse("Resource not found", status=404)
            else:
                return HttpResponse("Wrong password", status=403)
