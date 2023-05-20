from ast import Constant, keyword
from hashlib import algorithms_available
from importlib.machinery import WindowsRegistryFinder
from pickle import TRUE
from BitVector import *
import time

Sbox = (
    0x63, 0x7C, 0x77, 0x7B, 0xF2, 0x6B, 0x6F, 0xC5, 0x30, 0x01, 0x67, 0x2B, 0xFE, 0xD7, 0xAB, 0x76,
    0xCA, 0x82, 0xC9, 0x7D, 0xFA, 0x59, 0x47, 0xF0, 0xAD, 0xD4, 0xA2, 0xAF, 0x9C, 0xA4, 0x72, 0xC0,
    0xB7, 0xFD, 0x93, 0x26, 0x36, 0x3F, 0xF7, 0xCC, 0x34, 0xA5, 0xE5, 0xF1, 0x71, 0xD8, 0x31, 0x15,
    0x04, 0xC7, 0x23, 0xC3, 0x18, 0x96, 0x05, 0x9A, 0x07, 0x12, 0x80, 0xE2, 0xEB, 0x27, 0xB2, 0x75,
    0x09, 0x83, 0x2C, 0x1A, 0x1B, 0x6E, 0x5A, 0xA0, 0x52, 0x3B, 0xD6, 0xB3, 0x29, 0xE3, 0x2F, 0x84,
    0x53, 0xD1, 0x00, 0xED, 0x20, 0xFC, 0xB1, 0x5B, 0x6A, 0xCB, 0xBE, 0x39, 0x4A, 0x4C, 0x58, 0xCF,
    0xD0, 0xEF, 0xAA, 0xFB, 0x43, 0x4D, 0x33, 0x85, 0x45, 0xF9, 0x02, 0x7F, 0x50, 0x3C, 0x9F, 0xA8,
    0x51, 0xA3, 0x40, 0x8F, 0x92, 0x9D, 0x38, 0xF5, 0xBC, 0xB6, 0xDA, 0x21, 0x10, 0xFF, 0xF3, 0xD2,
    0xCD, 0x0C, 0x13, 0xEC, 0x5F, 0x97, 0x44, 0x17, 0xC4, 0xA7, 0x7E, 0x3D, 0x64, 0x5D, 0x19, 0x73,
    0x60, 0x81, 0x4F, 0xDC, 0x22, 0x2A, 0x90, 0x88, 0x46, 0xEE, 0xB8, 0x14, 0xDE, 0x5E, 0x0B, 0xDB,
    0xE0, 0x32, 0x3A, 0x0A, 0x49, 0x06, 0x24, 0x5C, 0xC2, 0xD3, 0xAC, 0x62, 0x91, 0x95, 0xE4, 0x79,
    0xE7, 0xC8, 0x37, 0x6D, 0x8D, 0xD5, 0x4E, 0xA9, 0x6C, 0x56, 0xF4, 0xEA, 0x65, 0x7A, 0xAE, 0x08,
    0xBA, 0x78, 0x25, 0x2E, 0x1C, 0xA6, 0xB4, 0xC6, 0xE8, 0xDD, 0x74, 0x1F, 0x4B, 0xBD, 0x8B, 0x8A,
    0x70, 0x3E, 0xB5, 0x66, 0x48, 0x03, 0xF6, 0x0E, 0x61, 0x35, 0x57, 0xB9, 0x86, 0xC1, 0x1D, 0x9E,
    0xE1, 0xF8, 0x98, 0x11, 0x69, 0xD9, 0x8E, 0x94, 0x9B, 0x1E, 0x87, 0xE9, 0xCE, 0x55, 0x28, 0xDF,
    0x8C, 0xA1, 0x89, 0x0D, 0xBF, 0xE6, 0x42, 0x68, 0x41, 0x99, 0x2D, 0x0F, 0xB0, 0x54, 0xBB, 0x16,
)

