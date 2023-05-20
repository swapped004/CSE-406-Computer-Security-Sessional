import random
from ast import Constant, keyword
from hashlib import algorithms_available
from importlib.machinery import WindowsRegistryFinder
from BitVector import *
import time

iter = 5
random.seed(10)

def gcd(p,q):
# Create the gcd of two positive integers.
    while q != 0:
        temp=q
        q=p%q
        p=temp
    return p

def is_coprime(x, y):
    return gcd(x, y) == 1


def generate_e(phi):
    # Generate a random number between 1 and phi
    # return a number
    while True:
        e = random.randint(1, phi)
        if is_coprime(e, phi) == True:
            return e


def modulo(a,temp,p):
    # Calculate (a ^ temp) % p
    # return the result
    res = 1
    a = a % p
    while temp > 0:
        if temp % 2 == 1:
            res = (res * a) % p
        a = (a * a) % p
        temp = temp // 2
    return res

def is_prime(p,iteration):
    # Check if num is prime
    # return True or False
    if p<2:
        return False
    if p!=2 and p%2==0:
        return False
    
    s=p-1
    while s%2==0:
        s/=2
    
    for i in range(0,iteration):
        a=random.randint(2,p-1)
        temp=s
        mod=modulo(a,temp,p)
        while temp!=p-1 and mod!=1 and mod!=p-1:
            mod=(mod*mod)%p
            temp*=2
        if mod!=p-1 and temp%2==0:
            return False
    return True
    
    
def gen_rand_prime(low, high):
    # Generate a random prime number between low and high
    # low and high are included
    # return a prime number
    while True:
        num = random.randint(low, high)
        if num%2==0:
            num+=1
        # print("num:", num, "low:", low, "high:", high)

        temp=BitVector(intVal=num)
        val=temp.test_for_primality()
        # print("val:", val)

        if val!=0:
            return num

def mod_inverse(a, m):
    # Calculate the modular multiplicative inverse of a
    # return the inverse
    p=BitVector(intVal=a)
    q=BitVector(intVal=m)
    r=BitVector.multiplicative_inverse(p,q)

    return r.int_val()

def key_pair_generation(k):  # p and q are two large prime numbers
    #randomly generate p and q
    k/=2
    p = gen_rand_prime(pow(2,k-1),pow(2,k)-1)
    q = gen_rand_prime(pow(2,k-1),pow(2,k)-1)

    print("p:", p, "q:",q)

    n = p * q
    phi = (p - 1) * (q - 1)
    e = generate_e(phi)
    print("e:", e)	
    d = mod_inverse(e, phi)
    print("d:", d)	

    print("p:", p, "q:", q, "n:", n, "phi:", phi, "e:", e, "d:", d)
    print("Public key: (", e, ",", n, ")")
    print("Private key: (", d, ",", n, ")")

    
    return (n, e, d)


def RSA_encrypt(plaintext,e,n):
    #split plaintext into characters
    #encrypt each character
    #return the encrypted text
    ciphertext = ""
    for i in plaintext:
        ciphertext += str(modulo(ord(i),e,n)) + ","
    
    ciphertext=ciphertext[:-1]
    return ciphertext

def RSA_decrypt(ciphertext,d,n):
    #split ciphertext into characters
    #decrypt each character
    #return the decrypted text
    plaintext = ""
    for i in ciphertext.split(","):
        plaintext += chr(modulo(int(i),d,n))
    return plaintext


#sample code

# (n,e,d)=key_pair_generation(32)
# cypher_text=RSA_encrypt("CanTheyArrangeTheFest?",e,n)
# print("cypher text:", cypher_text)
# plain_text=RSA_decrypt(cypher_text,d,n)
# print("plain text:", plain_text)

#time related metrics

def time_metric():

    i=16
    while True:
        print("size of key: ",i)
        start=time.time_ns()
        (n,e,d)=key_pair_generation(i)
        end=time.time_ns()
        print("time taken to generate key-pair:", end-start, " ns")	

        start=time.time_ns()
        cypher_text=RSA_encrypt("CanTheyArrangeTheFest?",e,n)
        end=time.time_ns()
        # print("cypher text:", cypher_text)
        print("time taken to encrypt in RSA:", end-start, " ns")
        

        start=time.time_ns()
        plain_text=RSA_decrypt(cypher_text,d,n)
        end=time.time_ns()
        # print("plain text:", plain_text)
        print("time taken to decrypt in RSA:", end-start, " ns\n\n\n\n")

        i=i*2
        if i>256:
            break
    

time_metric()