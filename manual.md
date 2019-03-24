

Parse a json string into a LaJSON object:
LaJSON lajson = new LaJSON(testInput);

Put in a basic value:
  lajson.putString("this is name","this is value");

Parse LaJSON into a json string:
  String output = lajson.toString();

Create an array and put it in:
  LaJSON jsonArray= LaJSON.newArray();
  jsonArray.addNumber(3234.323);
  lajson.putLaJSON("array", jsonArray);
  
**take special care to not create a circular loop.

