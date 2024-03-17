# system imports
import datetime
import os.path

from django.conf import settings

LOG_DIR = getattr(settings, 'NIAFIKRA_LOG_DIR',
                  os.path.join(settings.PROJECT_DIR, 'logs'))


def get_path(session):

    filename = "{}_{}.log".format(session['number'].lstrip("+"),
                                  convert_datestring_to_date(session['created']).strftime('%Y-%m-%d-%H.%M.%S'))

    return os.path.join(LOG_DIR, filename)


def make_line(session, line):
    time_now = datetime.datetime.now()
    time_created = convert_datestring_to_date(session['created'])
    time_from_start = time_now - time_created
    with open(find_or_make_logfile(session), 'a') as logfile:
        logfile.write('[{}]\t'.format(time_from_start) + line + '\n')


def convert_datestring_to_date(datestring):
    return datetime.datetime.strptime(datestring, '%m/%d/%Y, %H:%M:%S')


def find_or_make_logfile(session):
    path = get_path(session)

    if os.path.isfile(path):
        return path

    else:
        # make a new file
        with open(get_path(session), 'w') as logfile:
            logfile.write(
                'session_ID: {id} from {number} started {created}\n'.format(**session))
            logfile.write('[Time from Start]\tAction Type\n')
        return path


def log_user_input(session, input):
    if session['islogging']:
        make_line(session, "INPUT RECEIVED\t\t'{}'".format(input))


def log_render_screen_content(session, screen):
    note = screen.note()
    if session['islogging']:
        make_line(session, "RENDERED SCREEN\t\t"+str(note))


def log_press_back_button(session):
    if session['islogging']:
        make_line(session, "BACK PRESSED")


def log_return_home_screen(session):
    if session['islogging']:
        make_line(session, "HOME PRESSED")

def log_end_screen(session):
    if session['islogging']:
        make_line(session, "EXIT PRESSED")


def log_corresponding_menu_item(session, note):
    if session['islogging']:
        make_line(session, "MENU ITEM\t\t"+note.replace('\n', ' '))


def android_get_path(phone_num):
    filename = "Android_{}.log".format(phone_num)
    return os.path.join(LOG_DIR, filename)


def android_make_line(phone_num, bytes):
    with open(android_find_or_make_logfile(phone_num), "ab") as binary_file:
        binary_file.write(bytes)


def android_find_or_make_logfile(phone_num):
    path = android_get_path(phone_num)

    if os.path.isfile(path):
        return path

    else:
        with open(android_get_path(phone_num), 'xb'):
            pass
        return path
