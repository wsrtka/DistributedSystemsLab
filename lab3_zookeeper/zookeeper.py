"""
Dostępne komendy:
help \t wyświetla pomoc
list \t wyświetla strukturę drzewa "z"
exit \t wyjście z programu
"""

import subprocess
import sys
import signal
import os

from shutil import which
from multiprocessing import Process

from kazoo.client import KazooClient
from kazoo.exceptions import NoNodeError
from kazoo.recipe.watchers import DataWatch, ChildrenWatch


# ========== KONFIGURACJA ==========
HOST_IP = '127.0.0.1'                   # adres IP hosta dla serwerów
HOST_PORTS = ['2181', '2183', '2184']   # porty otwarte na serwerach dla klientów


# ============== MAIN ==============
if __name__ == "__main__":
    
    # przeczytanie z linii poleceń programu do uruchomienia
    try:
        program = sys.argv[1]
    except IndexError:
        program = input('Proszę podać nazwę programu do uruchomienia: ')

    # sprawdzenie, czy program isnieje
    while not which(program):
        program = input('Proszę podać poprawną nazwę programu do uruchomienia: ')

    # utworzenie klienta
    zk = KazooClient(hosts=','.join([':'.join([HOST_IP, port]) for port in HOST_PORTS]))
    zk.start()

    # zmienne pomocnicze
    program_thread = None
    try:
        z_children_num = len(zk.get_children('/z'))
    except NoNodeError:
        z_children_num = 0

    print('Klient został uruchomiony poprawnie.')
    print(__doc__)


    # ======== OBSERWATORZY ========
    @zk.ChildrenWatch('/')
    def z_watcher(children):
        global program
        global program_thread
        if 'z' in children:
            if not program_thread or program_thread.poll():
                program_thread = subprocess.Popen([program])
        else:
            if program_thread and not program_thread.poll():
                program_thread.terminate()

    @zk.ChildrenWatch('/z')
    def z_children_watcher(children):
        global z_children_num
        z_children = zk.get_children('/z')
        if len(z_children) > z_children_num:
            print(f'Liczba potomków node\'a "z": {len(z_children)}')
        z_children_num = len(z_children)

    # ======= FUNKCJE POMOCNICZE =======
    def sigint_handler(signal_number, frame):
        print('Wychodzę z programu. Do widzenia!')
        zk.stop()
        sys.exit()


    # handler SIGINT
    signal.signal(signal.SIGINT, sigint_handler)

    # główna pętla programu
    while True:
        cmd = input()

        if cmd == 'list':
            try:
                print('Potomkowie node\'a "z":')
                for child in zk.get_children('/z'):
                    print('\tchild')
            except NoNodeError:
                print('')
        elif cmd == 'exit':
            os.kill(os.getpid(), signal.SIGINT)
        else:
            print(__doc__)