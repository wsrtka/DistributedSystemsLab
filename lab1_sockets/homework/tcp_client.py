import socket
import threading
import struct


server_ip = '127.0.0.1'
server_port = 9008

multicast_ip = '224.0.0.1'
multicast_port = 9009


def receive(server):
    
    while True:
        try:
            msg = server.recv(1024).decode('utf-8')
            print(msg)
        except:
            print('An error occured.')
            server.close()
            break


# funkcja odbierająca wiadomości multicast
def receive_multicast(mcast_socket):

    while True:
        try:
            msg, _ = mcast_socket.recvfrom(1024)
            print(msg.decode('utf-8'))
        except:
            print('An error occured')
            mcast_socket.close()
            break


def write(nick, server, mcast_socket):

    udp_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

    while True:
        message = input(f'{nick}: ')

        if message == 'U':
            with open('heart.txt', 'r') as file:
                udp_socket.sendto(file.read().encode('utf-8'), (server_ip, server_port))
        # obsługa wysłania wiadomości multicast
        elif message == 'M':
            with open('peace.txt', 'r') as file:
                mcast_socket.sendto(file.read().encode('utf-8'), (multicast_ip, multicast_port))
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

    # tworzenie nowego gniazda multicast
    mcast_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    mcast_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    mcast_socket.bind(('', multicast_port))

    # dodanie gniazda do grupy multicast
    group = socket.inet_aton(multicast_ip)
    mreq = struct.pack('4sL', group, socket.INADDR_ANY)
    mcast_socket.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, mreq)

    recv_thread = threading.Thread(target=receive, args=(server,))
    write_thread = threading.Thread(target=write, args=(nick, server, mcast_socket))
    # dodatkowy wątek na multicast
    mcast_thread = threading.Thread(target=receive_multicast, args=(mcast_socket,))

    recv_thread.start()
    write_thread.start()
    # rozpoczęcie działania wątku
    mcast_thread.start()
