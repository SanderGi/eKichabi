import base64
import itertools
import re
from datetime import datetime, timedelta


def standard_format(phone_num):
    '''Converts any phone number format into a standard format. Assumes a valid number is passed (not None, not empty, etc.)'''
    phone_num = re.sub("[^0-9]", "", phone_num) # only keep numeric characters (no plus, space, etc.)
    return phone_num[-9:] # only keep last 9 numbers (no country code or leading zero)

def getBit(num, ix):
    return (num >> ix) & 1


def decode_Base64(binstr):
    '''Takes a binary string and decodes it into bits'''
    for byte in base64.b64decode(binstr):  # (get bytes that each read from lower order bit to higher order bit)
        for i in range(8):
            yield getBit(byte, i)


def getActionType(binary):
    '''loops through the bit generator's next 3 bits to find the action type'''
    if binary.__next__():
        if binary.__next__():
            if binary.__next__():
                return "FILTER"  # 111
            else:
                return "SEARCH"  # 110
        else:
            if binary.__next__():
                return "CONTACT"  # 101
            else:
                return "OPEN BUSINESS SCREEN"  # 100
    else:
        if binary.__next__():
            if binary.__next__():
                return "CALL"  # 011
            else:
                return "UNFAVORITE"  # 010
        else:
            if binary.__next__():  # 001
                return "FAVORITE"
            else:
                return "UNRECOGNIZED ACTION"  # 000


def getDate(binary):
    daycount = 0
    for i in range(12):
        if binary.__next__():
            daycount += pow(2, i)
    reference = datetime.strptime("2022/1/1", "%Y/%m/%d")
    return reference + timedelta(days=daycount)


def getPK(binary):
    pk = 0
    for i in range(16):
        if binary.__next__():
            pk += pow(2, i)
    return pk

def decode_Base64_actions(binstr):
    '''Takes a binary string (b'text') and decodes it into humanly readable action strings'''
    return decode_binary_actions(decode_Base64(binstr))

def decode_binary_actions(binary):
    '''Takes a binary (boolean) generator and decodes it into humanly readable action strings'''
    currentActionType = False
    actionStrings = []
    while binary:
        try:
            if not currentActionType:  # we are starting a new action
                currentActionType = getActionType(binary)
                actionStrings += [currentActionType + " | "]
                if currentActionType == 'UNRECOGNIZED ACTION':
                    actionStrings[-1] += "[000]"
            if currentActionType in ["FAVORITE", "UNFAVORITE", "CALL", "OPEN BUSINESS SCREEN", "CONTACT"]:
                date = getDate(binary)
                actionStrings[-1] += date.strftime("%Y/%m/%d") + " | "
                actionStrings[-1] += "Business pk: " + str(getPK(binary))
                for _ in range(9):
                    binary.__next__()
                currentActionType = False
            elif currentActionType in ["SEARCH", "FILTER"]:
                date = getDate(binary)
                actionStrings[-1] += date.strftime("%Y/%m/%d") + " | "
                actionStrings[-1] += "SUCCESSFUL | " if binary.__next__() else "UNSUCCESSFUL | "
                actionStrings[-1] += "TRUNCATED | " if binary.__next__() else "UNTRUNCATED | "
                actionStrings[-1] += "CLEANED | " if binary.__next__() else "RAW | "
                searchstr = ""
                isOffByThree = False
                while True:
                    value = -1
                    for i in range(5):
                        value += pow(2, i) * binary.__next__()
                    if value == -1:
                        bit1 = binary.__next__()
                        bit2 = binary.__next__()
                        bit3 = binary.__next__()
                        if not bit1 and not bit2 and not bit3 and not (currentActionType == "FILTER" and searchstr in ['mafuta ya', 'kuosha']):
                            binary = itertools.chain([0, 0, 0], binary)
                            break
                        elif len(searchstr) >= 21:
                            isOffByThree = True
                            binary = itertools.chain([bit1, bit2, bit3], binary)
                            break
                        else: # catch misencoded strings that contain special characters
                            binary = itertools.chain([bit1, bit2, bit3], binary)
                            searchstr += " "
                    else:
                        searchstr += chr(value + 97)
                actionStrings[-1] += searchstr
                bits = 3 + len(searchstr) * 5 + 8 + 3 + 12
                for _ in range(3):
                    binary.__next__()
                if bits % 8 != 0:
                    for _ in range(8 - (bits % 8)):
                        binary.__next__()
                currentActionType = False
                if isOffByThree: # recovery mode from off by 3 error
                    currentActionType = "UNRECOGNIZED ACTION"
                    actionStrings += [currentActionType + " | ["]
                    for _ in range(3):
                        actionStrings[-1] += str(binary.__next__())
                    actionStrings[-1] += "]"
            else:
                for _ in range(5):
                    actionStrings[-1] += str(binary.__next__())
                while not actionStrings[-1].endswith("0000000000"):
                    for _ in range(8):
                        actionStrings[-1] += str(binary.__next__())
                currentActionType = False
        except StopIteration:
            break
    return actionStrings

# print(decode_Base64_actions(b'bIkAAAA='))
