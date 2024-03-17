'''Simple helper file to print out the column number of each column. Run with "python ./data/enumerate_columns.py"'''

import csv

fields = []

# reading csv file
with open('./data/census_data_trimmed.csv', 'r') as csvfile:
    # creating a csv reader object
    csvreader = csv.reader(csvfile)

    # extracting field names through first row
    fields = next(csvreader)

for i, field in enumerate(fields):
    print(str(i) + ": " + str(field))