InvSbox = (
    0x52, 0x09, 0x6A, 0xD5, 0x30, 0x36, 0xA5, 0x38, 0xBF, 0x40, 0xA3, 0x9E, 0x81, 0xF3, 0xD7, 0xFB,
    0x7C, 0xE3, 0x39, 0x82, 0x9B, 0x2F, 0xFF, 0x87, 0x34, 0x8E, 0x43, 0x44, 0xC4, 0xDE, 0xE9, 0xCB,
    0x54, 0x7B, 0x94, 0x32, 0xA6, 0xC2, 0x23, 0x3D, 0xEE, 0x4C, 0x95, 0x0B, 0x42, 0xFA, 0xC3, 0x4E,
    0x08, 0x2E, 0xA1, 0x66, 0x28, 0xD9, 0x24, 0xB2, 0x76, 0x5B, 0xA2, 0x49, 0x6D, 0x8B, 0xD1, 0x25,
    0x72, 0xF8, 0xF6, 0x64, 0x86, 0x68, 0x98, 0x16, 0xD4, 0xA4, 0x5C, 0xCC, 0x5D, 0x65, 0xB6, 0x92,
    0x6C, 0x70, 0x48, 0x50, 0xFD, 0xED, 0xB9, 0xDA, 0x5E, 0x15, 0x46, 0x57, 0xA7, 0x8D, 0x9D, 0x84,
    0x90, 0xD8, 0xAB, 0x00, 0x8C, 0xBC, 0xD3, 0x0A, 0xF7, 0xE4, 0x58, 0x05, 0xB8, 0xB3, 0x45, 0x06,
    0xD0, 0x2C, 0x1E, 0x8F, 0xCA, 0x3F, 0x0F, 0x02, 0xC1, 0xAF, 0xBD, 0x03, 0x01, 0x13, 0x8A, 0x6B,
    0x3A, 0x91, 0x11, 0x41, 0x4F, 0x67, 0xDC, 0xEA, 0x97, 0xF2, 0xCF, 0xCE, 0xF0, 0xB4, 0xE6, 0x73,
    0x96, 0xAC, 0x74, 0x22, 0xE7, 0xAD, 0x35, 0x85, 0xE2, 0xF9, 0x37, 0xE8, 0x1C, 0x75, 0xDF, 0x6E,
    0x47, 0xF1, 0x1A, 0x71, 0x1D, 0x29, 0xC5, 0x89, 0x6F, 0xB7, 0x62, 0x0E, 0xAA, 0x18, 0xBE, 0x1B,
    0xFC, 0x56, 0x3E, 0x4B, 0xC6, 0xD2, 0x79, 0x20, 0x9A, 0xDB, 0xC0, 0xFE, 0x78, 0xCD, 0x5A, 0xF4,
    0x1F, 0xDD, 0xA8, 0x33, 0x88, 0x07, 0xC7, 0x31, 0xB1, 0x12, 0x10, 0x59, 0x27, 0x80, 0xEC, 0x5F,
    0x60, 0x51, 0x7F, 0xA9, 0x19, 0xB5, 0x4A, 0x0D, 0x2D, 0xE5, 0x7A, 0x9F, 0x93, 0xC9, 0x9C, 0xEF,
    0xA0, 0xE0, 0x3B, 0x4D, 0xAE, 0x2A, 0xF5, 0xB0, 0xC8, 0xEB, 0xBB, 0x3C, 0x83, 0x53, 0x99, 0x61,
    0x17, 0x2B, 0x04, 0x7E, 0xBA, 0x77, 0xD6, 0x26, 0xE1, 0x69, 0x14, 0x63, 0x55, 0x21, 0x0C, 0x7D,
)

Mixer = [
    [BitVector(hexstring="02"), BitVector(hexstring="03"), BitVector(hexstring="01"), BitVector(hexstring="01")],
    [BitVector(hexstring="01"), BitVector(hexstring="02"), BitVector(hexstring="03"), BitVector(hexstring="01")],
    [BitVector(hexstring="01"), BitVector(hexstring="01"), BitVector(hexstring="02"), BitVector(hexstring="03")],
    [BitVector(hexstring="03"), BitVector(hexstring="01"), BitVector(hexstring="01"), BitVector(hexstring="02")]
]

InvMixer = [
    [BitVector(hexstring="0E"), BitVector(hexstring="0B"), BitVector(hexstring="0D"), BitVector(hexstring="09")],
    [BitVector(hexstring="09"), BitVector(hexstring="0E"), BitVector(hexstring="0B"), BitVector(hexstring="0D")],
    [BitVector(hexstring="0D"), BitVector(hexstring="09"), BitVector(hexstring="0E"), BitVector(hexstring="0B")],
    [BitVector(hexstring="0B"), BitVector(hexstring="0D"), BitVector(hexstring="09"), BitVector(hexstring="0E")]
]

