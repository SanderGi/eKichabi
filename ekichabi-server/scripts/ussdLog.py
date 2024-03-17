#!/usr/bin/env python
# coding: utf-8
import os
import pandas as pd
import matplotlib.ticker as ticker
import matplotlib.pyplot as plt
from glob import glob
from datetime import datetime, timedelta
from pathlib import Path

# get_ipython().system(' rm ../logs/.DS_store')
dirname, filename = os.path.split(os.path.abspath(__file__))
filepath = dirname + '/ussdlog/'
os.makedirs(filepath, exist_ok=True)

logs = [path for path in glob("../logs/*_*-*-*-*.*.*.log")]

total = len(logs)

whitelist_firm = pd.read_csv("../data/census_data_trimmed.csv")
valid_numbers_firm = set(whitelist_firm.loc[0:, "mobile_number1"].dropna().astype(int)).union(set(whitelist_firm.loc[0:, "mobile_number2"].dropna().astype(int)))
whitelist = pd.read_csv("../data/whitelisting_info.csv")
blocked_number = set()
valid_numbers_household = set(whitelist.loc[0:, "mobile_number1"])
valid_numbers = valid_numbers_household.union(valid_numbers_firm)

total_whitelisted = len(valid_numbers)

df = pd.DataFrame(columns=['log', 'time_elapsed'])
dt = datetime.strptime(datetime.today().strftime('%Y-%m-%d'), '%Y-%m-%d')
start = dt - timedelta(days = dt.weekday())
end = start + timedelta(days = 6, hours=23, minutes=59, seconds=59.999999)
whitelist_weekly = len(whitelist.loc[(pd.DataFrame((datetime.strptime(x, '%Y-%d-%b') for x in whitelist['date']), columns=['date'])['date'] >= start) 
        & (pd.DataFrame((datetime.strptime(x, '%Y-%d-%b') for x in whitelist['date']), columns=['date'])['date'] <= end)])
callsweekly = 0
totalSessionTime = timedelta()
whitelist_session = 0
averageSessionTime = 0
validSession = 0
concurrent_session = {}
topBusiness = pd.DataFrame(columns=['business','visits'])
callsDaily = pd.DataFrame(columns=['date', 'calls'])
for i in range(7):
    cur = start.date() + timedelta(days=i)
    callsDaily.loc[len(callsDaily.index)] = [cur, 0]
searchMetrics = pd.DataFrame(columns=['search', 'times'])
userClassification = pd.DataFrame(columns=['phone number', 'calls'])
df_dic = {"district": pd.DataFrame(columns=['district', 'visits']), 
          "category": pd.DataFrame(columns=['category', 'visits']),
           "product": pd.DataFrame(columns=['product', 'visits']), 
           "business": pd.DataFrame(columns=['business', 'visits']),
           "subsector1": pd.DataFrame(columns=['subsector1', 'visits']),
           "village": pd.DataFrame(columns=['village', 'visits'])}
callsPerHour = pd.DataFrame(columns=['time', 'calls'])
for i in range(24):
    hour = '%d:00:00 to %d:00:00' % (i, i+1)
    callsPerHour.loc[len(callsPerHour.index)] = [hour, 0] 

whitelist_date_firm_start = datetime.strptime("2022-11-14", '%Y-%m-%d')
whitelist_date_firm_end = datetime.strptime("2022-12-7", '%Y-%m-%d')

session_household = 0
session_firm = 0
session_invalid = 0

