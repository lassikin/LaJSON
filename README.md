
<p align="center">
  <img width="600" height="131" src="https://github.com/lassikin/LaJSON/raw/master/jsonlogo2.png">
</p>
*note: renamed from LaJSON to LassiJSON as there is a python library named LaJSON already and every other single two letters+JSON name seems taken already as well

# LassiJSON - Light Alternative JSON Library for Java
<center>
LassiJSON aims to provide an alternative library to parse and create JSON formatted data, with direct and unambigious mapping to JSON input or output. 
<a href=https://github.com/lassikin/LassiJSON/blob/master/FAQ.md> Please see the FAQ before using, it takes just a moment to skim over it, please. </a>
</center>
<h3>Project goals</h3>
<p>-At least as fast or faster than JSON.org, At least as fast or faster as GSON JSONParser, focused on bigger input and outputs</p>
<p>-Small (an under 20 kbyte addition)</p>
<p>-Single .java file with no extra dependencies</p>
<p>-Usable in most Java environments</p>
<p>-Unambigious serialization to JSON and back into LaJSON objects. (input for numbers should match output, even for cases where it will not be so for json.org.</p>
<p>-No additional dependencies to other libraries</p>
<p>-Obfuscation friendly (no need to configure proguard etc) </p>
<p>-<bold>Practicality</bold></p>

<a href=https://github.com/lassikin/LassiJSON/blob/master/FAQ.md> FAQ </a>

# Usage

see <a href=https://github.com/lassikin/LassiJSON/blob/master/manual.md> manual. </a>

# Reached goals
With larger inputs, faster than either json or gson.

# Installation / Requirements of the runtime system

Add the .jar library into your project. Alternatively you can simply just add the .java into your project(it's a single file, which is part of the project goals, 1 java file - WITH NO EXTRA DEPENDENCIES)

So just drop https://github.com/lassikin/LassiJSON/blob/master/sources/LaJSONLibrary/src/lassik/LaJSON/LaJSON.java this file into your project and you're good to go.

Maven and Gradle packages coming in the future

*note: renamed from LaJSON to LassiJSON as there is a python library named LaJSON already and every other single two letters+JSON name seems taken already as well
