import os
from re import S

path = input("Path to log directory: ")
  
# Change the directory
os.chdir(path)

def readTime(timestr):
    content = timestr[1:-1]
    parts = content.split('.')
    subparts = parts[0].split(':')
    hr = int(subparts[0])
    min = int(subparts[1]) + hr * 60
    sec = int(subparts[2]) + min * 60
    micro = int(parts[1]) + sec * 1000000
    return micro

def readLog(file_path):
    serverTimeSpent = 0
    renders = 0
    with open(file_path, 'r') as f:
        lines = f.readlines()[2:]
        lastInputRecieved = 0
        for line in lines:
            parts = line.split('\t')
            if (parts[1] == 'RENDERED SCREEN'):
                renders += 1
                time = readTime(parts[0])
                serverTimeSpent += (time - lastInputRecieved)
            elif (parts[1] == 'INPUT RECEIVED'):
                lastInputRecieved = readTime(parts[0])
    return serverTimeSpent, renders, readTime(lines[-1].split('\t')[0])
  
  
# iterate through all file
serverTimeSpent = 0
renders = 0
total = 0
for file in os.listdir():
    # Check whether file is in text format or not
    if file.endswith(".log"):
        file_path = f"{file}"
  
        # call read text file function
        s, r, t = readLog(file_path)
        serverTimeSpent += s
        renders += r
        total += t

print('Server time spent: ' + str(serverTimeSpent) + " Renders: " + str(renders) + " Total: " + str(total))