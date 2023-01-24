# FileVault

An encrypted file container utility designed to store files and directories containing sensitive data securely with a powerful encryption algorithm (AES-256) inside a vault (an encrypted container). 

Encrypted files can be stored locally and shared with others securely. The encrypted files can be decrypted and saved to the local machine via this application.

The application emulates a MacOSX Finder view allowing the user to add, delete, and save files while encryption and decryption occur in the background.

## Features

* AES-GCM algorithm requires two parameters:
  1. A key:
  AES secret key is generated from a user-defined password with salt using PBKDF2 and hashed using HMAC SHA-256, which will then be used for encryption and decryption
  2. A unique IV:
  Unique 128-bit IV maximizes the time or memory required for a brute force attack

* 128-bit Salt:
  - There will be as many as 2^128 keys for each password, therefore increasing the difficulty of rainbow attacks
  - Prevents pre-computation attacks

* Iteration count of 100,000: increases difficulty and slows down the speed of attacks

## Motivation

As the world becomes more connected and complex and our dependence on information technology increases, there will be an increased risk of more disastrous cyber-attacks. In today’s data-driven world, the prevalence of privacy and cybersecurity threats accountable for the majority of sensitive data breaches highlights the necessity of sufficiently secure encryption. Transferring sensitive data over unencrypted mediums enables and increases the risk of unauthorized access to the data. 

Moreover, the current FileVault on macOS takes a long time to set up and its encryption can also slow down your computer as it encrypts everything on your machine. 

This application was designed to encourage data security and compensate for these limitations by ensuring flexibility and fast encryption.
