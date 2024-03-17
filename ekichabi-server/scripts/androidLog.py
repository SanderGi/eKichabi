#!/usr/bin/env python
# coding: utf-8

import os
from turtle import width
import pandas as pd
import matplotlib.ticker as ticker
import matplotlib.pyplot as plt
from glob import glob
from datetime import datetime, timedelta
from pathlib import Path

dirname, filename = os.path.split(os.path.abspath(__file__))
filepath = dirname + '/androidlog/'
os.makedirs(filepath, exist_ok=True)

logs = [path for path in glob("./logs/Android_*.log_decoded")]

totalActions = 0
totalFavorite = 0
totalCalls = 0
searchMetric = pd.DataFrame(columns=['search', 'times'])
temporal = pd.DataFrame(columns=['date', 'times'])

for i in logs:
    with open(i, 'r') as f:
        lines = f.readlines()
        totalActions += (len(lines) - 1)
        if len(lines) > 1:
            date = lines[1].split(' | ')[1]
            if date in temporal['date'].values:
                temporal.loc[temporal['date'] == date, 'times'] += 1
            else:
                temporal.loc[len(temporal.index)] = [date, 1]
        for j in lines:
            if "FAVORITE" in j:
                totalFavorite += 1
            elif "CALL" in j:
                totalCalls += 1
            elif "SEARCH" in j or "FILTER" in j:
                search = j.split(' | ')[-1].strip()
                if search in searchMetric['search'].values:
                    searchMetric.loc[searchMetric['search'] == search, 'times'] += 1
                else:
                    searchMetric.loc[len(searchMetric.index)] = [search, 1]




with open(filepath + 'analysis.txt', 'w') as f:
    f.write('total number of actions so far from when we go live: %d \n\n' % totalActions)
    f.write('Number of times that businesses have been favorited: %d\n\n' % totalFavorite)
    f.write('Number of times that businesses are called: %d \n\n' % totalCalls)
    f.write('Search related metrics:\n')
    searchMetric = searchMetric.sort_values(by = 'times', ascending=False)
    f.write(searchMetric.head(10).to_string(header=True, index=False))
    searchMetric.to_csv(filepath + 'searchmetrics.csv', index=False)
    plt.bar(searchMetric.head(10)['search'].values, searchMetric.head(10).times)
    plt.title('Most searched vs. frequency')
    plt.ylabel('times')
    plt.xlabel('search')
    plt.xticks(rotation=30, ha='right')
    plt.savefig(filepath + 'searchmetrics.png', bbox_inches = 'tight')
    plt.show()
    f.write('\n\nTemporal metrics:\n')
    temporal = temporal.sort_values(by = 'date', ascending=True)
    f.write(temporal.to_string(header=True, index=False))
    temporal.to_csv(filepath + 'temporal.csv', index=False)
    plt.bar(temporal.head(10)['date'].values, temporal.head(10).times)
    plt.title('Date vs. frequency')
    plt.ylabel('times')
    plt.xlabel('date')
    plt.xticks(rotation=30, ha='right')
    plt.savefig(filepath + 'temporal.png', bbox_inches = 'tight')
    plt.show()

