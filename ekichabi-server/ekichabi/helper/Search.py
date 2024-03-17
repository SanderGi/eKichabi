import itertools

from ekichabi.models import Business

Specialty  = ['Nakala (Fotokopi)', 'Kifungua kimywa', 'Mitindo ya kike',
       'Mitindo ya kiume', 'Geti', 'Chakula cha mchana',
       'Chakula cha usiku', 'Vodacom Mpesa',
       'Nguo za watoto na sare za shule', 'Nguo za kiume', 'Nguo za kike',
       'Kuuza bidhaa za karatasi', 'Huduma za upishi zinazotolewa',
       'Mitindo ya kiume na kike', 'Halo pesa', 'Airtel money',
       'Nakala (Fotokopi); Vodacom Mpesa', 'Benki ndogo ya fedha',
       'Tigopesa', 'Madirisha', 'SACCO', 'Tigopesa; Benki ndogo ya fedha',
       'Mitindo ya kiume; Vodacom Mpesa',
       'Vodacom Mpesa; Benki ndogo ya fedha', 'Postal wakala',
       'Kifungua kimywa; Vodacom Mpesa', 'Kuuza vifaa vya maofisini',
       'Vodacom Mpesa; SACCO', 'Mpesa wakala',
       'Kuuza bidhaa za karatasi; Airtel money', 'Milango',
       'Chakula cha mchana; Airtel money',
       'Airtel money; Benki ndogo ya fedha', 'CRDB wakala',
       'Tigopesa wakala', 'Airtel wakala',
       'Kuuza vifaa vya maofisini; Tigopesa', 'Vitanda',
       'Chakula cha usiku; Tigopesa', 'NMB wakala']

Livestock = ['Samaki', 'Mbuzi', 'Kuku au Kanga',
       'Nyama ya buchani', 'Mayai', "Ng'ombe", 'Nguruwe', 'Maziwa']
LivestockAll = 'Mifugo yote'

Crops = ['Mazao ya biashara', 'Miwa', 'Kahawa', 'Mchele', 'Nafaka',
       'Mahindi', 'Karanga', 'Matunda', 'Maharage',
       'Mbogamboga, viungo, matunda na maua', 'Ndizi', 'Mboga za majani',
       'Mtama', 'Viazi vikuu na mihogo',
       'Viazi vitamu, mihogo, viazi vikuu', 'Alizeti', 'Mchele, mayai',
       'Vitunguu', 'Mihogo', 'Uwele', 'Viazi vitamu', 'Vanilla', 'Mbaazi',
       'Ufuta', 'Kunde', 'Sukari', 'Maharage ya soya', 'Dengu',
       'Viazi vikuu']
CropsAll = 'Mazao yote'

Inputs = ['Chakula cha mifugo', 'mbolea/madawa za kilimo', 'Mbegu',
       'pembejeo za kilimo', 'Viuatilifu',
       'pembejeo zinazohusiana na mifugo', 'Madawa ya mifugo',
       'Zana za umwagiliaji', 'Mbolea', 'Zana za Kilimo']
InputsAll = 'Pembejeo zote'

def getQuerySet(fields, searchkey):
    kwarg = {fields[0]: searchkey}
    results = Business.objects.filter(**kwarg)
    for field in fields[1:]:
        kwarg = {field: searchkey}
        results |= Business.objects.filter(**kwarg)
    return results.distinct().only('name', 'owner', 'category', 'district', 'village', 'subvillage', 'subsector1', 'subsector2', 'number1')

def getQuerySetCrops(key):
    return getQuerySet(['crop1','crop2','crop3'], key)

def getQuerySetLivestock(key):
    return getQuerySet(['livestock1'], key)

def getQuerySetSpecialty(key):
    return getQuerySet(['specialty1','specialty2','specialty3','specialty4'], key)

def getQuerySetInputs(key):
    return getQuerySet(['input1','input2','input3'], key)

def getQuerySetByCategory(fieldcategory, key):
    if fieldcategory == "Crop":
        return getQuerySetCrops(key)
    elif fieldcategory == "Livestock":
        return getQuerySetLivestock(key)
    elif fieldcategory == "Specialty":
        return getQuerySetSpecialty(key)
    elif fieldcategory == "Input":
        return getQuerySetInputs(key)

def geticontainskeys(searchstr, searchkeys):
    searchstr = searchstr.lower()
    for skey in searchkeys:
        key = skey.lower()
        if searchstr in key: # or key == 'all inputs' or 'mifugo yote' in key or key == 'mazao yote':
            yield skey

def getKeysByCategory(input, fieldcategory):
    if fieldcategory == "Crop":
        keyslist = Crops
        keyall = CropsAll
    elif fieldcategory == "Livestock":
        keyslist = Livestock
        keyall = LivestockAll
    elif fieldcategory == "Specialty":
        keyslist = Specialty
        keyall = False
    elif fieldcategory == "Input":
        keyslist = Inputs
        keyall = InputsAll
    keys = list(geticontainskeys(input, keyslist))
    if len(keys) == 0:
        if keyall == False:
            return False
        keys = [keyall]
    return keys

def peek(iterable):
    try:
        first = iterable.__next__()
    except StopIteration:
        return None
    return first, itertools.chain([first], iterable)