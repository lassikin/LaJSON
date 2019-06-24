package lassi.kinnunen;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lassi.kinnunen.utilities.Lal;
import lassik.LassiJSON.LassiJSON;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import static java.lang.System.exit;


public class Main {


    public static void printKeys(LassiJSON la, String prefix) {
        if (la.isArray()) {
            for (int n = 0; n < la.arraySize(); n++) {
                Lal.l(prefix + la.getValue(n).toString());

            }


        } else
            for (String key : la.values.keySet()) {


                if (la.getValue(key).type == LassiJSON.LaJSONDataType.JSONOBJECT || la.getValue(key).type == LassiJSON.LaJSONDataType.ARRAY) {
                    Lal.l(prefix + "" + key + ":");
                    printKeys(la.getValue(key).getLaJSON(), prefix + "--*");
                } else {
                    Lal.l(prefix + "" + key + ": " + la.getValue(key).getString());

                }

            }

    }

    public static int countkeys(LassiJSON la) {
        int count = 0;

        if (la.isArray()) {
            for (int n = 0; n < la.arraySize(); n++) {
                LassiJSON.Value value = la.getValue(n);
                if (value.type == LassiJSON.LaJSONDataType.ARRAY || value.type == LassiJSON.LaJSONDataType.JSONOBJECT) {
                    count += countkeys(value.getLaJSON());
                }
                count++;
            }
        } else
            for (String key : la.values.keySet()) {
                if (la.getValue(key).type == LassiJSON.LaJSONDataType.JSONOBJECT || la.getValue(key).type == LassiJSON.LaJSONDataType.ARRAY) {
                    count += countkeys(la.getValue(key).getLaJSON());//    printKeys(la.getValue(key).getLaJSON(), prefix + "--*");
                }
                count++;
            }
        return count;
    }

    public static int countkeys(JsonElement tree) {
        int count = 0;

        if (tree.isJsonArray()) {
            JsonArray array = tree.getAsJsonArray();
            for (int n = 0; n < array.size(); n++) {
                count++;
                JsonElement elem = array.get(n);
                if (elem.isJsonArray() || elem.isJsonObject()) {
                    count += countkeys(elem);
                }
            }
        } else {
            for (String key : tree.getAsJsonObject().keySet()) {
                JsonElement elem = tree.getAsJsonObject().get(key);
                if (elem.isJsonArray() || elem.isJsonObject()) {
                    count += countkeys(elem);
                }
                count++;
            }
        }
        return count;
    }


    public static int countkeys(JSONArray tree) {
        int count = 0;

        for (int n = 0; n < tree.length(); n++) {
            try {
                count += countkeys(tree.getJSONArray(n));
            } catch (JSONException e) {
                try {
                    count += countkeys(tree.getJSONObject(n));
                } catch (JSONException ee) {
                }
            }
            count++;
        }

        return count;
    }


    public static int countkeys(JSONObject tree) {
        int count = 0;


        for (String key : tree.keySet()) {
            //Lal.l(""+ key);

            try {


                count += countkeys(tree.getJSONArray(key));

            } catch (JSONException e) {
                try {


                    count += countkeys(tree.getJSONObject(key));

                } catch (JSONException ee) {
                }
            }
            count++;
        }
        return count;
    }


