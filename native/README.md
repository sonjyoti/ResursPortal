# native/, C/C++ Modules for v2

This directory contains the C/C++ native modules that will be called from Java via JNA (Java Native Access) in v2.

## Planned Modules

### 1. PII Encryption: `libresurs_crypto.so`

AES-256-GCM encryption of sensitive information (organization numbers, personal information, and financial data) on the hot path.

**Purpose:** In v1, the company name, organization number, and authorized signatory are stored in plain text (`ApplicationController.java`, comment: `// TODO: encrypt PII before go-live`). The encryption key must be stored separately from the database, not in the same storage location as the ciphertext.

**Functions:**

```c
// Encrypt PII string
int resurs_encrypt_pii(
    const char* plaintext,
    const unsigned char* key,       // 32 bytes (AES-256)
    const unsigned char* nonce,     // 12 bytes (GCM)
    unsigned char* ciphertext_out,
    size_t* ciphertext_len
);

// Decrypt PII string
int resurs_decrypt_pii(
    const unsigned char* ciphertext,
    size_t ciphertext_len,
    const unsigned char* key,
    const unsigned char* nonce,
    char* plaintext_out,
    size_t* plaintext_len
);
```

**JNA Bridge (Java):**

```java
public interface ResursCryptoLibrary extends Library {
    ResursCryptoLibrary INSTANCE = Native.load("resurs_crypto", ResursCryptoLibrary.class);

    int resurs_encrypt_pii(
        String plaintext,
        byte[] key,
        byte[] nonce,
        byte[] ciphertextOut,
        IntByReference ciphertextLen
    );
}
```

**Key Storage:**

* The key is stored separately from the database (HashiCorp Vault or AWS KMS)
* A new nonce is generated for each encryption operation and stored together with the ciphertext

### 2. Audit Signing: `libresurs_audit.so`

Secure signing of the audit log using hash chains to detect tampering after the fact.

**Purpose:** In v1, the audit log is unsigned (a JSON blob in a column without an index). A row could be modified or deleted later without being detected. In v2, each audit entry should be hashed together with the hash of the previous entry (hash chain) and signed, so that manipulation of an individual entry or the ordering of the chain can be detected during verification.

**Functions:**

```c
// Calculate the hash for an audit entry and chain it to the previous entry
int resurs_audit_chain_entry(
    const unsigned char* prev_hash,     // 32 bytes, SHA-256 of previous entry (NULL for first entry in the chain)
    const char* entry_json,             // audit entry content: timestamp, rule ID, input, outcome
    size_t entry_len,
    unsigned char* hash_out,            // 32 bytes, SHA-256(prev_hash || entry_json)
    unsigned char* signature_out,        // digital signature of hash_out
    size_t* signature_len
);

// Verify a chain of audit entries, finding the first manipulated entry if any
int resurs_audit_verify_chain(
    const unsigned char* hashes,        // entry_count * 32 bytes, hashes in chain order
    const unsigned char* signatures,    // signatures in the same order
    const size_t* signature_lens,
    size_t entry_count,
    const unsigned char* public_key,
    int* first_invalid_index            // -1 if chain is valid, otherwise index of first manipulated entry
);
```

**JNA Bridge (Java):**

```java
public interface ResursAuditLibrary extends Library {
    ResursAuditLibrary INSTANCE = Native.load("resurs_audit", ResursAuditLibrary.class);

    int resurs_audit_chain_entry(
        byte[] prevHash,
        String entryJson,
        int entryLen,
        byte[] hashOut,
        byte[] signatureOut,
        IntByReference signatureLen
    );

    int resurs_audit_verify_chain(
        byte[] hashes,
        byte[] signatures,
        int[] signatureLens,
        int entryCount,
        byte[] publicKey,
        IntByReference firstInvalidIndex
    );
}
```

**Key Storage:**

* The signing key (private key) is stored separately from the database, following the same principle as PII encryption
* The public key can be freely distributed for verification, for example to auditors or regulatory authorities

## Compilation

```bash
# PII encryption (requires libssl-dev)
gcc -shared -fPIC -o libresurs_crypto.so resurs_crypto.c -lssl -lcrypto

# Audit signing (requires libssl-dev)
gcc -shared -fPIC -o libresurs_audit.so resurs_audit.c -lssl -lcrypto
```

## JNA Integration Guide

1. Add JNA to `pom.xml`:

```xml
<dependency>
    <groupId>net.java.dev.jna</groupId>
    <artifactId>jna</artifactId>
    <version>5.13.0</version>
</dependency>
```

2. Place the `.so` files in `/usr/local/lib/` or specify the path using `-Djna.library.path`

3. Define Java interfaces that extend `Library`

4. Load the libraries using:

```java
Native.load("resurs_crypto", ResursCryptoLibrary.class)
```

and:

```java
Native.load("resurs_audit", ResursAuditLibrary.class)
```

## Status

* [ ] `libresurs_crypto.so` — not implemented (v2)
* [ ] `libresurs_audit.so` — not implemented (v2)
* [ ] JNA bridge — not implemented (v2)
