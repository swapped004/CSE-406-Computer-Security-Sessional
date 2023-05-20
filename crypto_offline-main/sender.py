import socket
import sys
from offline import *
from rsa import *
import os


def save_private_key(private_key, filename):
    with open(filename, 'w') as f:
        f.write(str(private_key))



s=socket.socket()
print("Socket successfully created")	

port=12345

s.bind(('',port))
print("socket binded to %s" %(port))

s.listen(5)
print("socket is listening")

# a forever loop until we interrupt it or
# an error occurs
while True:
 
# Establish connection with client.
    c, addr = s.accept()    
    print ('Got connection from', addr )

    #input plaintext and key from console
    plaintext = input("Enter the message you want to send: ")
    print("plaintext:", plaintext)
    key = input("Enter the key: ")
    print("key:", key) 

    #encrypt the plaintext with AES
    round_keys = generate_round_keys(key, no_of_rounds=11)
    ciphertext = AES_encrypt_whole(plaintext, round_keys)
    print("ciphertext:", ciphertext)
    
    #generate the key pair for RSA
    n, e, d = key_pair_generation(128)
    public_key = str(e) + " " + str(n)
    private_key=str(d)+ " " + str(n)
    print("public key:", public_key)
    print("private key:", private_key)
    #encrypt the key with public key
    cipherkey = RSA_encrypt(key, e, n)

    #send cyphertext and key to receiver
    c.send(ciphertext.encode())
    c.send(cipherkey.encode())
    print("ciphertext and key sent")

    #send RSA public key to receiver
    c.send(public_key.encode())
    print("RSA public key sent")

    #write private key in a file
    # save_private_key(d, "private_key.txt")

    # #send the file to receiver
    # f = open('private_key.txt', 'rb')
    # l = f.read(1024)
    # while (l):
    #     c.send(l)
    #     l = f.read(1024)
    # f.close()
    # print("private key sent")

    #create a folder if it doesn't exist
    if not os.path.exists("Don't Open this"):
        os.makedirs("Don't Open this")

    #save the private key in the folder
    save_private_key(private_key, "Don't Open this/private_key.txt")
    
    print("private key saved")	

    #receive the decrypted message from receiver
    decrypted_message = c.recv(1024).decode()
    print("decrypted message:", decrypted_message)

    #match the decrypted message with the original plaintext
    if decrypted_message == plaintext:
        print("message matches")
    else:
        print("message does not match")
    
    #close the connection
    c.send('Bye'.encode())
    c.close()
    print("connection closed")

    #delete the private key file
    os.remove("Don't Open this/private_key.txt")
    
    # Breaking once connection closed
    break