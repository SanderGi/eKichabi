from django.core.exceptions import ValidationError
from django.db import models
from django.utils.translation import gettext_lazy as _
from django.utils.translation import ugettext as gettext

managed = True


class Category(models.Model):

    managed = True

    name = models.CharField(max_length=100)

    def __str__(self):
        """ lazily hardcoding translations here
                don't want to mess with Brian's .csv """
        return gettext(self.name)  # self.translations.get(self.name,self.name)

    @staticmethod
    def translated_name():
        """ should be used instead of __name__ for all user-facing
            purposes ONLY"""
        return _('Category')


class Phone(models.Model):

    managed = True

    name = models.CharField(max_length=30)

    def __str__(self):
        return self.name


# Ivy changed this
class Subsector(models.Model):

    managed = True

    name = models.CharField(max_length=100)
    category = models.ForeignKey(Category, on_delete=models.CASCADE)

    def __str__(self):
        return self.name

    def __len__(self):
        return len(self.name)

    @staticmethod
    def translated_name():
        """ should be used instead of __name__ for all user-facing
            purposes ONLY"""
        return _('Subsector')


class District(models.Model):

    managed = True

    name = models.CharField(max_length=100)

    def __str__(self):
        return self.name

    @staticmethod
    def translated_name():
        """ should be used instead of __name__ for all user-facing
            purposes ONLY"""
        return _('District')


class Village(models.Model):

    managed = True

    name = models.CharField(max_length=100)
    district = models.ForeignKey(District, on_delete=models.CASCADE)

    def __str__(self):
        return self.name

    @staticmethod
    def translated_name():
        """ should be used instead of __name__ for all user-facing
            purposes ONLY"""
        return _('Village')


class Subvillage(models.Model):

    managed = True

    name = models.CharField(max_length=100)
    village = models.ForeignKey(Village, on_delete=models.CASCADE)

    class Meta:
        ordering = ['village__name', 'name']

    def __str__(self):
        return "{0.village.name} - {0.name}".format(self)

    @staticmethod
    def translated_name():
        """ should be used instead of __name__ for all user-facing
            purposes ONLY"""
        return _('Subvillage')


class Business(models.Model):

    managed = True

    name = models.CharField(max_length=70)

    category = models.ForeignKey(Category, on_delete=models.CASCADE)

    # location information
    district = models.ForeignKey(District, on_delete=models.CASCADE)
    village = models.ForeignKey(Village, on_delete=models.CASCADE)
    subvillage = models.ForeignKey(Subvillage,
                                   on_delete=models.CASCADE)
    # Ivy changed this
    ward = models.CharField(max_length=15)

    # phone numbers
    number1 = models.CharField(max_length=15)
    number2 = models.CharField(max_length=15)

    # business info
    # Ivy changed this
    subsector1 = models.ForeignKey(
        Subsector, on_delete=models.CASCADE, related_name="subsector1")
    subsector2 = models.ForeignKey(
        Subsector, on_delete=models.CASCADE, related_name="subsector2")

    crop1 = models.CharField(max_length=50, blank=True, null=True)
    crop2 = models.CharField(max_length=50, blank=True, null=True)
    crop3 = models.CharField(max_length=50, blank=True, null=True)

    livestock1 = models.CharField(max_length=50, blank=True, null=True)

    specialty1 = models.CharField(max_length=50, blank=True, null=True)
    specialty2 = models.CharField(max_length=50, blank=True, null=True)
    specialty3 = models.CharField(max_length=50, blank=True, null=True)
    specialty4 = models.CharField(max_length=50, blank=True, null=True)

    input1 = models.CharField(max_length=50, blank=True, null=True)
    input2 = models.CharField(max_length=50, blank=True, null=True)
    input3 = models.CharField(max_length=50, blank=True, null=True)

# Ivy changed this
    owner = models.CharField(max_length=100)

    class Meta:
        indexes = [
            models.Index(fields=['name', ]), 
            models.Index(fields=['owner', ]), 
            models.Index(fields=['category', ]),
            models.Index(fields=['district', ]), 
            models.Index(fields=['village', ]), 
            models.Index(fields=['subvillage', ]), 
            models.Index(fields=['subsector1', ]), 
            models.Index(fields=['subsector2', ]), 
            models.Index(fields=['crop1', ]), 
            models.Index(fields=['crop2', ]),  
            models.Index(fields=['crop3', ]),
            models.Index(fields=['livestock1', ]), 
            models.Index(fields=['specialty1', ]), 
            models.Index(fields=['specialty2', ]), 
            models.Index(fields=['specialty3', ]), 
            models.Index(fields=['specialty4', ]), 
            models.Index(fields=['input1', ]), 
            models.Index(fields=['input2', ]), 
            models.Index(fields=['input3', ]), 
        ]

    def __str__(self):
        return self.name

    def description(self):
        """returns the keywords associated with the business"""
        # TODO: implement brian's logic here
        return " ".join((self.subsector1.name, self.subsector2.name))

    @staticmethod
    def translated_name():
        """ should be used instead of __name__ for all user-facing
            purposes ONLY"""
        return _('Business')


class Whitelist(models.Model):
    managed = True

    phone_num = models.CharField(max_length=15, blank=False, unique=True)

    def save(self, *args, **kwargs):
        if self.phone_num is None or self.phone_num == '':
            raise ValidationError('phone number cannot be empty')
        super().save(*args, **kwargs)
