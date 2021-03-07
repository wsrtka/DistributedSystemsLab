import socket
import threading


# ustawienia serwera
server_ip = '127.0.0.1'
server_port = 9008
# struktura do przechowywania nicków klientów wraz z ich połączeniami
clients = {}


# funkcja wysyłająca wiadomość do wszystkich podłączonych klientów
def broadcast(message, sender):
    
    for client in clients.values():
        if client != sender:
            client.send(message.encode('utf-8'))


# funkcja obsługująca wiadomość przychodzącą od klienta
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


# funkcja obsługująca nowe połączenie
def receive():
    
    # tworzone jest gniazdo serwera
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind((server_ip, server_port))
    server_socket.listen()

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
