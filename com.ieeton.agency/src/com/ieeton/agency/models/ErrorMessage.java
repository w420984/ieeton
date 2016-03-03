package com.ieeton.agency.models;


import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.ieeton.agency.exception.PediatricsParseException;

import android.text.TextUtils;


/**
 * @author nieyu1
 *
 */
public class ErrorMessage extends DataObject implements Serializable {

    private static final long serialVersionUID = -2861166030329864848L;

    
    /**
     * 错误码
     */
    public String errorcode;
    /**
     * 错误信息
     */
    public String errmsg;
	
	public ErrorMessage(){
    }
    
    public ErrorMessage(String xmlStr) throws PediatricsParseException{
        super(xmlStr);
    }
    
    public ErrorMessage(XmlPullParser _parser) throws PediatricsParseException{
        initFromParser(_parser);
    }
    
	public ErrorMessage initFromParser(XmlPullParser _parser) throws PediatricsParseException {
        parser = _parser;
        return parse();
    }
	
    @Override
    public ErrorMessage initFromString(String xmlStr) throws PediatricsParseException {
    	if(TextUtils.isEmpty(xmlStr)){
    		return null;
    	}
    	if(xmlStr.startsWith("<?xml")){
    		try {
    			parser.setInput(new StringReader(xmlStr));
    		} catch (XmlPullParserException ex) {
    			throw new PediatricsParseException(ex);
    		}
    		return parse();
    	}else{
    		try {
				parseJson(xmlStr);
			} catch (JSONException e) {
				throw new PediatricsParseException(PARSE_ERROR);
			}
    		return this;
    	}
    }
    
    private void parseJson(String xmlStr) throws JSONException{
    	JSONObject jsonObject = null;
    	try {
    		jsonObject = new JSONObject(xmlStr);
    		boolean isError = jsonObject.optBoolean("error");
    		int code = jsonObject.optInt("code");
    		if (!isError && (code == 200)){
    			return;
    		}
    		JSONObject message = jsonObject.optJSONObject("messages");
    		if(message == null){
    			//图片上传服务器下行数据没有message、error等信息，只有files信息
    			return;
    		}
    		JSONObject errorInfo = message.optJSONObject("error");
    		errmsg = errorInfo.optString("message");
    		errorcode = errorInfo.optString("code");
    	} catch (JSONException e) {
    		JSONArray jsonArr = new JSONArray(xmlStr);
    	}
	}

	@Override
    protected ErrorMessage parse() throws PediatricsParseException {
        int type;
        try {
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT) {
                switch (type) {
                case XmlPullParser.START_TAG:
                    if (parser.getName().equals("errorcode")) {
                        this.errorcode = parseText(parser);
                    } else if (parser.getName().equals("errmsg")) {
                        this.errmsg = parseText(parser);
                    }
                    break;
                default:
                    break;
                }
            }

        } catch (XmlPullParserException e) {
            throw new PediatricsParseException(PARSE_ERROR, e);
        } catch (IOException e) {
            throw new PediatricsParseException(PARSE_ERROR, e);
        }finally{
            parser = null;
        }
        return this;
    }
}