    static void runTest(String input, String title, int loops) {
        int dobbi = 0;
        String jsonarrayThing = input;
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setGroupingSeparator(' ');
        DecimalFormat formatter = new DecimalFormat("###,###.##", symbols);

        long laJsonParseTime = 0;
        long laJsonSerialTime = 0;

        long laJsonNoThreadingParseTime = 0;
        long laJsonNoThreadingSerialTime = 0;



        long jsonrgParseTime = 0;
        long jsonorgSerializeTime = 0;

        long gsonParseTime = 0;
        long gsonSerializeTime = 0;
        int aa = 0;
        int oo = 0;
        int gg = 0;

        int qq = 0;

        Lal.l("\n\nperformance testing... " + title + " input length " + formatter.format(jsonarrayThing.length()));

      //  long t5;
       // long t6;





        int perfLoops = loops;
        for (int l = 0; l < perfLoops; l++) {




            long start = System.nanoTime();
            //Lal.l("lajs parsing commencing");
            LassiJSON lajs = new LassiJSON(jsonarrayThing, true);


            laJsonNoThreadingParseTime += (System.nanoTime() - start);

            qq += countkeys(lajs);


            start = System.nanoTime();
            String lajsostring = lajs.toString();



            laJsonNoThreadingSerialTime += (System.nanoTime() - start);


            for (int n = 0; n < lajsostring.length() / 10; n++) {
                if (lajsostring.charAt(n) == '{') {
                    dobbi += 10;
                }
            }



















             start = System.nanoTime();
            //Lal.l("lajs parsing commencing");
            LassiJSON lajs2 = new LassiJSON(jsonarrayThing, false);



            laJsonParseTime += (System.nanoTime() - start);

            aa += countkeys(lajs2);


            start = System.nanoTime();
            lajsostring = lajs2.toString();


            laJsonSerialTime += (System.nanoTime() - start);


            for (int n = 0; n < lajsostring.length() / 10; n++) {
                if (lajsostring.charAt(n) == '{') {
                    dobbi += 10;
                }
            }

            start = System.nanoTime();
            JSONObject jo = new JSONObject(jsonarrayThing);


            jsonrgParseTime += (System.nanoTime() - start);

            oo += countkeys(jo);





            start = System.nanoTime();
            String jsostring = jo.toString();



            jsonorgSerializeTime += (System.nanoTime() - start);

            for (int n = 0; n < jsostring.length() / 10; n++) {
                if (jsostring.charAt(n) == '{') {
                    dobbi += 10;

                }


            }


            start = System.nanoTime();


            JsonElement jsonTree = new JsonParser().parse(jsonarrayThing);

            gsonParseTime += (System.nanoTime() - start);



            gg += countkeys(jsonTree);




            start = System.nanoTime();
            String gsostring = jsonTree.toString();


            gsonSerializeTime += (System.nanoTime() - start);


            for (int n = 0; n < gsostring.length() / 10; n++) {
                if (gsostring.charAt(n) == '{') {
                    dobbi += 10;
                }
            }


//            Lal.l("" + t + " vs " + t2 + " vs " + t5);
            System.out.print("\r" +l);
            //System.out.println(" " + laJsonParseTime + " " + jsonrgParseTime);


        }

        System.out.println("");


        System.out.print("lajson single thread parse      " + (laJsonNoThreadingParseTime / perfLoops));
        if (laJsonNoThreadingParseTime < jsonrgParseTime && laJsonNoThreadingParseTime < gsonParseTime && laJsonNoThreadingParseTime < laJsonParseTime)
            System.out.println("    --   winner");
        else
            System.out.println("");


        System.out.print("lajson single thread  serialize " + (laJsonNoThreadingSerialTime / perfLoops));
        if (laJsonNoThreadingSerialTime < jsonorgSerializeTime && laJsonNoThreadingSerialTime < gsonSerializeTime  && laJsonNoThreadingSerialTime < laJsonSerialTime )
            System.out.println("    --   winner");
        else
            System.out.println("");





        System.out.print("lajson multi thread  parse      " + (laJsonParseTime / perfLoops));
        if (laJsonParseTime < jsonrgParseTime && laJsonParseTime < gsonParseTime && laJsonParseTime < laJsonNoThreadingParseTime)
            System.out.println("    --   winner");
        else
            System.out.println("");


        System.out.print("lajson multi thread  serialize  " + (laJsonSerialTime / perfLoops));
        if (laJsonSerialTime < jsonorgSerializeTime && laJsonSerialTime < gsonSerializeTime && laJsonSerialTime < laJsonNoThreadingSerialTime)
            System.out.println("    --   winner");
        else
            System.out.println("");


        System.out.print("json.org parse                  " + (jsonrgParseTime / perfLoops));
        if (jsonrgParseTime < laJsonParseTime && jsonrgParseTime < gsonParseTime && jsonrgParseTime < laJsonNoThreadingParseTime)
            System.out.println("    --   winner");
        else
            System.out.println("");


        System.out.print("json.org serialize              " + (jsonorgSerializeTime / perfLoops));
        if (jsonorgSerializeTime < laJsonSerialTime && jsonorgSerializeTime < gsonSerializeTime  && jsonorgSerializeTime < laJsonNoThreadingSerialTime )
            System.out.println("    --   winner");
        else
            System.out.println("");

        System.out.print("gson     parse                  " + (gsonParseTime / perfLoops));
        if (gsonParseTime < laJsonParseTime && gsonParseTime < jsonrgParseTime  && gsonParseTime < laJsonNoThreadingParseTime )
            System.out.println("    --   winner");
        else
            System.out.println("");

        System.out.print("gson     serialize              " + (gsonSerializeTime / perfLoops));
        if (gsonSerializeTime < laJsonSerialTime && gsonSerializeTime < jsonorgSerializeTime && gsonSerializeTime < laJsonNoThreadingSerialTime)
            System.out.println("    --   winner");
        else
            System.out.println("");


        Lal.l("LassiJSON single thread  / LassiJSON multi threaded  / jsonorg  / gson test check numbers:  "+qq+"/" + aa + "/" + oo + "/" + gg + " ::  content counter " + dobbi);


    }