for i in logs:
    number = int(i.split('_')[0].split('/')[-1][-9:])
    if number in valid_numbers:
        date = i.split('_')[1][:len(i.split('_')[1]) - 13]
        log_date = datetime.strptime(date, '%Y-%m-%d')
        if number in valid_numbers_household:
            whitelist_date = datetime.strptime(whitelist.loc[whitelist[whitelist['mobile_number1'] == number].index[0], "date"], '%Y-%d-%b')
            if log_date >= whitelist_date:
                session_household += 1
            else:
                session_invalid += 1
                continue
        elif number in valid_numbers_firm and log_date >= whitelist_date_firm_start and log_date <= whitelist_date_firm_end:
            session_firm += 1
        else:
            session_invalid += 1
            continue
        if log_date >= start and log_date <= end:
            callsweekly += 1
            callsDaily.loc[callsDaily['date'] == log_date.date(), 'calls'] += 1
        with open(i, 'r') as f:
            lines = f.readlines()
            last_line = lines[-1]
            number = lines[0].split(' ')[3]
            if number in userClassification['phone number'].values:
                userClassification.loc[userClassification['phone number'] == number, 'calls'] += 1
            else:
                userClassification.loc[len(userClassification.index)] = [number, 1] 
            time = last_line.split('	')[0].split("[")[1].split("]")[0]
            (h,m,s) = time.split(':')
            time = timedelta(hours=int(h), minutes=int(m), seconds=float(s))
            df.loc[len(df.index)] = [i, time] 
            totalSessionTime += time
            whitelist_session += 1
            #populate the df_dic
            for i in range(len(lines)):
                if 'RENDERED SCREEN' not in lines[i]:
                    continue
                elif i == len(lines) - 1:
                    if 'again' in lines[i]:
                            item = lines[i-1].split('		')[1].strip()[1:-1]
                            if item in searchMetrics['search'].values:
                                searchMetrics.loc[searchMetrics['search'] == item, 'times'] += 1
                            else:
                                searchMetrics.loc[len(searchMetrics.index)] = [item, 1]
                    continue
                for j in df_dic.keys():
                    if j.lower() in lines[i].lower():
                        next = lines[i+1]
                        if 'All businesses' not in next and 'Biashara Zote' not in next:
                            if 'INPUT RECEIVED' in next and 'FirstChars' in lines[i]:
                                if 'again' in lines[i]:
                                    item = lines[i-1].split('		')[1].strip()[1:-1]
                                    if item in searchMetrics['search'].values:
                                        searchMetrics.loc[searchMetrics['search'] == item, 'times'] += 1
                                    else:
                                        searchMetrics.loc[len(searchMetrics.index)] = [item, 1]
                                item = next.split('		')[1].strip()[1:-1]
                            elif 'MENU ITEM' in next and 'MenuHierarchyScreen' in lines[i] and '99' not in next:
                                item = next.split('		')[1].strip()
                            elif 'MENU ITEM' in next and 'SearchProductScreen' in lines[i]:
                                item = next.split('		')[1].strip()
                            elif 'BusinessDetailsScreen' in lines[i] and 'Failed to call prompt service' not in next:
                                item = lines[i].split(' - for ')[1].strip()
                            else:
                                continue
                            cur_db = df_dic[j]
                            if item in cur_db[j].values:
                                cur_db.loc[cur_db[j] == item, 'visits'] += 1
                            else:
                                cur_db.loc[len(cur_db.index)] = [item, 1]
                            df_dic[j] = cur_db
        f.close()
    elif len(str(number)) == 9:
        blocked_number.add(number)
    

if total != 0:
    averageSessionTime = totalSessionTime / whitelist_session
else: 
    averageSessionTime = 0

# get the standard deviation of the session time
std_time = df['time_elapsed'].std()
for i in logs:
    number = int(i.split('_')[0].split('/')[-1][-9:])
    if number in valid_numbers:
        date = i.split('_')[1][:len(i.split('_')[1]) - 4]
        date = datetime.strptime(date, '%Y-%m-%d-%H.%M.%S')
        if number in valid_numbers_household:
            whitelist_date = datetime.strptime(whitelist.loc[whitelist[whitelist['mobile_number1'] == number].index[0], "date"], '%Y-%d-%b')
            if date < whitelist_date:
                continue
        elif number in valid_numbers_firm and date < whitelist_date_firm_start and date > whitelist_date_firm_end:
            continue
        with open(i, 'r') as f:
            lines = f.readlines()
            time = lines[-1].split('	')[0].split("[")[1].split("]")[0]
            (h,m,s) = time.split(':')
            time = timedelta(hours=int(h), minutes=int(m), seconds=float(s))
            if time > (averageSessionTime - std_time) and time < (averageSessionTime + std_time):
                validSession += 1
            if date >= start and date <= end:
                for line in lines:
                    if "BusinessDetailsScreen" in line:
                        business = line.split(' - for ')[1].strip()
                        if business in topBusiness['business'].values:
                            topBusiness.loc[topBusiness['business'] == business, 'visits'] += 1
                        else:
                            topBusiness.loc[len(topBusiness.index)] = [business, 1]
                hour = "%d:00:00 to %d:00:00" % (int(date.hour), int(date.hour)+1)
                callsPerHour.loc[callsPerHour['time'] == hour, 'calls'] += 1/7
                concurrent_session[i] = 1
                start_time_i = date
                end_time_i = date + time + timedelta(minutes=3)
                for j in logs:
                    date = j.split('_')[1][:len(i.split('_')[1]) - 4]
                    date = datetime.strptime(date, '%Y-%m-%d-%H.%M.%S')
                    if date >= start and date <= end:
                        with open(j, 'r') as f_:
                            time = f_.readlines()[-1].split('	')[0].split("[")[1].split("]")[0]
                            (h,m,s) = time.split(':')
                            time = timedelta(hours=int(h), minutes=int(m), seconds=float(s))
                            start_time_j = date
                            end_time_j = date + time + timedelta(minutes=3)
                            if (start_time_j > start_time_i and start_time_j < end_time_i) or (end_time_j > start_time_i and end_time_j < end_time_i):
                                concurrent_session[i] += 1
                        f_.close()
        f.close()