def print_matrix(matrix):
    for i in range(len(matrix)):
        for j in range(len(matrix[i])):
            print(matrix[i][j].get_bitvector_in_hex(), end=", ")
        print()

def shift_list(l, n):
    result=[]
    for i in range(0,len(l)):
        result.append(l[(i+n+len(l))%len(l)])
    
    return result

def mix_columns(matrix, mixer_matrix, words, isValid):
     #Mix Columns
        # print(isValid)
        prod=[]
        if isValid==False:
            prod=matrix
        else:
            col=[]
            for j in range(0,4):
                l=[]
                col.append(l)
            
            for j in range(0,4):
                for k in range(0,4):
                    col[j].append(matrix[k][j])
            
            # print("col")
            # print_matrix(col)
            AES_modulus = BitVector(bitstring='100011011')
        
            for j in range(0,4):
                sum=BitVector(intVal=0,size=32)
                l=[]
                for k in range(0,4):
                    for p in range(0,4):
                        # print("col[",k,"][",p,"]=",col[k][p].get_bitvector_in_hex())
                        # print("Mixer[",j,"][",p,"]=",Mixer[j][p].get_bitvector_in_hex())
                        val = col[k][p].gf_multiply_modular(mixer_matrix[j][p], AES_modulus, 8)
                        if(p==0):
                            sum=val
                        else:
                            sum=sum^val

                    # print("sum: ",sum.get_bitvector_in_hex())
                    l.append(sum) 
                prod.append(l)
                   

        #print(prod)
        #update words from prod
        for j in range(0,4):
            words[j]=BitVector(intVal=0,size=32)
            for k in range(0,4):
                # print("prod[",k,"][",j,"]=", prod[k][j])
               
                words[j]=(words[j]<<8)|prod[k][j]
                
                # print("words[",j,"]=",words[j])
        
        # print("words at round")
        # for j in range(0,4):
        #     print(words[j].get_bitvector_in_hex())

        return words
        
        #print("words after round",i+1,"=",words)
#AES encryption algorithm

def split_into_bytes(bv):

    words=[]
    # print(bv.get_bitvector_in_hex())
    for i in range(0, 4):
        temp=bv.deep_copy()
        val=(temp>>(i*8))&(BitVector(hexstring='FF'))
        words.append(val)
        # print(val.get_bitvector_in_hex())
    
    words.reverse()
    return words

def get_round_constant(round_num):
    if round_num == 1:
        return BitVector(hexstring='1')
    elif round_num == 2:
        return BitVector(hexstring='2')
    elif round_num == 3:
        return BitVector(hexstring='4')
    elif round_num == 4:
        return BitVector(hexstring='8')
    elif round_num == 5:
        return BitVector(hexstring='10')
    elif round_num == 6:
        return BitVector(hexstring='20')
    elif round_num == 7:
        return BitVector(hexstring='40')
    elif round_num == 8:
        return BitVector(hexstring='80')
    elif round_num == 9:
        return BitVector(hexstring='1B')
    elif round_num == 10:
        return BitVector(hexstring='36')

def g_function(val, round):
    #circular byte left shift
    # print(val.get_bitvector_in_hex())

    intval=val.intValue();

    for i in range(0,8):
        msb=intval&(1<<31)
        intval = (intval << 1) | (msb>>31)
        temp=BitVector(intVal=intval)
        # print(temp,i)
    
    intval = intval&(0xFFFFFFFF)
    temp=BitVector(intVal=intval,size=32)
    # print(temp.get_bitvector_in_hex())

    words=split_into_bytes(temp)
    # print(words)

    s_box_words=[]

    for i in range(0,4):
        s_box_words.append(BitVector(intVal=Sbox[words[i].intValue()], size=8))
        # print(s_box_words[i].get_bitvector_in_hex())

    round_constant=get_round_constant(round)
    # print(round_constant.get_bitvector_in_hex())
    s_box_words[0]^=round_constant
    # print(s_box_words[0].get_bitvector_in_hex())

    merged=BitVector(intVal=s_box_words[0].intValue(),size=32)
    for i in range(1,4):
        merged=(merged<<8)|s_box_words[i]
    # print("merged =", merged.get_bitvector_in_hex())
    
    return merged

