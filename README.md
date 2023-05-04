# FileVault

## Description

This application is designed to compensate for the limitations of the current version of Mac's FileVault, which can be time-consuming to set up and can slow down the computer due to its encryption process. The application is a faster and more efficient encryption process using advanced algorithms while minimizing any negative impact on computer performance. 

The application utilizes the powerful AES-256 encryption algorithm to create a secure vault in which files and directories containing sensitive data can be stored.

The application emulates that of a file manager, enabling the user to add, delete, and open files within the secure container while encryption and decryption occur in the background to optimize security and flexibility.

The user can create secure vault(s), within which each file added to a vault will undergo robust encryption, thereby ensuring the safety of sensitive data.

The application provides the functionality to decrypt and save encrypted files to the local machine, ensuring that the user can access their data when needed.

## Objective

* To provide a flexible and efficient solution for data security that compensates for the limitations of the current version of Mac's FileVault.

* To encourage greater awareness and adoption of data security through its user-friendly features.

## Motivation

As our world becomes more interconnected and reliant on technology, the likelihood of catastrophic cyber-attacks grows. The increasing use of digital technologies, the rise of remote work and cloud computing have created new opportunities for cyber attacks, making it more important than ever to prioritize cybersecurity measures. In today's data-driven landscape, the prevalence of privacy and cybersecurity threats - which account for the majority of sensitive data breaches - underscores the vital importance of robust encryption. Failure to employ sufficiently secure encryption protocols when transmitting sensitive data via unencrypted channels significantly increases the risk of unauthorized data access.

## Security Algorithm Details

* AES-GCM algorithm requires two parameters:
  1. A key:
  AES secret key is generated from a user-defined password with salt using PBKDF2 and hashed using HMAC SHA-256, which will then be used for encryption and decryption
  2. A unique IV:
  Unique 128-bit IV maximizes the time or memory required for a brute force attack

* 128-bit Salt:
  - There will be as many as 2^128 keys for each password, therefore increasing the difficulty of rainbow attacks
  - Prevents pre-computation attacks

* Iteration count of 100,000: increases difficulty and slows down the speed of attacks
