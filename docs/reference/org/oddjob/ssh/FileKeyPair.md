[HOME](../../../README.md)
# ssh:file-keypair

Provide a Key Pair from Open SSH format files.


This is the file format created with the 'ssh-keytool'
and defaults to 'id_rsa'. I think this form is also known as PEM, it's of the form
<pre>
-----BEGIN OPENSSH PRIVATE KEY-----
Base 64 stuff
-----END RSA PRIVATE KEY-----
</pre>
And not to be confused, as I did, with SSH2 format files hat begin `---- BEGIN SSH2` (note the space) and
can have headers in before the Base 64 stuff. Oddjob doesn't currently support these.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [keyFiles](#propertykeyFiles) | The files. | 
| [passphraseProvider](#propertypassphraseProvider) | Provide the passphrase if the file is password protected. | 


### Property Detail
#### keyFiles <a name="propertykeyFiles"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes, at least one..</td></tr>
</table>

The files.

#### passphraseProvider <a name="propertypassphraseProvider"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Maybe.</td></tr>
</table>

Provide the passphrase if the file is password protected.


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
