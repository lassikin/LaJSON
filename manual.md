<h1>Basic usage</h1>

Parse a json string into a LaJSON object:<br>
LaJSON lajson = new LaJSON(testInput);<br>
<br><br>
Put in a basic value:<br>
  lajson.putString("this is name","this is value");<br>
<br><br>
Parse LaJSON into a json string:<br>
  String output = lajson.toString();<br>
<br><br>
Create an array and put it in:<br>
  LaJSON jsonArray= LaJSON.newArray();<br>
  jsonArray.addNumber(3234.323);<br>
  lajson.putLaJSON("array", jsonArray);<br>
  <br><br>

<br>
<p>When working with an LaJSON array, use the methods prefixed with "add" to add in array elements. With regular LaJSON name-value pairs use put-prefixed methods.</p>
<br>
<br>



  <b>take special care to not create a circular loop with the json objects, use .clone() to create a clone if you need a copy(clone) that doesn't reference the original objects contents. At the moment clone serializes and parses it again and has no references to the original LaJSON object anymore.</b> You can safely have the same LaJSON at different places of your hierarchy however as there is no circular reference loops.