    public static void main(String[] args) {

        Lal.l("starting test");

        // write your code here
        LassiJSON test1 = new LassiJSON();
        test1.putNumber("number1", 11);
        test1.putString("string", "test\n\r\tstring");

        LassiJSON test1inner = new LassiJSON();
        test1inner.putNumber("exponent number", 1e7);
        test1inner.putString("string", "test\n\r\tstring");

        LassiJSON testArray = LassiJSON.newArray();

        testArray.addNumber(100);
        testArray.addString("halo halo");
        testArray.addNumber(0, 0);
        testArray.addNull();

        test1inner.putLaJSON("array with different type elements", testArray);

        test1.putLaJSON("contained_json", test1inner);


        String testInput1 = test1.toString();

        LassiJSON json = new LassiJSON(testInput1);

        String str1 = testInput1;
        String str2 = json.toString();


        printKeys(json, "----");


        Lal.l("compare : " + str1);
        Lal.l("          " + str2);


        if (str1.contentEquals(str2)) {
            Lal.l("OK matches");
        } else {
            Lal.l("ERROR ERROR ERROR NO MATCH");
            exit(-1);
        }



        LassiJSON ns = new LassiJSON();


        ns.putNumber("floaty  aaa", 1.3d);


        for (int n = 0; n < 5; n++) {

            for (int o = 0; o < 2; o++) {
                LassiJSON uus = new LassiJSON();
                uus.putNumber("sd\n\n\nardordooo" + o, o);
                uus.putNumber("x", o);
                uus.putNumber("nx", o + n);
                //uus.putNumber("y",-o);

                LassiJSON arv = LassiJSON.newArray();

                for (int b = 0; b < 10; b++) {
                    LassiJSON uv = new LassiJSON();
                    uv.putNumber("x", b + o);
                    uv.putNumber("y", b + o / 2);
                    uv.putString("hardy ", "sdfs\n\n");

                    LassiJSON subarray = LassiJSON.newArray();

                    uv.putLaJSON("sisainen array", subarray);

                    for (int k = 0; k < 1111; k++) {
                        uv.putNumber("x" + k + "" + b, b + o);
                        subarray.addString("abcdefg " + (k * k));
                        subarray.addNumber(k * k);
                    }


                    arv.addNumber(n + b + o);

                    arv.addLaJSON(uv);
                }
                uus.putLaJSON("samejson", json);
                uus.putLaJSON("cordlist", arv);
                uus.putLaJSON("samejson2", test1);
                uus.putNumber("floaty][}{" + n, 1.3);
                ns.putLaJSON("" + n, uus);

            }

            ns.putNumber("floaty][}{" + n, 1.3);
        }

        runTest(ns.toString(), "LassiJSON generated big input file ", 100);

       // if(true)
         //   return;

        String test2String = "{\n" +
                "  \"valuename1\": \"krustystring\",\n" +
                "  \"only1 level deep\": \"with two strings\"\n" +
                "}";

        runTest(test2String, "small json with no depth and two strings", 10000);

        LassiJSON strLj=new LassiJSON(test2String);

        LassiJSON test3 = new LassiJSON();

        for(int n=0; n<10000; n++)
        {
            test3.putLaJSON(""+n+"", strLj);
        }

        runTest(test3.toString(),"10 000 small named subjsons straight on the root element", 50);

        LassiJSON test4 = new LassiJSON();

        for(int n=0; n<100000; n++)
        {
            test4.putLaJSON(""+n+"", strLj);
        }

        runTest(test4.toString(),"100 000 small named subjsons straight on the root element", 10);


        LassiJSON test5 = new LassiJSON();

        LassiJSON array = new LassiJSON();

        array.clearAsArray();


        for(int n=0; n<10000; n++)
        {
            array.addLaJSON(strLj);
        }
        test5.putLaJSON("lajson array", array);


        runTest(test5.toString(),"10 000 small items in a named array", 50);


        test5.clearAsObject();
        array.clearAsArray();

        for(int n=0; n<10000; n++)
        {

            LassiJSON inner=new LassiJSON();
                for(int o=0; o<50; o++)
                {
                    inner.putString("test_string "+o, "the strings content "+o);
                    inner.putNumber("test_number"+o, o*o);
                }
           test5.putLaJSON("inner_json"+n , inner);
        }
        runTest(test5.toString(), "10000 sub items that are bigger items",15);

        test5.clearAsObject();
        array.clearAsArray();

        for(int n=0; n<20000; n++)
        {

            LassiJSON inner=new LassiJSON();
            for(int o=0; o<50; o++)
            {
                inner.putString("test_string "+o, "the strings content "+o);
                inner.putNumber("test_number"+o, o*o);
            }
            test5.putLaJSON("inner_json"+n , inner);
        }
        runTest(test5.toString(), "20000 sub items that are bigger items",15);

        for(int n=0; n<100; n++)
        {

            LassiJSON inner=new LassiJSON();
            for(int o=0; o<100; o++)
            {
                inner.putString("test_string "+o, "the strings content "+o);
                inner.putNumber("test_number"+o, o*o);
            }
            test5.putLaJSON("inner_json"+n , inner);
        }
        runTest(test5.toString(), "100 sub items that are bigger items",15);

    }


}
