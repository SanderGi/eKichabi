import csv

from ekichabi.models import Business

with open('biz.csv', 'w') as output_f:
    writer = csv.writer(output_f)
    writer.writerow(('name', 'village', 'subvillage'))

    for Business in Business.objects.all():
        writer.writerow((str(Business.name), str(
            Business.village), Business.subvillage.name))
