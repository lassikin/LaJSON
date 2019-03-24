

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
  <b>take special care to not create a circular loop with the json objects, use .clone() to create a clone if you need a copy in the same hieararchy.</b>

