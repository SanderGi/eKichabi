# Generated by Django 3.2.10 on 2022-11-12 22:02

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('ekichabi', '0004_auto_20221104_0918'),
    ]

    operations = [
        migrations.RemoveField(
            model_name='business',
            name='livestock2',
        ),
        migrations.RemoveField(
            model_name='business',
            name='livestock3',
        ),
        migrations.AddField(
            model_name='business',
            name='input1',
            field=models.CharField(blank=True, max_length=50, null=True),
        ),
        migrations.AddField(
            model_name='business',
            name='input2',
            field=models.CharField(blank=True, max_length=50, null=True),
        ),
        migrations.AddField(
            model_name='business',
            name='input3',
            field=models.CharField(blank=True, max_length=50, null=True),
        ),
        migrations.AddField(
            model_name='business',
            name='specialty1',
            field=models.CharField(blank=True, max_length=50, null=True),
        ),
        migrations.AddField(
            model_name='business',
            name='specialty2',
            field=models.CharField(blank=True, max_length=50, null=True),
        ),
        migrations.AddField(
            model_name='business',
            name='specialty3',
            field=models.CharField(blank=True, max_length=50, null=True),
        ),
        migrations.AddField(
            model_name='business',
            name='specialty4',
            field=models.CharField(blank=True, max_length=50, null=True),
        ),
        migrations.AlterField(
            model_name='business',
            name='crop1',
            field=models.CharField(blank=True, max_length=50, null=True),
        ),
        migrations.AlterField(
            model_name='business',
            name='crop2',
            field=models.CharField(blank=True, max_length=50, null=True),
        ),
        migrations.AlterField(
            model_name='business',
            name='crop3',
            field=models.CharField(blank=True, max_length=50, null=True),
        ),
        migrations.AlterField(
            model_name='business',
            name='livestock1',
            field=models.CharField(blank=True, max_length=50, null=True),
        ),
        migrations.AddIndex(
            model_name='business',
            index=models.Index(fields=['livestock1'], name='ekichabi_bu_livesto_5a020d_idx'),
        ),
        migrations.AddIndex(
            model_name='business',
            index=models.Index(fields=['specialty1'], name='ekichabi_bu_special_eb1055_idx'),
        ),
        migrations.AddIndex(
            model_name='business',
            index=models.Index(fields=['specialty2'], name='ekichabi_bu_special_ae581d_idx'),
        ),
        migrations.AddIndex(
            model_name='business',
            index=models.Index(fields=['specialty3'], name='ekichabi_bu_special_3e5963_idx'),
        ),
        migrations.AddIndex(
            model_name='business',
            index=models.Index(fields=['specialty4'], name='ekichabi_bu_special_3b5f85_idx'),
        ),
        migrations.AddIndex(
            model_name='business',
            index=models.Index(fields=['input1'], name='ekichabi_bu_input1_7d9954_idx'),
        ),
        migrations.AddIndex(
            model_name='business',
            index=models.Index(fields=['input2'], name='ekichabi_bu_input2_718306_idx'),
        ),
        migrations.AddIndex(
            model_name='business',
            index=models.Index(fields=['input3'], name='ekichabi_bu_input3_559463_idx'),
        ),
    ]