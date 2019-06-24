package lassik.LaJSON;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class LaJSON {
    public static String LAJS_VERSION_STRING = "0.002";
    public static String COPYRIGHT = "Lassi Kinnunen 2019, released under wtfgpl, do as you please.";

    /**
     * Set configuration
     * do not u se while any process is parsing a json.
     *
     * @param useThreading               Use threading implementation
     * @param characterLimitForThreading the subjson length limit to send for a new thread, default 1024
     */

    public static void setConfig(boolean useThreading, int threads,  int characterLimitForThreading, boolean uselinkedHashmap) {
        immediateLimit = characterLimitForThreading;
        useThreads = useThreading;
        linkedHashmap=uselinkedHashmap;
        setThreads=threads;
    }

    private static int setThreads=-1;

    private static boolean linkedHashmap = false;
    private static boolean useThreads = true;
    private static int immediateLimit = 2048;

    private ExecutorService executor = null;
    private boolean noSubLaJsons = true;
    private boolean disableThreading = false;
    private boolean hadNonImmediates = false;


    private class StartEnd {
        int start;
        int end;
        char endChar = 0;
    }

    public static void setThreadFactory(ThreadFactory ifactory)
    {
        factory=ifactory;
    }

    private static ThreadFactory factory=new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r);
        }};

    private class ThreadMaster {
        public int q = 0; //count for items in pipeline
        public ThreadMaster() {
            if (useThreads == true) {
                int threads = setThreads;
                if(threads<1) {
                    threads = Runtime.getRuntime().availableProcessors() / 2;
                    if (threads < 2)
                        threads = 2;
                }

                if(setThreads>0)
                    threads=setThreads;

                executor = Executors.newFixedThreadPool(threads,factory);
            }
        }

         void add(ThreadQueueItem threadQueueItem) {
            synchronized (this) {
                q++;
            }
            executor.submit(threadQueueItem);
        }

         void waitForComplete() {
            try {
                synchronized (this) {
                    wait();
                }
                executor.shutdown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class ThreadQueueItem implements Runnable {

        boolean parsed = false;
        String inp;
        ThreadMaster master = null;

        public ThreadQueueItem(ThreadMaster master, boolean threadOut, String inp) {
            this.inp = inp;
            if (master != null) {
                this.master = master;

                if (threadOut && !disableThreading) {
                    master.add(this);
                } else {
                    doParse();
                }
            } else {
                doParse();
            }
        }

        @Override
        public void run() {
            //this feels a bit sketchy
            doParse();
            synchronized (master) {
                master.q--;
                if (master.q == 0) {
                    master.notify();
                }

            }
        }

        private void doParse() {
            int current = 0;
            boolean inThisJson = false;
            StartEnd se = new StartEnd();
            while (current < inp.length()) {
                char ch = inp.charAt(current);
                if (inThisJson == false) {
                    if (ch == '[') {
                        clearAsArray();
                        array = parseArray(inp.substring(current), master);
                        noSubLaJsons = false;
                        return;
                    }
                    if (ch == '{') {
                        inThisJson = true;
                        clearAsObject();
                    }
                } else {
                    {
                        if (ch == '"') {
                            se.start = current;
                            se = finddEndStartElement(inp, se);
                            String name = jsonUnescape(inp.substring(se.start + 2, se.end - 1));
                            se.start = se.end + 1;
                            se = finddEndStartElement(inp, se);
                            String value = inp.substring(se.start + 1, se.end);
                            current = se.end;


                            Value val = new Value(value, true, master);
                            if (val.wasImmediate == false)
                                hadNonImmediates = true;

                            if (val.type == LaJSONDataType.JSONOBJECT)
                                noSubLaJsons = false;
                            else if (val.type == LaJSONDataType.ARRAY)
                                noSubLaJsons = false;



                            values.put(name, val);
                        }
                    }
                }
                current++;
            }
            parsed = true;
        }
    }





    /*
    log utility
    note that the logs are removed to help with speed/to not need preprocessor.
    but when developing/debugging, this is still here.
     */

    public final static boolean LOG = true;

    public static void l(String log) {
        if (LOG)
            System.out.println(log);
    }


    public enum LaJSONDataType {STRING, NUMBER, BOOLEAN, ARRAY, JSONOBJECT, NULL, UNDEFINED};

    public LaJSON() {
        clearAsObject();
    }


    /*
    kinda lazy at the moment. can use massive amounts of memory unnecessarely.

    returns a clone that has no references to the original, with all data cloned.
     */

    public LaJSON clone() {
        String str = toString();
        LaJSON ret = new LaJSON(str, null, disableThreading);
        return ret;
    }
/*

simply json escape a string. if there's no characters to escape then returns the original string.

 */

    private String jsonEscape(final String input) {
        boolean canSkip = true;
        for (int n = 0; n < input.length(); n++) {
            char ch = input.charAt(n);
            switch (ch) {
                case '\\':
                case '\b':
                case '\r':
                case '\t':
                case '"':
                case '\n':
                case '\f':
                    canSkip = false;
                    n = input.length();
                    break;
                default:
            }
        }

        if (canSkip)
            return input;


        StringBuilder sb = getSb();

        for (int n = 0; n < input.length(); n++) {
            char ch = input.charAt(n);

            switch (ch) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                default:
                    sb.append(ch);
                    break;
            }
        }
        return sb.toString();
    }

    StringBuilder sb = null;

    private StringBuilder getSb() {
        if (sb == null)
            sb = new StringBuilder();
        else
            sb.setLength(0);

        return sb;
    }

    /*
    un-escapes a json string. if no escapes are present then returns the input string.

     */

    private String jsonUnescape(String input) {

        if (input.indexOf('\\') == -1) {
            return input;
        }

        StringBuilder sb = getSb();
        boolean escaped = false;
        for (int n = 0; n < input.length(); n++) {
            char ch = input.charAt(n);
            if (escaped) {
                switch (ch) {
                    case '\\':
                        sb.append('\\');
                        break;
                    case 'b':
                        sb.append('\b');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case '"':
                        sb.append('"');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'f':
                        sb.append('\f');
                        break;
                }
                escaped = false;
            } else {
                if (ch == '\\')
                    escaped = true;
                else
                    sb.append(ch);
            }
        }
        return sb.toString();
    }


    public String toString() {
        return jsonSerialize();
    }


    public String jsonSerialize(int indentation) {
        StringBuilder builder = new StringBuilder();
        jsonSerialize(indentation, indentation, builder);
        return builder.toString();
    }


    public String jsonSerialize() {
        StringBuilder builder = new StringBuilder();
        jsonSerialize(0, 0, builder);
        return builder.toString();
    }

    public void addNewlineAndIndent(StringBuilder sb, int indent, int addToIndentation) {
        if (addToIndentation == 0)
            return;

        sb.append('\n');
        for (int n = 0; n < indent; n++) {
            sb.append(' ');
        }
    }


    public void jsonSerialize(int indentation, int addToIndentation, StringBuilder builder) {
        indentation += addToIndentation;
        if (this.isArray()) {
            builder.append('[');
            for (Value val : array
            ) {
                addNewlineAndIndent(builder, indentation, addToIndentation);

                val.toJsonString(indentation, addToIndentation, builder);
                builder.append(',');
            }
            builder.setLength(builder.length() - 1);
            addNewlineAndIndent(builder, indentation - addToIndentation, addToIndentation);
            builder.append(']');
        } else {
            builder.append('{');
            for (String key : values.keySet()) {
                addNewlineAndIndent(builder, indentation, addToIndentation);

                LaJSON.Value val = values.get(key);
                builder.append('"');
                builder.append(jsonEscape(key));
                builder.append('"');

                builder.append(':');
                val.toJsonString(indentation, addToIndentation, builder);
                builder.append(',');
            }
            builder.setLength(builder.length() - 1);
            addNewlineAndIndent(builder, indentation - addToIndentation - addToIndentation, addToIndentation);
            builder.append('}');
        }

    }


    public boolean isArray() {
        return !(array == null);
    }

    public void clearAsArray() {
        values = null;
        array = new ArrayList<>();
    }

    public void clearAsObject() {
        if(linkedHashmap)
        values = new LinkedHashMap<>();
        else
            values = new HashMap<>();
        array = null;
    }

    ArrayList<Value> array = null;



    /*
    Value represents any json value. if the value is an array or another json object it is in the obj.

     */


    public class Value {
        public LaJSONDataType type = LaJSONDataType.UNDEFINED;
        private String iStr = null;
        private LaJSON obj = null;
        private BigDecimal nu = null;

        private void reset(){
            type=LaJSONDataType.UNDEFINED;
            iStr =null;
            nu =null;
            obj =null;
        }

        public Value() {
        }

        public Value(long innumber) {
            type = LaJSONDataType.NUMBER;
            this.nu = new BigDecimal(innumber);
        }

        public Value(BigDecimal innumber) {
            type = LaJSONDataType.NUMBER;
            this.nu = innumber;
        }


        public Value(int innumber) {
            type = LaJSONDataType.NUMBER;
            this.nu = new BigDecimal(innumber);
        }

        public Value(float innumber) {
            type = LaJSONDataType.NUMBER;
            this.nu = new BigDecimal(innumber);
        }

        public Value(double innumber) {
            type = LaJSONDataType.NUMBER;
            this.nu = new BigDecimal(innumber);
        }

        public Value(String str) {
            iStr = str;
            type = LaJSONDataType.STRING;
        }

        public Value(LaJSON la) {
            obj = la;
            if (type != LaJSONDataType.JSONOBJECT) {
                iStr = null;
                nu = null;
                type = LaJSONDataType.JSONOBJECT;
            }
        }

        public String toString() {
            return getString();
        }

        Value(String content, boolean parseJsonContent, ThreadMaster tm) {
            Parse(content, tm);
        }
        boolean wasImmediate = true;
        public void Parse(String content, ThreadMaster tm) {
            content = cutEnd(content);
            content = cutStart(content);
            if (content.startsWith("n")) {
                type = LaJSONDataType.NULL;
                return;
            }
            if (content.startsWith("t")) {
                type = LaJSONDataType.BOOLEAN;
                bool = true;
                return;
            }
            if (content.startsWith("f")) {
                type = LaJSONDataType.BOOLEAN;
                bool = false;
                return;
            }
            char startChar = content.charAt(0);
            if (startChar == '"') {
                type = LaJSONDataType.STRING;
                String cut = content.substring(1, content.length() - 1);//without the quotes
                iStr = jsonUnescape(cut);
                return;
            } else if (startChar == '[' || startChar == '{') {
                type = LaJSONDataType.JSONOBJECT;
                if (content.length() > immediateLimit)
                    wasImmediate = false;
                if (startChar == '[')
                    type = LaJSONDataType.ARRAY;
                obj = new LaJSON(content, tm, disableThreading);
                return;
            } else {
                type = LaJSONDataType.NUMBER;
                this.nu = new BigDecimal(content);
            }
        }

        public void setString(String str) {
            iStr = str;
            type = LaJSONDataType.STRING;
        }

        public void setBoolean(boolean content) {
            type = LaJSONDataType.BOOLEAN;
            iStr = null;
            nu=null;
            bool = content;
        }

        public String getString() {
            if (type == LaJSONDataType.STRING) {
                return iStr;
            }

            if (type == LaJSONDataType.NUMBER)
                return nu.toEngineeringString();

            if (type == LaJSONDataType.BOOLEAN)
                return "" + bool;

            if (type == LaJSONDataType.JSONOBJECT)
                return obj.toString();

            return null;
        }

        public boolean bool;

        public void toJsonString(int indentation, int addToindentation, StringBuilder builder) {
            switch (type) {
                case JSONOBJECT:
                case ARRAY:
                    obj.jsonSerialize(indentation, addToindentation, builder);
                    break;
                case NULL:
                    builder.append("null");
                    break;
                case BOOLEAN:
                    builder.append(bool);
                    break;
                case STRING:
                    builder.append('"');
                    builder.append(jsonEscape(iStr));
                    builder.append('"');
                    break;
                case NUMBER:
                    builder.append(nu.toString());
                    break;
                default:
                    throw new RuntimeException("UNINITIALIZED VALUE");
            }
        }

        public boolean isNull() {
            return type == LaJSONDataType.NULL;
        }

        public LaJSON getLaJSON() {
            return obj;
        }

        /*
        nu sets
         */

        public void setNumber(int content) {
            type = LaJSONDataType.NUMBER;
            nu = new BigDecimal(content);
        }

        public void setNumber(long content) {
            type = LaJSONDataType.NUMBER;
            nu = new BigDecimal(content);
        }

        public void setNumber(float content) {
            type = LaJSONDataType.NUMBER;
            nu = new BigDecimal(content);
        }

        public void setNumber(double content) {
            type = LaJSONDataType.NUMBER;
            nu = new BigDecimal(content);
        }

        public void setLaJSON(LaJSON content) {
            obj = content;
            type = LaJSONDataType.JSONOBJECT;
        }

        public void setNull() {
            type = LaJSONDataType.NULL;
        }

        public void setNumber(BigDecimal content) {
            type = LaJSONDataType.NUMBER;
            nu = content;
        }
    }

    /**
     * Cuts junk characters from the end of string representing the value
     * @param content input
     * @return cut substring or original string if no cutting necessary
     */

    public String cutEnd(String content) {
       // boolean cuttingEnd = true;
        int n = content.length();
        while (true) {
            char ch = content.charAt(n - 1);
            switch (ch) {
                case ' ':
                case '\n':
                case '\r':
                case '\t':
                    //skip
                    break;
                default:
                    if (n != content.length()) {
                   //     l("cuttong "+content+ " "+n);
                        content = content.substring(0, n);
                    }
                    return content;
            }
            n--;
        }
        //return content;
    }

    /**
     * Cuts unnecessary characters from the start of a string
     *
     * @param content
     * @return substring without the junk at the start or original string if no cutting necessary
     */

    public String cutStart(String content) {
        //boolean cuttingStart = true;
        int n = 0;
        while (true) {
            char ch = content.charAt(n);
            switch (ch) {
                case ' ':
                case '\n':
                case '\r':
                case '\t':
                    //skip
                    break;
                default:
                    if (n != 0)
                        content = content.substring(n);
                    return content;
            }
            n++;
        }
        //return content;
    }

    public Value parseVal(String content, ThreadMaster tm) {
        Value val = new Value(content, true, tm);
        if (val.wasImmediate == false)
            hadNonImmediates = true;
        return val;
    }


    /**
     * Parses JSON string into an array
     *
     * @param inp
     * @return
     */

    ArrayList<Value> parseArray(String inp, ThreadMaster tm) {
        ArrayList<Value> ret = new ArrayList<>();
        values = null;
        int cur = 0;
        boolean startFound = false;
        int start = 0;
        char ch = 0;
        char lastChar = 0;
        int depth = 0;
        boolean inQuote = false;
        boolean inValue = false;
        while (true) {

            lastChar = ch;

            if (cur == inp.length())
                return ret;

            ch = inp.charAt(cur);

            if (!startFound) {
                if (ch == '[')
                    startFound = true;
                start = cur;
                depth++;
            } else {

                switch (ch) {
                    case '{':
                    case '[': {
                        if (!inQuote) {
                            StartEnd se = new StartEnd();
                            se.start = cur;
                            finddEndStartElement(inp, se);
                            String valString = inp.substring(se.start + 1, se.end);
                            cur = se.end;
                            start = cur;
                            Value val = parseVal(valString, tm);
                            ret.add(val);
                        }
                    }
                    break;

                    case '}':
                    case ']': {


                        if (!inQuote) {

                            String valString = inp.substring(start + 1, cur);

                            if (valString.length() != 0) {
                                ret.add(parseVal(valString, tm));
                            }
                        }
                    }
                    break;

                    case ',':
                        if (depth == 1) {
                            String valString = inp.substring(start + 1, cur);
                            ret.add(parseVal(valString, tm));
                            start = cur;
                            inValue = false;
                        }
                        break;
                    case '"':
                        if (inQuote == false) {
                            inQuote = true;
                        } else {
                            if (lastChar != '\"') {
                                inQuote = false;
                            }
                        }
                        break;
                    case ' ':
                    case '\n':
                    case '\r':
                        if (inValue == false) {
                            start = cur;
                        }

                        break;
                    default:
                        inValue = true;
                }
            }
            cur++;
        }
    }


    static StartEnd finddEndStartElement(String inp, StartEnd se) {
        int cur = se.start;

       // boolean running = true;
        int depth = 0;

        char startChar = 0;
        char previousChar = 0;

        boolean inQuote = false;

        char ch = 0;

        boolean maybeValue = false;

        //   se.startChar = 0;
        se.endChar = 0;

        while (true) {
            previousChar = ch;//for lookback
            ch = inp.charAt(cur);


            if (depth == 0) {

                if (ch == '{' || ch == '[') {
                    depth++;
                    se.start = cur - 1;
                    // se.startChar = ch;
                    startChar = ch;
                } else if (ch == ',' || ch == ']' || ch == '}') {
                    se.end = cur;
                    /*
                    if (startChar == ':') {
                        se.end = cur;
                        return se;
                    }
                    */


                    if (ch == ']' || ch == '}') {
                        se.end = cur;
                        depth--;
                    }

                    if (depth == 0)
                        return se;
                } else if (ch == '"') {
                    se.start = cur;
                    depth++;

                    startChar = ch;
                } else if (ch != ' ' && ch != ':') {
                    se.start = cur - 1;
                    depth++;

                    startChar = ch;
                    maybeValue = true;
                }


            } else {
                if (maybeValue) {
                    switch (ch) {
                        case ',':
                        case ']':
                        case '}':
                            se.endChar = ch;
                            se.end = cur;
                            return se;
                        default:
                    }
                }


                if (startChar == '"') {
                    if (previousChar != '\\')// not escaped
                        if (ch == '"') {
                            se.start = se.start - 1;
                            se.end = cur + 1;
                            return se;
                        }


                } else if (!inQuote) {
                    if (ch == '"') { //starts a quoted area
                        inQuote = true;

                    } else {

                        switch (ch) {
                            case '[':
                                depth++;
                                break;
                            case '{':
                                depth++;
                                break;
                            case ']':
                                depth--;
                                break;
                            case '}':
                                depth--;
                                break;
                        }

                        if (depth == 0) {

                            if (startChar == '{' || startChar == '[')
                                se.end = cur + 1;
                            else {

                                se.end = cur;
                            }
                            return se;
                        }
                    }
                } else {
                    if (previousChar != '\\')// not escaped
                        if (ch == '"') {
                            inQuote = false;
                        }
                }
            }
            cur++;
        }
    }


    //for practicalitys sake, the hashmap is accessible publicly
    public HashMap<String, LaJSON.Value> values;


    public LaJSON(String inp) {
        constructor(inp, null, false);
    }


    public LaJSON(String inp, boolean disableThreading) {
        constructor(inp, null, disableThreading);
    }

    public LaJSON(String inp, ThreadMaster tm) {
        constructor(inp, tm, false);
    }


    public LaJSON(String inp, ThreadMaster tm, boolean disableThreading) {
        constructor(inp, tm, disableThreading);
    }




    private void constructor(final String inp, ThreadMaster tm, boolean threadingDisabled) {


        if (useThreads == false)
            threadingDisabled = true;

        if (threadingDisabled)
            this.disableThreading = true;


        boolean smaster = false;


        if (!disableThreading)
            if (tm == null) {
                tm = new ThreadMaster();
                smaster = true;
            }


        boolean threadout = inp.length() > immediateLimit;

        if (smaster)
            threadout = false;

        new ThreadQueueItem(tm, threadout, inp);

        if (this.disableThreading)
            return;

        if (smaster) {
            if (!noSubLaJsons && hadNonImmediates)
                tm.waitForComplete();
        }
    }


    public Value getValue(String str) {
        return values.get(str);
    }

    public Value getValue(int n) {
        return array.get(n);
    }

    public String getString(String str) {
        Value val = values.get(str);
        return val.getString();
    }

    public LaJSON getLaJSON(String str) {
        Value val = values.get(str);
        return val.obj;
    }

    public void putString(String name, String content) {
        Value val = values.get(name);
        if (val == null) {
            val = new Value();
            values.put(name, val);
        }
        val.setString(content);
    }

    public void putNumber(String name, BigDecimal content) {
        Value val = values.get(name);
        if (val == null) {
            val = new Value();
            values.put(name, val);
        }
        val.setNumber(content);
    }

    public void putNumber(String name, long content) {
        Value val = values.get(name);
        if (val == null) {
            val = new Value();

            values.put(name, val);
        }
        val.setNumber(content);
    }

    public void putNumber(String name, double content) {
        Value val = values.get(name);
        if (val == null) {
            val = new Value();
            values.put(name, val);
        }
        val.setNumber(content);
    }

    public void putNumber(String name, int content) {
        Value val = values.get(name);
        if (val == null) {
            val = new Value();
            values.put(name, val);
        }
        val.setNumber(content);
    }

    public void putNumber(String name, float content) {
        Value val = values.get(name);
        if (val == null) {
            val = new Value();
            values.put(name, val);
        }
        val.setNumber(content);
    }


    public void putBoolean(String name, boolean content) {
        Value val = values.get(name);
        if (val == null) {
            val = new Value();
            values.put(name, val);
        }
        val.setBoolean(content);
    }


    public void putLaJSON(String name, LaJSON content) {
        Value val = values.get(name);
        if (val == null) {
            val = new Value();
            values.put(name, val);
        }
        val.setLaJSON(content);
    }

    public void putNull(String name) {
        Value val = values.get(name);
        if (val == null) {
            val = new Value();
            values.put(name, val);
        }
        val.setNull();
    }

    public void remove(String name) {
        values.remove(name);
    }

    public void putValue(String name, Value newVal) {
        values.remove(name);
        values.put(name, newVal);
    }

     /*
    array functions
     */

    /**
     * creates an empty array LaJSON
     *
     * @return LaJSON initialized as an empty array
     */
    public static LaJSON newArray() {
        LaJSON ret = new LaJSON();
        ret.clearAsArray();
        return ret;
    }

    /*
    public LaJSON addArray(String str) {
        Value val = new Value();
        val.type = LaJSONDataType.ARRAY;
        val.obj = newArray();
        values.put(str, val);
        return val.obj;
    }
    */

    public Value remove(int n) {
        return array.remove(n);
    }


    public void replace(int n, Value val) {
        array.set(n, val);
    }

    public int arraySize() {
        return array.size();
    }

    public void addValue(int n, Value val) {
        array.add(n, val);
    }

    public void addValue(Value val) {
        array.add(val);
    }

    public void addString(int n, String str) {
        array.add(n, new Value(str));
    }

    public void addNull() {
        Value val = new Value();
        val.type = LaJSONDataType.NULL;
        array.add(val);

    }

    public void addNull(int n) {
        Value val = new Value();
        val.type = LaJSONDataType.NULL;
        array.add(n, val);
    }


    public void addNumber(int n, long numb) {
        array.add(n, new Value(numb));
    }

    public void addNumber(int n, int numb) {
        array.add(n, new Value(numb));
    }


    public void addNumber(int n, double numb) {
        array.add(n, new Value(numb));
    }

    public void addNumber(int n, float numb) {
        array.add(n, new Value(numb));
    }

    public void addLaJSON(int n, LaJSON jso) {
        array.add(n, new Value(jso));
    }

    public void addString(String str) {
        array.add(new Value(str));
    }

    public void addNumber(long numb) {
        array.add(new Value(numb));
    }

    public void addNumber(int numb) {
        array.add(new Value(numb));
    }

    public void addNumber(double numb) {
        array.add(new Value(numb));
    }

    public void addNumber(float numb) {
        array.add(new Value(numb));
    }

    public void addLaJSON(LaJSON jso) {
        array.add(new Value(jso));
    }

    public Value newValue() {
        return new Value();
    }

}
