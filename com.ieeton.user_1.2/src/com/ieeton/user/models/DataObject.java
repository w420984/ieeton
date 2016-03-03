package com.ieeton.user.models;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;

import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.utils.Utils;

import android.util.Xml;

public abstract class DataObject{

	private static final Pattern entryPattern = Pattern.compile("&\\w+;");

    private static final HashMap<String, String> ENTRY_MAP = new HashMap<String, String>();

    protected static String PARSE_ERROR = "Problem parsing API response";

    protected static String UNKNOWN_ERROR = "Unknown error";
    
    protected XmlPullParser parser;
    
    static {
        ENTRY_MAP.put("&lt;", "<");
        ENTRY_MAP.put("&gt;", ">");
        ENTRY_MAP.put("&amp;", "&");
        ENTRY_MAP.put("&apos;", "'");
        ENTRY_MAP.put("&quot;", "\"");
    }
    
    public DataObject(){
    }
    
    public DataObject(String xmlStr) throws PediatricsParseException{
    	parser = Xml.newPullParser();
    	if(xmlStr != null){
    		initFromString(xmlStr);
    	}
        parser = null;
    }
    
    public DataObject(XmlPullParser _parser) throws PediatricsParseException{
        initFromParser(_parser);
    }
    
	public abstract DataObject initFromString(String xmlStr) throws PediatricsParseException;
	
	public abstract DataObject initFromParser(XmlPullParser _parser) throws PediatricsParseException;
	
	protected abstract DataObject parse() throws PediatricsParseException;
	
	protected String parseText(XmlPullParser parser) throws PediatricsParseException {
        try {
            int type = parser.next();
            if (type == XmlPullParser.TEXT) {
                return replaceEntityRef(parser.getText().trim());
            } else {
                return "";
            }
        } catch (Exception e) {
            throw new PediatricsParseException(PARSE_ERROR, e);
        }
    }
	
	/**
	 * 标签的文本中含有&lt;br /&gt;时，在解析字符串时替换为\n
	 */
    protected String parseMultiLineText(XmlPullParser parser, String tag) throws PediatricsParseException {
        StringBuilder builder = new StringBuilder();
        int type;

        try {
            while ((type = parser.next()) != XmlPullParser.END_TAG
                    || !parser.getName().equalsIgnoreCase(tag)) {
                if (type == XmlPullParser.TEXT) {
                    builder.append(replaceEntityRef(parser.getText().trim()));
                } else if (type == XmlPullParser.START_TAG
                        && parser.getName().equals("br")) {
                    builder.append("\n");
                }
            }
        } catch (Exception e) {
            throw new PediatricsParseException(PARSE_ERROR, e);
        }

        return builder.toString();
    }
    
	private String replaceEntityRef(String src) {
	    Matcher m = entryPattern.matcher(src);
        StringBuilder buffer = new StringBuilder();
        int start = -1, end = -1, lastEnd = -1;
        String val = null;
        while (m.find()) {
            start = m.start();
            end = m.end();
            val = ENTRY_MAP.get(m.group());
            if (!Utils.isEmptyOrBlank(val)) {
                if (lastEnd != -1) {
                    buffer.append(src.substring(lastEnd, start));
                    buffer.append(val);
                    lastEnd = end;
                    start = -1;
                    end = -1;
                }
            }
        }
        if (lastEnd == -1) {
            return src;
        } else if (lastEnd != src.length()) {
            buffer.append(src.substring(lastEnd));
        }

        return buffer.toString();
    }

    protected static int safeParseInt( String text, int defValue ) {
        try {
            return Integer.parseInt( text );
        }
        catch (NumberFormatException e) {
        }
        return defValue;
    }

    protected static long safeParseLong( String text, long defValue ) {
        try {
            return Long.parseLong( text );
        }
        catch( NumberFormatException e ) {
        }
        return 0L;
    }
}
