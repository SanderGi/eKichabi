import json
import plotly
import plotly.express as px
import plotly.graph_objects as go
import pandas as pd
from glob import glob
import os
from datetime import datetime, timedelta
from plotly.subplots import make_subplots
from zipfile import ZipFile
import calendar
import re
import csv
import json
start_time = datetime.now()
from django.conf import settings
redis = settings.REDIS
import numpy as np
import operator 

def standard_format(phone_num):
	'''Converts any phone number format into a standard format. Assumes a valid number is passed (not None, not empty, etc.)'''
	phone_num = re.sub("[^0-9]", "", phone_num) # only keep numeric characters (no plus, space, etc.)
	return phone_num[-9:] # only keep last 9 numbers (no country code or leading zero)


def populate_whitelist_for_admins(set):
    set.add(standard_format('255000000000'))
    set.update([standard_format(num) for num in ['SECRET']])


def populate_whitelist_from_csv(set):
    whitelist_csv = os.path.join(settings.PROJECT_DIR +'/data/whitelisting_info.csv')
    with open(whitelist_csv, 'r') as csvfile:
        csvreader = csv.reader(csvfile)
        next(csvreader)
        set.update({standard_format(num) for num in sum([row[2:4] for row in csvreader], []) if num})

def get_newest_whitelist():
	whitelist_set = set()
	populate_whitelist_for_admins(whitelist_set)
	populate_whitelist_from_csv(whitelist_set)
	return whitelist_set


def add_histogram(fig, x_data, y_data, name):
    return fig.add_trace(go.Histogram(x=x_data,
                               y=y_data,
                               histfunc='count',
                               autobinx=False,
                               xbins=dict(start='2022-11-14', end=datetime.today().strftime('%Y-%m-%d'), size='D1'),
                               name=name))

def label_top10(fig, hist):
    x = hist.x
    y = hist.y
    sorted_indices = sorted(range(len(y)), key=lambda i: y[i], reverse=True)
    top10_x = [x[i] for i in sorted_indices[:10]]
    top10_y = [y[i] for i in sorted_indices[:10]]
    annotations = [dict(x=x_val, y=y_val, text=str(y_val), showarrow=False, font=dict(size=10)) for x_val, y_val in zip(top10_x, top10_y)]
    return fig.update_layout(annotations=annotations)

colors = ["#757197", "#B6D1D0","#F0ECD5","#CCCDB2","#FFF7FA","#A39089"]

def plot_donut(data, values, names, title):
    
    fig = px.pie(data_frame=data, values=values, names=names,
                 color_discrete_sequence=colors)
    fig.update_traces(hole=.4,
                      marker=dict(line=dict(color='white', width=3)))
    fig.update_layout(legend=dict(x=0.5,y=-0.1,xanchor='center',orientation='h'))
    return plotly.offline.plot(fig, auto_open=False, output_type="div")


def create_line_chart(filtered_files, x_label_values, y_label_values, x_label_title, y_label_title, title):

    trace = go.Scatter(x=x_label_values, y=y_label_values, mode='lines', line=dict(color=colors[3]))
    layout = go.Layout(xaxis=dict(
                                  showticklabels=True, title=x_label_title,tickangle=45),
                       yaxis=dict(
                                  showticklabels=True, title=y_label_title)
                       ,
                       plot_bgcolor=colors[2])
    fig = go.Figure(data=[trace], layout=layout)
    return fig.to_html(full_html=False)



