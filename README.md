Oddjob SSH 
==========

SSH Oddjob Client Jobs. These are thin wrappers over Apache Mina SSHD.

This is a work in progress and is currently focused on the SSH required
to use Amazon Web Services.

It provides:
- A simple Exec Client.
- A simple SCP Client.

The only Authentication provided is:
- Password.
- Public Key.

Authentication Not yet supported: 
- Host Based.
- Keyboard Interactive.
- GSSAPI (Kerberos).

Oh, and the Client accepts all host keys, so this really isn't production
ready yet...