topBusiness = topBusiness.sort_values(by = 'visits', ascending=False)

with open(filepath + 'analysis.txt', 'w') as f:
    pd.DataFrame({'numbers': list(blocked_number)}).to_csv(filepath + 'blockeduser.csv', index=False)
    f.write('The total number of calls/sessions so far from when we go live: %d \n\n' % total)
    f.write('Number of users whitelisted so far: %d \n\n' % total_whitelisted)
    f.write('The number of calls this week: %d \n\n' % callsweekly)
    f.write('Number of whitelisted people this week: %d \n\n' % whitelist_weekly)
    f.write('The average length of time for each session is %s hr %s min %s sec\n\n' % 
    (str(averageSessionTime).split(':')[0], str(averageSessionTime).split(':')[1], str(averageSessionTime).split(':')[2]))
    f.write('Number of sessions within one std of the average lasting time: %d\n\n' % validSession)
    if len(concurrent_session.values()) == 0: 
        f.write('The number of max concurrent sessions this week (in order to evaluate the server traffic): %d\n\n' % 0)
    else: 
        f.write('The number of max concurrent sessions this week (in order to evaluate the server traffic): %d\n\n' % max(concurrent_session.values()))
    f.write('The number of calls to the USSD server made by each phone number:\n')
    userClassification = userClassification.sort_values(by = 'calls', ascending=False)
    f.write(userClassification.to_string(header=True, index=False))
    userClassification.to_csv(filepath + 'userclassification.csv', index=False)
    f.write('\n\nThe number of calls for every day this week:\n')
    f.write(callsDaily.to_string(header=True, index=False))
    f.write('\n\nThe number of calls for each hour average over the days:\n')
    f.write(callsPerHour.to_string(header=True, index=False))
    f.write('\n\n10 Businesses that are visited the most # of times this week:\n')
    f.write(topBusiness.head(10).to_string(header=True, index=False))
    for i in df_dic.keys():
        cur = df_dic[i].sort_values(by = 'visits', ascending=False)
        plt.bar(cur.head(25)[i].values, cur.head(25).visits)
        plt.title('Top ' + i + ' that are visited the most')
        plt.ylabel('visits')
        plt.xlabel(i)
        plt.xticks(rotation=30, ha='right')
        plt.savefig(filepath + i.replace("/","-") + '.png', bbox_inches = 'tight')
        plt.show()
        f.write('\n\nNumber of times that each %s is visited/clicked:\n' % i)
        f.write(cur.head(25).to_string(header=True, index=False))
        cur.to_csv(filepath + i.replace("/", "-") + '.csv', index=False)
    f.write('\n\nType in search related metrics:\n')
    f.write(searchMetrics.head(10).to_string(header=True, index=False))
    searchMetrics.to_csv(filepath + 'searchmetrics.csv', index=False)
    plt.bar(searchMetrics.head(25)['search'].values, searchMetrics.head(25).times)
    plt.title('Type in search that leads to 0 result')
    plt.ylabel('times')
    plt.xlabel('search')
    plt.xticks(rotation=30, ha='right')
    plt.savefig(filepath + 'searchmetrics.png', bbox_inches = 'tight')
    plt.show()
f.close()

plt.pie([session_household, session_firm, session_invalid], labels=["sessions from whitelisted household(%d)"%session_household, "session from whitelisted firm(%d)"%session_firm, "sessions filtered by whitelist date(%d)"%session_invalid], autopct='%1.1f%%')
plt.title('whitelisted session from household vs firm')
plt.show()

plt.plot(callsDaily.date, callsDaily.calls)
plt.title('Calls daily this week')
plt.xticks(rotation=30, ha='right')
plt.xlabel('Date')
plt.ylabel('Calls')
plt.savefig(filepath + 'Calls daily.png',bbox_inches = 'tight')
plt.show()

plt.plot(callsPerHour.time, callsPerHour.calls)
plt.title('Calls per hour average over this week')
plt.xticks(rotation=30, ha='right')
plt.xlabel('Hour')
plt.ylabel('Calls')
# locator = ticker.MaxNLocator(nbins=24)
plt.savefig(filepath + 'CallsPerHour.png',bbox_inches = 'tight')
plt.show()