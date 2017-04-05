# SecureCommunication
A simple system for two users to communicate securely.

This scenario is described as follows:

Communication scenario: Alice (as a Client) needs to send messages to Bob (as a Server). Either TCP or UDP is fine for the transport protocol. Both messages must be encrypted and integrity- protected. Each of Alice and Bob has a pair of <public key, private key> under the RSA cryptosystem (their key pairs are different), and they know each other’s public key beforehand (the public keys can be hard coded into the program or sent to each other).
