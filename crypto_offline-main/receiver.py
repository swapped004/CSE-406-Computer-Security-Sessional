
# Import socket module
import socket 
from offline import *
from rsa import *    
import struct
import os

def read_private_key(filename):
    with open(filename, 'r') as f:
        private_key = f.read()
    return private_key
 
# Create a socket object
s = socket.socket()        
 
# Define the port on which you want to connect
port = 12345               
 
# connect to the server on local computer
s.connect(('127.0.0.1', port))
 
# receive data from the server and decoding to get the string.
# print (s.recv(1024).decode())

#receive cyphertext and key from sender
ciphertext = s.recv(1024).decode()
cipherkey = s.recv(1024).decode()

print("ciphertext:", ciphertext)
print("cipherkey:", cipherkey)

#receive RSA public key from sender
public_key = s.recv(1024).decode()
n=int(public_key.split(" ")[1])
e=int(public_key.split(" ")[0])

print("n:", n, "e:", e)

#check if there is a private key file in "Don't Open this" folder
while True:
    if os.path.exists("Don't Open this/private_key.txt"):
        break


#read private key from file
private_key = read_private_key("Don't Open this/private_key.txt")
print("read the private key:", private_key)

d=int(private_key.split(" ")[0])

print("n:", n, "d:", d)

#decrypt the key with private key
key = RSA_decrypt(cipherkey, d, n)

print("key:", key)

#decrypt the cyphertext with key
# cypher_ascii=BitVector(hexstring=ciphertext).get_bitvector_in_ascii()
round_keys=generate_round_keys(key, no_of_rounds=11)
plaintext = AES_decrypt_whole(ciphertext, round_keys)
print("decrypted message:", plaintext)

#send the decrypted message to sender
s.send(plaintext.encode())



# close the connection
s.close()    
     