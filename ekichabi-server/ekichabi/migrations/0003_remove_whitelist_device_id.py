# Generated by Django 3.2.10 on 2022-11-02 01:54

from django.db import migrations


class Migration(migrations.Migration):

    dependencies = [
        ('ekichabi', '0002_whitelist'),
    ]

    operations = [
        migrations.RemoveField(
            model_name='whitelist',
            name='device_id',
        ),
    ]
