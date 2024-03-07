[HOME](../../../README.md)
# ssh:exec

Runs a remote SSH Exec command.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [command](#propertycommand) | The command to run. | 
| [connection](#propertyconnection) | The Remote Connection. | 
| [name](#propertyname) | The name of the job. | 
| [redirectStderr](#propertyredirectStderr) | Merge stderr into stdout. | 
| [stderr](#propertystderr) | An output to where stderr of the proces will be written. | 
| [stdin](#propertystdin) | An input stream which will act as stdin for the process. | 
| [stdout](#propertystdout) | An output to where stdout for the process will be written. | 


### Property Detail
#### command <a name="propertycommand"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The command to run.

#### connection <a name="propertyconnection"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The Remote Connection. This will be automatically
injected if this is the child of an [ssh:cascade](../../../org/oddjob/ssh/SshSequenceJob.md).

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The name of the job. Can be any text.

#### redirectStderr <a name="propertyredirectStderr"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

Merge stderr into stdout.

#### stderr <a name="propertystderr"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

An output to where stderr
of the proces will be written.

#### stdin <a name="propertystdin"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

An input stream which will
act as stdin for the process.

#### stdout <a name="propertystdout"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

An output to where stdout
for the process will be written.


-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
