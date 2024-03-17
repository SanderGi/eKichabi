from datetime import datetime
from glob import glob

from django.core.management.base import BaseCommand

from ekichabi.services.android.utils import decode_binary_actions, getBit


class Command(BaseCommand):
    help = 'decodes the files that match the given path patterns e.g. "./logs/Android_*.log"'

    def add_arguments(self, parser):
        parser.add_argument('pattern', nargs='+')

    def handle(self, *args, **options):
        for pattern in options['pattern']:
            for path in glob(pattern):
                decoded_actions = "no actions found"
                with open(path, "rb") as binary_file:
                    bytes = binary_file.read()
                    # print(bytes)
                    bits = self.decode_binary_file(bytes)
                    decoded_actions = decode_binary_actions(bits)
                    print('decoded ' + path)
                    # print(decoded_actions)
                with open(path + "_decoded", "w") as decoded_file:
                    decoded_file.write("Actions decoded: {}\n".format(datetime.now().strftime("%Y/%m/%d-%H.%M.%S")))
                    for action_str in decoded_actions:
                        decoded_file.write(action_str + "\n")

    def decode_binary_file(self, bytes):
        for byte in bytes: 
            for i in range(8):
                yield getBit(byte, i)
