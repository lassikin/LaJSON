LaJSON faq:
<br><br>
<h4>Is it really faster than json.org implementation?</h4>
  Depends on the input. On extremely long inputs at the moment it is slower. Serializing into a string is however always faster as per our testing(up to doing serializing in 1/3rd of the time).
  <br>
  <br>
  The actual speed depends a lot on how the json branches, if you have multithreading on when parsing large inputs and the tree model threads nicely then LaJSON can be significantly faster than other more popular JSON libraries. On small files this is detrimental. The library has threshold character sizes which can be adjusted for when threading kicks into action.
  <br><br>
  The USAGE of the library is still synchronous, the threading happens internally and is meant to happen internally. Async methods might be added in the future.
  <br><br>

<h4>Why?</h4>
  Why not. Also this provides some advantages.
  <br><br>

<h4>Why is null it's own type?</h4>
  So you know that the input json had null in it. It is an explicit type in json and this approach is meant to avoid confusion. You will understand it one day if a server returns you null somewhere where you thought it wouldn't.
  <br><br>

<h4>Why all numbers are a bigdecimal?</h4>
  For now it is for not losing data. There are access methods for reading as doubles, ints and such. Also this makes it more feasible to use it when dealing with money. I feel this is more consistent than storing a number as int if it has no decimals and being an int internally after that and vice versa for a decimal. This also makes it more safer when reading in prices and such.
  <br><br>

<h4>Why aren't there more exceptions for everything that could feasibly go wrong?</h4>
  It's a practicality thing. If your json source has a habit of producing json with unpredictable content, you should check that it matches in type to what you're expecting. If something goes _terribly_ wrong an exception will be thrown of course, but it is not explicitly stated so you don't need to wrap everything in a try catch. Whether you like this or not is up to you. It's not like you're usually doing anything terribly useful with JSONExceptions in json.org anyways. If a file is a partial transfer some attempt at parsing it is made unlike with other parsers.
  <br><br>

<h4>There's no auto-serialization mappers?</h4>
  Possibly in the future a seralization interface that objects can implement might be added - but json can contain anything, even different types in the same array - or in a typical use case the server could return in cases a json that totally can't be mapped to what you thought you were going to map it into anyways - when this happens it isn't fun to fix it up to take into account the different json data, even if the original code looked very elegeant and tempting.
  <br><br>
