[HOME](../../../README.md)
# ssh:scp

A very basic SCP job. Copies a single file to or from
the remote host or both, or not at all if from and to are missing.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [connection](#propertyconnection) | The Remote Connection. | 
| [from](#propertyfrom) | The path of the file to copy from the remote. | 
| [name](#propertyname) | The name of the job. | 
| [remote](#propertyremote) | The remote file name. | 
| [to](#propertyto) | The path of the file to copy to the remote. | 


### Property Detail
#### connection <a name="propertyconnection"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The Remote Connection. This will be automatically
injected if this is the child of an [ssh:cascade](../../../org/oddjob/ssh/SshSequenceJob.md).

#### from <a name="propertyfrom"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The path of the file to copy from the remote.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The name of the job. Can be any text.

#### remote <a name="propertyremote"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The remote file name.

#### to <a name="propertyto"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The path of the file to copy to the remote.


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
