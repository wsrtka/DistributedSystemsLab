import socket
import threading


server_ip = '127.0.0.1'
server_port = 9008

clients = {}


def broadcast(message, sender):
    
    for client in clients.values():
        if client != sender:
            client.send(message.encode('utf-8'))


def handle(nick):
    
    client = clients[nick]

    while True:
        try:
            msg = client.recv(1024).decode('utf-8')
            broadcast(msg, client)
        except:
            del clients[nick]
            client.close()
            broadcast(f'{nick} left.', client)
            print(f'Disconnected from {nick}')
            break


# funkcja odpowiadająca za odbieranie wiadomości na kanale udp
def handle_udp():
    
    # tworzenie gniazda typu udp
    udp_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    udp_socket.bind(('', server_port))

    # odbieranie i przesyłanie wiadomości
    while True:
        msg, client = udp_socket.recvfrom(1024)
        broadcast(msg.decode('utf-8'), client)


def receive():
    
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind((server_ip, server_port))
    server_socket.listen()

    # wątek odpowiadający za kanał udp od strony serwera
    upd_thread = threading.Thread(target=handle_udp)
    upd_thread.start()

    while True:

        client, addr = server_socket.accept()
        print(f'Connected with {addr}')

        while True:
            
            client.send('Enter nickname'.encode('utf-8'))
            nick = client.recv(1024).decode('utf-8')

            if nick not in clients:
                clients[nick] = client
                break

        print(f'Set nick {nick}')
        broadcast(f'{nick} joined.', None)
        client.send('Joined the server.'.encode('utf-8'))

        thread = threading.Thread(target=handle, args=(nick,))
        thread.start()


if __name__ == '__main__':
    receive()
