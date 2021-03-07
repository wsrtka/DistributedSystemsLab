import socket
import threading


def receive(server):
    
    while True:
        try:
            msg = server.recv(1024).decode('utf-8')
            print(msg)
        except:
            print('An error occured.')
            server.close()
            break


def write(nick, server):
    while True:
        message = input()
        server.send(f'{nick}: {message}'.encode('utf-8'))


if __name__ == '__main__':

    server = socket.create_connection(('127.0.0.1', 9008))
    msg = server.recv(1024).decode('utf-8')

    while msg == 'Enter nickname':

        print(msg)
        nick = input()
        server.send(nick.encode('utf-8'))
        msg = server.recv(1024).decode('utf-8')

    recv_thread = threading.Thread(target=receive, args=(server,))
    write_thread = threading.Thread(target=write, args=(nick, server))

    recv_thread.start()
    write_thread.start()