def runscript():

	# FILE IMPORTS

	ussdfiles = [path for path in glob(settings.NIAFIKRA_LOG_DIR + "/*_*-*-*-*.*.*.log")]
	ussdArchive = settings.PROJECT_DIR + "/logsArchive/all_ussd.zip"
	androidfiles = [path for path in glob(settings.NIAFIKRA_LOG_DIR + "/Android_*.log")]
	archive = ZipFile(ussdArchive, 'r')

	# DICTIONARY INITIALIZATION

	context = {}

	# GENERAL CODE:
	"""Variable for All Log Files"""
	context['ussd_log_files'], context['android_log_files'] = ussdfiles + archive.namelist(), androidfiles
	whitelist = get_newest_whitelist()
	total_whitelisted = len(whitelist)


	# Day vs number of calls

	fig1 = go.Figure()
	day_calls = [file[file.index('_')+1:file[1:].index('.')-2] for file in ussdfiles + archive.namelist() if '255000000000' not in file and standard_format(file.split('_')[0]) in whitelist]
	fig1.add_trace(go.Bar(x=day_calls, y=[1] * len(day_calls), name='Calls By Only Whitelisted Users', marker=dict(color=colors[0], line=dict(color=colors[0], width=2))))

	day_calls_all = [file[file.index('_')+1:file[1:].index('.')-2] for file in ussdfiles + archive.namelist() if '255000000000' not in file]
	fig1.add_trace(go.Bar(x=day_calls_all,y=[1] * len(day_calls_all), name='Calls By Whitelisted + Non Whitelisted Users', marker=dict(color=colors[3], line=dict(color=colors[3], width=2))))
	fig1.update_layout(bargap=0.2,xaxis_title="Date",yaxis_title="Sessions", xaxis=dict(tickangle=45), barmode='stack')
	context['ussdsessionsperday'] = plotly.offline.plot(fig1, auto_open=False,output_type="div")


	fig2 = go.Figure()

	week_calls = pd.to_datetime(day_calls).to_period('W').astype(str).tolist()
	fig2.add_trace(go.Bar(x=week_calls, y=[1] * len(week_calls), name='Weekly Calls By Only Whitelisted Users', marker=dict(color=colors[0], line=dict(color=colors[0], width=2))))

	week_calls_all = pd.to_datetime(day_calls_all).to_period('W').astype(str).tolist()
	fig2.add_trace(go.Bar(x=week_calls_all, y=[1] * len(week_calls_all), name='Weekly Calls By Whitelisted + Non Whitelisted Users', marker=dict(color=colors[3], line=dict(color=colors[3], width=2))))

	fig2.update_layout(bargap=0.2,xaxis_title="Week",yaxis_title="Sessions", barmode='stack')
	context['ussdsessionsperweek'] = plotly.offline.plot(fig2, auto_open=False,output_type="div")


	# Month vs number of calls

	fig3 = go.Figure()
	month_calls = pd.to_datetime(day_calls).to_period('M').strftime('%b %Y').tolist()
	fig3.add_trace(go.Bar(x=month_calls, y=[1] * len(month_calls), name='Monthly Calls By Only Whitelisted Users', marker=dict(color=colors[0], line=dict(color=colors[0], width=2))))


	month_calls_all = pd.to_datetime(day_calls_all).to_period('M').strftime('%b %Y').tolist()
	fig3.add_trace(go.Bar(x=month_calls_all, y= [1] * len(month_calls_all), name='Monthly Calls By Whitelisted + Non Whitelisted Users', marker=dict(color=colors[3], line=dict(color=colors[3], width=2))))

	fig3.update_layout(bargap=0.2,xaxis_title="Month",yaxis_title="Sessions", xaxis=dict(tickangle=45), barmode='stack')
	context['ussdsessionspermonth']  = plotly.offline.plot(fig3 , auto_open=False,output_type="div")

	context['non_whitelisted_calls'] = len([file for file in ussdfiles + archive.namelist() if '255000000000' not in file]) - len([file for file in ussdfiles + archive.namelist() if '255000000000' not in file and standard_format(file.split('_')[0]) in whitelist])
	whitelist_set = set(whitelist)

	############################################# USSD  SESSIONS PER DAY #########################

	filtered_files_weekday = [file.split('/')[-1] for file in ussdfiles + archive.namelist() 
                          if '255000000000' not in file and standard_format(file.split('_')[0]) in whitelist_set]
	count_weekday = {day: sum(datetime.strptime(file[file.index('_')+1:file[1:].index('.')-2], '%Y-%m-%d').weekday() == day 
							for file in filtered_files_weekday) for day in range(7)}
	x_labels_weekday = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
	y_values_weekday = [count_weekday[day] for day in range(7)]
	context['ussdsessionsperweekdaynot255000000000'] = create_line_chart(filtered_files_weekday, x_labels_weekday, y_values_weekday,
																		'Weekday', 'Sessions', 'Sessions per weekday not 255000000000')

	filtered_files_hour = [file.split('/')[-1] for file in ussdfiles + archive.namelist() 
                        if '255000000000' not in file and standard_format(file.split('_')[0]) in whitelist]

	# Convert timezone from UTC to EAT
	tz_offset = timedelta(hours=0)
	hour_counts = {}
	for file in filtered_files_hour:
		file_time = datetime.strptime(file[file.index('_')+1:file.index('.log')], '%Y-%m-%d-%H.%M.%S')
		file_time_eat = file_time + tz_offset
		hour = file_time_eat.hour
		hour_counts[hour] = hour_counts.get(hour, 0) + 1

	sorted_counts = sorted(hour_counts.items(), key=lambda x: x[0])
	x_vals_hour = [(datetime.strptime(str(t[0]) + ':00:00', '%H:%M:%S') + tz_offset).strftime('%I %p') for t in sorted_counts]
	y_vals_hour = [t[1] for t in sorted_counts]

	context['ussdsessionsperhour'] = create_line_chart(filtered_files_hour, x_vals_hour, y_vals_hour, 'Time (EAT, 12-hour clock)', 'Sessions', 'Sessions per hour')


	###########################################

	df = pd.DataFrame([[0, 0, 'USSD'], [0, 0, 'Android']], columns=['uniqueusers','actions','platform'])
	ussdnumbers = {standard_format(path.split('_')[0]) for path in ussdfiles if '255000000000' not in path}
	df.iloc[0, 1] += sum((sum(1 for _ in open(path)) - 2) / 2.06 for path in ussdfiles if standard_format(path.split('_')[0]) in whitelist)
	ussdnumbers = {standard_format(path.split('_')[0]) for path in archive.namelist() if '255000000000' not in path}
	df.iloc[0, 1] += sum((archive.getinfo(path).file_size / 54 - 2) for path in archive.namelist() if standard_format(path.split('_')[0]) in whitelist)
	df.iloc[0, 0] = len(ussdnumbers.intersection(whitelist))
	df.iloc[1, 1] += sum(str(open(path, 'rb').read()).count("\\x00") for path in androidfiles if '255000000000' not in path)
	df.iloc[1, 0] += len([path for path in androidfiles if '255000000000' not in path])

	context['avuactions'] = plot_donut(df, 'actions', 'platform', 'Total Logged Actions by Whitelisted Users')
	users = pd.DataFrame([[df.iloc[0, 0], 'USSD (currently whitelisted)'], [len(ussdnumbers - whitelist), 'USSD (non-whitelisted)'], [df.iloc[1, 0], 'Android']], columns=['uniqueusers','platform'])
	context['avuuniqueusers'] = plot_donut(users,'uniqueusers', 'platform', 'Number of Unique Users (phone numbers)')
	df['actionsperuser'] = df['actions'] / df['uniqueusers']
	context['avuactionsperuser'] = plot_donut(df,'actionsperuser', 'platform', 'Logged Actions Per Active Whitelisted User')

	###################### This code is not tested ######################

	logs = ussdfiles + archive.namelist()
	total = len(logs)
	whitelist = pd.read_csv(settings.PROJECT_DIR +"/data/whitelisting_info.csv")

	whitelist_firm = pd.read_csv(settings.PROJECT_DIR +"/data/census_data_trimmed.csv")
	valid_numbers_firm = set(whitelist_firm.loc[0:, "mobile_number1"].dropna().astype(int)).union(set(whitelist_firm.loc[0:, "mobile_number2"].dropna().astype(int)))

	valid_numbers_household = set(whitelist.loc[0:, "mobile_number1"])
	valid_numbers = valid_numbers_household.union(valid_numbers_firm)
	
	df = pd.DataFrame(columns=['log', 'time_elapsed'])
	dt = datetime.strptime(datetime.today().strftime('%Y-%m-%d'), '%Y-%m-%d')
	start = dt - timedelta(days = dt.weekday())
	end = start + timedelta(days = 6, hours=23, minutes=59, seconds=59.999999)
	whitelist_weekly = len(whitelist.loc[(pd.DataFrame((datetime.strptime(x, '%Y-%d-%b') for x in whitelist['date']), columns=['date'])['date'] >= start) & (pd.DataFrame((datetime.strptime(x, '%Y-%d-%b') for x in whitelist['date']), columns=['date'])['date'] <= end)])
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
	df_dic = {"district": pd.DataFrame(columns=['district', 'visits']), "category": pd.DataFrame(columns=['category', 'visits']), "product": pd.DataFrame(columns=['product', 'visits']), "business": pd.DataFrame(columns=['business', 'visits']), "subsector1": pd.DataFrame(columns=['subsector1', 'visits']), "village": pd.DataFrame(columns=['village', 'visits'])}
	callsPerHour = pd.DataFrame(columns=['time', 'calls'])
	# Application Uptime today's datetime minus Nov 14 2022 Midnight and comma separated
	context["applicationUptimedays"] = str(datetime.now() - datetime(2022, 11, 14, 0, 0, 0)).split(',')[0]
	

	for i in range(24):
		hour = '%d:00:00 to %d:00:00' % (i, i+1)
		callsPerHour.loc[len(callsPerHour.index)] = [hour, 0] 
	this_week = []
	last_week = []

	whitelist_date_firm_start = datetime.strptime("2022-11-14", '%Y-%m-%d')
	whitelist_date_firm_end = datetime.strptime("2022-12-7", '%Y-%m-%d')


	session_household = 0
	session_firm = 0
	session_invalid = 0

	for i in logs:
		try:
			number = int(i.split('_')[0].split('/')[-1][-9:])
		except:
			continue
		if number in valid_numbers:
			date = i.split('_')[1][:len(i.split('_')[1]) - 4]
			date = datetime.strptime(date, '%Y-%m-%d-%H.%M.%S')
			if number in valid_numbers_household:
				whitelist_date = datetime.strptime(whitelist.loc[whitelist[whitelist['mobile_number1'] == number].index[0], "date"], '%Y-%d-%b')
				if date >= whitelist_date:
					session_household += 1
				else:
					session_invalid += 1
					continue
			elif number in valid_numbers_firm and date >= whitelist_date_firm_start and date <= whitelist_date_firm_end:
				session_firm += 1
			else:
				session_invalid += 1
				continue
			
			if i in ussdfiles:
				f = open(i, 'r')
				lines = f.readlines()
			elif i in archive.namelist():
				f = archive.open(i)
				lines = f.readlines()
				lines = [line.decode('utf-8') for line in lines]
			else:
				pass
			time = lines[-1].split('	')[0].split("[")[1].split("]")[0]
			(h,m,s) = time.split(':')
			time = timedelta(hours=int(h), minutes=int(m), seconds=float(s))
			last_line = lines[-1]
			number = lines[0].split(' ')[3]
			if number in userClassification['phone number'].values:
				userClassification.loc[userClassification['phone number'] == number, 'calls'] += 1
			else:
				userClassification.loc[len(userClassification.index)] = [number, 1] 

			df.loc[len(df.index)] = [i, time] 
			totalSessionTime += time
			whitelist_session += 1

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
			if total != 0:
				averageSessionTime = totalSessionTime / whitelist_session
			else: 
				averageSessionTime = 0
			std_time = df['time_elapsed'].std()		
			if time > (averageSessionTime - std_time) and time < (averageSessionTime + std_time):
				validSession += 1
			if date >= start and date <= end:
				callsweekly += 1
				callsDaily.loc[callsDaily['date'] == date.date(), 'calls'] += 1
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
					try:
						number = int(i.split('_')[0].split('/')[-1][-9:])
					except:
						continue
					if number in valid_numbers:
						date_str = j.split('_')[1][:len(i.split('_')[1]) - 4]
						date = datetime.strptime(date_str, '%Y-%m-%d-%H.%M.%S')
						if start <= date <= end:
							if i in ussdfiles:
								with open(i, 'r') as f_:
									lines = f_.readlines()
							elif i in archive.namelist():
								with archive.open(i) as f_:
									lines = [line.decode('utf-8') for line in f_.readlines()]
							else:
								continue
							time_str = lines[-1].split('\t')[0].split("[")[1].split("]")[0]
							h, m, s = time_str.split(':')
							time = timedelta(hours=int(h), minutes=int(m), seconds=float(s))
							start_time_j = date
							end_time_j = date + time + timedelta(minutes=3)
							if (start_time_i < start_time_j < end_time_i) or (start_time_i < end_time_j < end_time_i):
								concurrent_session[i] += 1
						


	context['both_weeks'] = len(set(this_week).intersection(last_week))

	top5searches = searchMetrics.sort_values(by=['times'], ascending=False)
	top5searches = top5searches[~top5searches['search'].str.contains('error|fail')].head(5)
	labels = list(top5searches['search'].values)
	values = list(top5searches['times'].values)
	data = [go.Bar(x=labels, y=values, marker=dict(color=colors[2], line=dict(color=colors[3], width=1)))]
	layout = go.Layout(yaxis=dict(title='Search Times', tickmode='linear', tick0=0), xaxis=dict(tickangle=45))
	fig = go.Figure(data=data, layout=layout)
	context['top5searches'] = fig.to_html(full_html=False)
	if whitelist_session != 0:
		averageSessionTime = totalSessionTime / whitelist_session
	else: 
		averageSessionTime = '0:00:00'
	std_time = df['time_elapsed'].std()

	agg = {}
	for i in df['time_elapsed']:
		rounded = round(i.total_seconds() / 60) * 60
		if rounded in agg.keys():
			agg[rounded] += 1
		else:
			agg[rounded] = 1
	
	buckets = [0] * 4
	for time, count in agg.items():
		if time < 30:
			buckets[0] += count
		elif time < 60:
			buckets[1] += count
		elif time < 120:
			buckets[2] += count
		else:
			buckets[3] += count

	less_than_30, between_30_60, between_60_120, more_than_120 = buckets

	# create new x and y values for the grouped data

	fig = go.Figure(data=[go.Bar(x=['<30 secs', '30secs-1 min', '1-2 mins', '>2 mins'], y=[less_than_30, between_30_60, between_60_120, more_than_120], marker=dict(color=colors[2], line=dict(color=colors[3], width=1)))], layout=go.Layout( title_font=dict(size=18, family='Arial', color='#000000'), xaxis=dict(tickangle=45, title='Time Elapsed (Seconds)', title_font=dict(size=14, family='Arial', color='#000000')), yaxis=dict(title='Count', title_font=dict(size=14, family='Arial', color='#000000')), bargap=0.2, bargroupgap=0.1,))
	context['time_elapsed_plot'] = fig.to_html(full_html=False)



	df = pd.DataFrame(columns=['log', 'time_elapsed'])
	dt = datetime.now()
	start = dt.replace(day=1)
	month_end = calendar.monthrange(dt.year, dt.month)[1]
	end = dt.replace(day=month_end, hour=23, minute=59, second=59, microsecond=999999)
	whitelist_monthly = len(whitelist.loc[(pd.DataFrame((datetime.strptime(x, '%Y-%d-%b') for x in whitelist['date']), columns=['date'])['date'] >= start) 
			& (pd.DataFrame((datetime.strptime(x, '%Y-%d-%b') for x in whitelist['date']), columns=['date'])['date'] <= end)])
	context['whitelist_monthly'] = whitelist_monthly


	# Number of whitelisted people this week for each day of week (Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday)
	whitelist_weekly_day = []
	for i in range(7):
		start = datetime.now() - timedelta(days=datetime.now().weekday()) + timedelta(days=i)
		end = datetime.now() - timedelta(days=datetime.now().weekday()) + timedelta(days=i+1)
		whitelist_weekly_day.append(len(whitelist.loc[(pd.DataFrame((datetime.strptime(x, '%Y-%d-%b') for x in whitelist['date']), columns=['date'])['date'] >= start) 
			& (pd.DataFrame((datetime.strptime(x, '%Y-%d-%b') for x in whitelist['date']), columns=['date'])['date'] <= end)]))

	context['whitelist_weekly_day'] = '{"type":"line", "data": {"labels":["Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"], "datasets":[{"label":"Whitelisted", "data": %s, "backgroundColor":"rgba(255, 99, 132, 0.2)", "borderColor":"rgba(255,99,132,1)", "borderWidth":1, "fill":false}]}, "options": {"scales": {"yAxes": [{"ticks": {"beginAtZero": true}}]}}}' % str(whitelist_weekly_day)

	topBusiness = topBusiness.sort_values(by = 'visits', ascending=False)

	


	context['total'] = total
	context['total_whitelisted'] = total_whitelisted
	context['callsweekly'] = callsweekly
	context['whitelist_weekly'] = whitelist_weekly
	context['averageSessionTime_hr'] = str(
		averageSessionTime).split(':')[0]
	context['averageSessionTime_min'] = str(
		averageSessionTime).split(':')[1]
	context['averageSessionTime_sec'] = str(
		averageSessionTime).split(':')[2]
	context['validSession'] = validSession
	if len(concurrent_session.values()) == 0:
		context['concurrent_session'] = 0
	else:
		context['concurrent_session'] = max(concurrent_session.values())
	context['userClassification'] = userClassification.to_string(
		header=True, index=False)

	callsDaily = callsDaily.set_index('date')
	context['callsDaily'] = go.Figure(data=[go.Bar(x=callsDaily.index.tolist(), y=callsDaily.calls.tolist(), marker=dict(color=colors[2], line=dict(color=colors[3], width=1)))], layout=go.Layout( title_font=dict(size=18, family='Arial', color='#000000'), yaxis=dict(tickformat=',d'))).to_html(full_html=False)

	topBusiness = topBusiness.head(10)
	context['topBusiness'] = go.Figure(data=[go.Bar(x=topBusiness.business.tolist(), y=topBusiness.visits.tolist(), marker=dict(color=colors[2], line=dict(color=colors[3], width=1)))], layout=go.Layout( title_font=dict(size=18, family='Arial', color='#000000'), xaxis=dict(title="Business",tickangle=45), yaxis=dict(title="Visits", ticks="", tickformat=',d'))).to_html(full_html=False)

	df_dic['business'] = df_dic['business'].drop(df_dic['business'][df_dic['business']['business'] == 'firm_name'].index) 
	df_dic['district'] = df_dic['district'].drop(df_dic['district'][df_dic['district']['district'] == 'District'].index)
	for key, value in df_dic.items():
		value = value.sort_values('visits', ascending=False).set_index(key)
		if len(value.index) > 10:
			value = value.iloc[:10]
		context[key.strip()] = go.Figure(data=[go.Bar(x=value.index.tolist(), y=value.visits.tolist(), marker=dict(color=colors[2], line=dict(color=colors[3], width=1)))], layout=go.Layout(title_font=dict(size=18, family='Arial', color='#000000'), xaxis=dict(title='Date', tickangle=45), yaxis=dict(title='Visits'))).to_html(full_html=False)

	context["product"] = context["product"] 
	archive.close()

	# Android logs

	logs = [path for path in glob(settings.PROJECT_DIR +"/android_logs/Android_*.log_decoded")]
	totalActions = 0
	totalFavorite = 0
	totalCalls = 0
	searchMetric = pd.DataFrame(columns=['search', 'times'])
	temporal = pd.DataFrame(columns=['date', 'times'])
	android_whitelist = 0
	for i in logs:
		if '255000000000' not in i:
			number = standard_format(i.split('_')[1].split('.')[0])
			ussdnumbers.add(number)
			if (number in whitelist):
				android_whitelist += 1
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
	

	# Define the data
	x_values = searchMetric['search'].tolist()
	y_values = searchMetric['times'].tolist()

	# Sort the data in descending order
	sorted_indices = sorted(range(len(y_values)), key=lambda i: y_values[i], reverse=True)
	if len(sorted_indices) > 10:
		sorted_indices = sorted_indices[:10]
	fig = go.Figure(data=[go.Bar(x=[x_values[i] for i in sorted_indices], y=[y_values[i] for i in sorted_indices], marker=dict(color=colors[2], line=dict(color=colors[3], width=1)))], layout=go.Layout(title_font=dict(size=18, family='Arial', color='#000000'), xaxis=dict(title='Search',tickangle=45,  title_font=dict(size=14, family='Arial', color='#000000')), yaxis=dict(title='Times', title_font=dict(size=14, family='Arial', color='#000000'))))
	context['searchMetric'] = fig.to_html(full_html=False)


	temporal['month'] = pd.to_datetime(temporal['date']).dt.to_period('M').apply(lambda x: x.strftime('%b %Y'))
	monthly_temporal = temporal.groupby('month').sum(numeric_only=True).reset_index()
	monthly_temporal['month'] = pd.to_datetime(monthly_temporal['month'], format='%b %Y')
	monthly_temporal = monthly_temporal.sort_values(by='month')
	monthly_temporal['month_name'] = monthly_temporal['month'].dt.strftime('%b')
	fig = go.Figure(data=[go.Bar( x=monthly_temporal['month_name'].tolist(), y=monthly_temporal['times'].tolist(), marker=dict(color=colors[2], line=dict(color=colors[3], width=1)) )], layout=go.Layout(  title_font=dict(size=18, family='Arial', color='#000000'), xaxis=dict(title='Month',tickangle=45, title_font=dict(size=14, family='Arial', color='#000000')), yaxis=dict(title='Number of Calls', title_font=dict(size=14, family='Arial', color='#000000')) ))

	context['temporal'] = fig.to_html(full_html=False)
	context['android_whitelist'] = android_whitelist
	context['totalActions'] = totalActions	
	context['totalFavorite'] = totalFavorite
	context['totalCalls'] = totalCalls

	 # SERVER HEALTH METRICS
	production_env = os.getenv('PRODUCTION_ENV') == 'true'
	if not production_env:
		# cursor.execute('''SELECT COUNT(*) FROM silk_request''')
		# context['silkedrequests'] = cursor.fetchone()[0]
		context['silkedrequests'] = '--'
	else:
		context['silkedrequests'] = '--'
	if not production_env:
		# cursor.execute('''SELECT AVG(time_taken) FROM silk_request''')
		# context['silkedrequestsaveragetime'] = cursor.fetchone()[0]
		context['silkedrequestsaveragetime'] = '--'
	else:
		context['silkedrequestsaveragetime'] = '--'

	redisinfo = redis.info()
	context['redismemoryused'] = redisinfo.get('used_memory_human', '--')
	context['redismemoryusedos'] = redisinfo.get( 'used_memory_rss_human', '--')
	context['redisactivesessions'] = len(redis.keys())
	json_data = json.dumps(context)
	with open(settings.PROJECT_DIR +"/data/context.json", "w") as file:
		file.write(json_data)

	print("--- %s seconds ---" % (datetime.now() - start_time).total_seconds())

if __name__=="__main__":
	runscript()