def split_into_four_words(bv):

    words=[]
    for i in range(0, 4):
        temp=bv.deep_copy()
        val=(temp>>(i*32))&(BitVector(hexstring='FFFFFFFF'))
        words.append(val)
        #print(val)
    
    words.reverse()
    return words
   

def generate_round_keys(key, no_of_rounds):
    round_keys=[]
    init_key = BitVector(textstring=key)
    # print(init_key.get_bitvector_in_hex())
    # print("\n")
    round_keys.append(init_key)

    for i in range(1,no_of_rounds):
        words=split_into_four_words(round_keys[i-1])
        # print(words)
        g_val=g_function(words[3], i)
        w=[]
        w.append(words[0]^g_val)
        w.append(words[1]^w[0])
        w.append(words[2]^w[1])
        w.append(words[3]^w[2])

        merged=BitVector(intVal=w[0].intValue(),size=128)
        for j in range(1,4):
            merged=(merged<<32)|w[j]
        print("round_key :",i,"=", merged.get_bitvector_in_hex())

        round_keys.append(merged)


    return round_keys


def AES_encrypt(text, round_keys):
    
    #print(round_keys)
    # print("\n")

    init_text = BitVector(textstring=text)
    print(init_text.get_bitvector_in_hex())
    print("\n")

    #init_text = init_text.pad_from_right(128)
    print(init_text.get_bitvector_in_hex())
    print("\n")
    words=split_into_four_words(init_text)
    #print(words)
    

    for i in range(0,11):
        key_words=split_into_four_words(round_keys[i])

        #XOR with round key
        for j in range(0,4):
            words[j]=words[j]^key_words[j]
            print(words[j].get_bitvector_in_hex())

        if i==10:
            break

        matrix=[]
        for k in range(0,4):
            l=[]
            matrix.append(l)

        #S-box
        for j in range(0,4):
            bytes=split_into_bytes(words[j])
            for k in range(0,4):
                bytes[k]=BitVector(intVal=Sbox[bytes[k].intValue()], size=8)
                matrix[k].append(bytes[k])
                # print(bytes[k].get_bitvector_in_hex())

        #print(matrix)
        

        #Shift Rows
        for j in range(1,4):
            matrix[j]=shift_list(matrix[j], j)
        

        #print("matrix",matrix)
        

        #Mix Columns
        isValid=True
        if i==9:
            isValid=False
        words=mix_columns(matrix,Mixer,words,isValid)
        
        #print("words after round",i+1,"=",words)
    
    #merge words
    merged=BitVector(intVal=words[0].intValue(),size=128)
    for i in range(1,4):
        merged=(merged<<32)|words[i]
    print("cypher_text =", merged.get_bitvector_in_hex())

    return merged.get_bitvector_in_hex()

def AES_decrypt(cypher_text, round_keys):
    
    init_text = BitVector(hexstring=cypher_text)

    words=split_into_four_words(init_text)
    key_words=split_into_four_words(round_keys[10])
    for i in range(0,4):
        # print("words 1\n",words[i].get_bitvector_in_hex())
        words[i]^=key_words[i]

    # for i in range(0,4):
    #     print("words 2\n",words[i].get_bitvector_in_hex())
        

    for i in range(9,-1,-1):
        #convert columns to rows
        matrix=[]
        for j in range(0,4):
            l=[]
            matrix.append(l)
        
             
        for j in range(0,4):
            bytes=split_into_bytes(words[j])
            for k in range(0,4):
                bytes[k]=BitVector(intVal=bytes[k].intValue(), size=8)
                matrix[k].append(bytes[k])
        
        # print("matrix")
        # print_matrix(matrix)

        
        
        #inverse shift rows
        for j in range(1,4):
            matrix[j]=shift_list(matrix[j], 4-j)

        # print("matrix after inv byte shift")
        # print("matrix")
        # print_matrix(matrix)

        #inverse s-box
        for j in range(0,4):
            for k in range(0,4):
                matrix[k][j]=BitVector(intVal=InvSbox[matrix[k][j].intValue()], size=8)
        
        # print("matrix after inv S-box")
        # print_matrix(matrix)

        #XOR with round key
        print("key:",round_keys[i].get_bitvector_in_hex())
        key_words=split_into_four_words(round_keys[i])
        
        key_matrix=[]
        for j in range(0,4):
            l=[]
            key_matrix.append(l)

        for j in range(0,4):
            col_split=split_into_bytes(key_words[j])
            for k in range(0,4):
                key_matrix[k].append(col_split[k])

        # print("key matrix")
        # print_matrix(key_matrix)

        for j in range(0,4):
            for k in range(0,4):
                matrix[j][k]^=key_matrix[j][k]
        
        # print("matrix after adding round key")
        # print_matrix(matrix)

        #inverse mix columns
        isValid=True
        if i==0:
            isValid=False
        words=mix_columns(matrix,InvMixer,words,isValid)

    #merge words
    merged=BitVector(intVal=words[0].intValue(),size=128)
    for i in range(1,4):
        merged=(merged<<32)|words[i]
    print("decypherd_text =", merged.get_bitvector_in_ascii())

    return merged.get_bitvector_in_ascii()

