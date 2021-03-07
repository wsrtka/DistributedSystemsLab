import socket
import threading


# funkcja odpowiadająca za odbieranie wiadomości
def receive(server):
    
    while True:
        try:
            msg = server.recv(1024).decode('utf-8')
            print(msg)
        except:
            print('An error occured.')
            server.close()
            break


# funkcja odpowiedzialna za wysyłanie wiadomości
def write(nick, server):
    while True:
        message = input(f'{nick}: ')
        server.send(f'{nick}: {message}'.encode('utf-8'))


if __name__ == '__main__':

    # create_connection to funkcja ułatwiająca tworzenie klienta tcp, w pythonie 3.9 jest też funkcja ułatwiająca tworzenie serwera tcp
    server = socket.create_connection(('127.0.0.1', 9008))
    msg = server.recv(1024).decode('utf-8')

    # ustawianie nicku
    while msg == 'Enter nickname':

        print(msg)
        nick = input()
        server.send(nick.encode('utf-8'))
        msg = server.recv(1024).decode('utf-8')

    # wątki odpowiedzialne za wysyłanie i odbieranie wiadomości
    recv_thread = threading.Thread(target=receive, args=(server,))
    write_thread = threading.Thread(target=write, args=(nick, server))

    recv_thread.start()
    write_thread.start()
