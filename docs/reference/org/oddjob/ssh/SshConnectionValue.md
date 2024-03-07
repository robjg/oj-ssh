[HOME](../../../README.md)
# ssh:connection

Provide an SSH connection.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [host](#propertyhost) | The host to connect to. | 
| [keyIdentityProvider](#propertykeyIdentityProvider) | Provides a key for Public Key Authentication. | 
| [passwordProvider](#propertypasswordProvider) | Provide a password for password authentication. | 
| [port](#propertyport) | The port. | 
| [timeout](#propertytimeout) | Timeout when trying to connect. | 
| [user](#propertyuser) | The user for the connection. | 


### Property Detail
#### host <a name="propertyhost"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The host to connect to.

#### keyIdentityProvider <a name="propertykeyIdentityProvider"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Provides a key for Public Key Authentication.

#### passwordProvider <a name="propertypasswordProvider"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Provide a password for password authentication.

#### port <a name="propertyport"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to 22.</td></tr>
</table>

The port.

#### timeout <a name="propertytimeout"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to 60 seconds.</td></tr>
</table>

Timeout when trying to connect.

#### user <a name="propertyuser"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The user for the connection.


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
