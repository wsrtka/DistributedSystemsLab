import socket
import threading


server_ip = '127.0.0.1'
server_port = 9008


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

    # funkcja odpowiadająca za wysłanie wiadomości udp
    def write_udp_msg():

        with open('heart.txt', 'r') as file:
            udp_socket.sendto(file.read().encode('utf-8'), (server_ip, server_port))

    udp_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

    while True:
        message = input(f'{nick}: ')

        # reakcja na komendę użytkownika
        if message == 'U':
            write_udp_msg()
        else: 
            server.send(f'{nick}: {message}'.encode('utf-8'))


if __name__ == '__main__':

    server = socket.create_connection((server_ip, server_port))
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