def split_into_128_bit_blocks(text):
    blocks=[]
    for i in range(0,text.__len__(),16):
        blocks.append(text[i:i+16])
    return blocks

def pad_to_128_bit_block(text):
    val=text.__len__()*8
    pad_bits=128-val%128
    pad_bytes=pad_bits//8

    if pad_bytes == 0:
        pad_bytes=16

    for i in range(0,pad_bytes):
        text+=chr(pad_bytes)

    print("pad_bits=",pad_bits)
    print("pad_bytes=",pad_bytes)
    print("padded text=",text)

    return text

def remove_padding(text):
    pad_bytes=ord(text[-1])
    print("pad_bytes=",pad_bytes)
    text=text[:-pad_bytes]
    print("removed padding text=",text)
    return text

def AES_encrypt_whole(text, round_keys):
    text=pad_to_128_bit_block(text)
    blocks=split_into_128_bit_blocks(text)
    cipher_text=''
    for block in blocks:
        cipher_text+=AES_encrypt(block, round_keys)
    return cipher_text

def AES_decrypt_whole(text, round_keys):
    text=BitVector(hexstring=text).get_bitvector_in_ascii()
    blocks=split_into_128_bit_blocks(text)
    plain_text=''
    for block in blocks:
        plain_text+=AES_decrypt(BitVector(textstring=block).get_bitvector_in_hex(),round_keys)
    plain_text=remove_padding(plain_text)
    return plain_text
    
#main code
# AES_encrypt("Two One Nine Two", "Thats my Kung Fu")
#sample code
# text="CanTheyDoTheirFest"
# key="BUET CSE17 Batch"

# cypher_text=AES_encrypt_whole(text, key)
# print("cypher_text=",cypher_text)

# cypher_ascii=BitVector(hexstring=cypher_text).get_bitvector_in_ascii()
# decrypted_text=AES_decrypt_whole(cypher_ascii, key)
# print("decrypted_text=",decrypted_text)


# text=pad_to_128_bit_block(text)
# blocks=split_into_128_bit_blocks(text)
# print("blocks=",blocks)

# cypher_text=""

# for i in blocks:
#     cypher_text+=AES_encrypt(i,key)

# print("cypher text=",cypher_text)

# cypher_ascii=BitVector(hexstring=cypher_text).get_bitvector_in_ascii()
# print("cypher_ascii=",cypher_ascii)
# cyphered_blocks=split_into_128_bit_blocks(cypher_ascii)

# decrypted_text=""
# for i in cyphered_blocks:
#     decrypted_text+=AES_decrypt(BitVector(textstring=i).get_bitvector_in_hex(),key)

# print("decrypted text=",decrypted_text)

# without_padding_text=remove_padding(decrypted_text)
    
# cypher_text=AES_encrypt("CanTheyDoTheirFest", "BUET CSE17 Batch")
# print(cypher_text)
# AES_decrypt(cypher_text, "BUET CSE17 Batch")

#time related metrics
def time_metrics():
    text="CanTheyDoTheirFe"
    key="BUET CSE17 Batch"

    start=time.time_ns()
    round_keys = generate_round_keys(key, no_of_rounds=11)
    end=time.time_ns()

    print("time taken to generate round keys=",(end-start)," ns")

    start=time.time_ns()
    cypher_text=AES_encrypt_whole(text, round_keys)
    end=time.time_ns()

    print("time taken to encrypt whole text=",(end-start)," ns")

    start=time.time_ns()
    decrypted_text=AES_decrypt_whole(cypher_text, round_keys)
    end=time.time_ns()

    print("time taken to decrypt the ciphertext: ",(end-start)," ns")


time_metrics()






