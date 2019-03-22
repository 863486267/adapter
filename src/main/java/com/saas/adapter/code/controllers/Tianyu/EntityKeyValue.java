package com.saas.adapter.code.controllers.Tianyu;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.List;

public class EntityKeyValue implements Comparable<EntityKeyValue>{


	private String key;

	private Object value;

	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public EntityKeyValue(String key, Object value) {
		super();
		this.key = key;
		this.value = value;
	}
	public EntityKeyValue( ) {

	}
	@Override
	public int compareTo(EntityKeyValue o) {


		String key2 = o.getKey();

		if(key == null && key2 != null){
			return -1;
		}

		if(key != null && key2 == null){
			return 1;
		}

		if(key == null && key2 == null){
			return 0;
		}

		int compareTo = this.key.compareTo(key2);

		return compareTo;
	}
	@Override
	public String toString() {
		return "EntityKeyValue [key=" + key + ", value=" + value + "]";
	}




	public static final String DEFAULT_CHARSET_NAME = "UTF-8";
	/**
	 *
	 * @param s
	 * @return 返回md5 的小写
	 */
	public final static String MD5Normal(String s) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			byte[] strTemp = s.getBytes();
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(strTemp);
			byte[] md = mdTemp.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * Used to build output as Hex
	 */
	private static final char[] DIGITS_LOWER = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	/**
	 * Used to build output as Hex
	 */
	private static final char[] DIGITS_UPPER = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public static byte[] decodeHex(final char[] data) throws RuntimeException {

		final int len = data.length;

		if ((len & 0x01) != 0) {
			throw new RuntimeException("Odd number of characters.");
		}

		final byte[] out = new byte[len >> 1];

		// two characters form the hex value.
		for (int i = 0, j = 0; j < len; i++) {
			int f = toDigit(data[j], j) << 4;
			j++;
			f = f | toDigit(data[j], j);
			j++;
			out[i] = (byte) (f & 0xFF);
		}

		return out;
	}

	/**
	 * @param src
	 *            需要转码的字符串
	 * @param charset
	 *            字符编码集
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String stringToHex(String src, String charset)
			throws UnsupportedEncodingException {
		byte[] bytes = src.getBytes(charset);
		String encodeHexString = encodeHexString(bytes);
		return encodeHexString;
	}

	/**
	 * 默认编码未utf-8
	 *
	 * @param src
	 *            需要转码的字符串
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String StringToHex(String src)
			throws UnsupportedEncodingException {
		return stringToHex(src, DEFAULT_CHARSET_NAME);
	}

	/**
	 * @param src
	 *            需要转码的字符串
	 * @param charset
	 *            字符编码集
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String hexToString(String src, String charset)
			throws UnsupportedEncodingException {

		char[] charArray = src.toCharArray();

		byte[] decodeHex = decodeHex(charArray);

		return new String(decodeHex, charset);
	}

	/**
	 * 默认编码未utf-8
	 *
	 * @param src
	 *            需要转码的字符串
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String hexToString(String src)
			throws UnsupportedEncodingException {
		return hexToString(src, DEFAULT_CHARSET_NAME);
	}

	public static char[] encodeHex(final byte[] data) {
		return encodeHex(data, true);
	}

	public static char[] encodeHex(final byte[] data, final boolean toLowerCase) {
		return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
	}

	protected static char[] encodeHex(final byte[] data, final char[] toDigits) {
		final int l = data.length;
		final char[] out = new char[l << 1];
		// two characters form the hex value.
		for (int i = 0, j = 0; i < l; i++) {
			out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
			out[j++] = toDigits[0x0F & data[i]];
		}
		return out;
	}

	public static String encodeHexString(String data) {
		if (data == null) {
			return null;
		}
		return new String(encodeHex(data.getBytes()));
	}

	public static String encodeHexString(final byte[] data) {
		return new String(encodeHex(data));
	}

	protected static int toDigit(final char ch, final int index)
			throws RuntimeException {
		final int digit = Character.digit(ch, 16);
		if (digit == -1) {
			throw new RuntimeException("Illegal hexadecimal character " + ch
					+ " at index " + index);
		}
		return digit;
	}

	public byte[] decode(final byte[] array) throws RuntimeException,
			UnsupportedEncodingException {
		return decodeHex(new String(array, DEFAULT_CHARSET_NAME).toCharArray());
	}

	public Object decode(final Object object) throws RuntimeException {
		try {
			final char[] charArray = object instanceof String ? ((String) object)
					.toCharArray() : (char[]) object;
			return decodeHex(charArray);
		} catch (final ClassCastException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public byte[] encode(final byte[] array)
			throws UnsupportedEncodingException {
		return encodeHexString(array).getBytes(DEFAULT_CHARSET_NAME);
	}

	public Object encode(final Object object)
			throws UnsupportedEncodingException {
		final byte[] byteArray = object instanceof String ? ((String) object)
				.getBytes(DEFAULT_CHARSET_NAME) : (byte[]) object;
		return encodeHex(byteArray);

	}

	private static JsonParser jsonParser = null;

	public static JsonParser getJsonParser(){
		if(jsonParser == null){
			synchronized(JsonParser.class){
				jsonParser = new JsonParser();
			}
		}
		return jsonParser;
	}
	public static JsonObject parseJsonObject(String input_string){
		if(input_string == null  || input_string.trim().isEmpty()){
			return null;
		}

		JsonParser jsonParser_tmp = getJsonParser();

		JsonElement parse = jsonParser_tmp.parse(input_string);

		if(parse.isJsonObject()){
			return parse.getAsJsonObject();
		}
		return null;
	}
	/**
	 *
	 * @param data
	 * @param name
	 * @return	如果存在并且类型是JsonPrimitive返回值	否则返回null
	 */
	public static String getString(JsonObject data, String name){
		if(data == null){
			return null;
		}

		JsonElement jsonElement = data.get(name);

		if(jsonElement != null && jsonElement.isJsonPrimitive()){
			return jsonElement.getAsString();
		}
		return null;
	}

	/**
	 *
	 * @param data
	 * @param name
	 * @return	如果存在并且类型是JsonObject返回值	否则返回null
	 */
	public static JsonObject getJsonObject(JsonObject data, String name){
		if(data == null){
			return null;
		}

		JsonElement jsonElement = data.get(name);

		if(jsonElement != null && jsonElement.isJsonObject()){
			return jsonElement.getAsJsonObject();
		}
		return null;
	}
	/**
	 *
	 * @param data
	 * @param name
	 * @return	如果存在并且类型是JsonArray返回值	否则返回null
	 */
	public static JsonArray getJsonArray(JsonObject data, String name){
		if(data == null){
			return null;
		}

		JsonElement jsonElement = data.get(name);

		if(jsonElement != null && jsonElement.isJsonArray()){
			return jsonElement.getAsJsonArray();
		}else if(jsonElement != null && !jsonElement.isJsonNull()){
			JsonArray jsonArray = new JsonArray();
			jsonArray.add(jsonElement);
			return jsonArray;
		}
		return null;
	}

	public static String charset = "utf-8";

	public static String http_post(String url_str,List<EntityKeyValue> entityKeyValues) throws Exception {


		Integer default_timeout = 30 * 1000;

		URL url = new URL(url_str);

		HttpURLConnection openConnection = (HttpURLConnection) url.openConnection();

		openConnection.setDoOutput(true);
		openConnection.setDoInput(true);

		openConnection.setConnectTimeout(default_timeout);
		openConnection.setReadTimeout(default_timeout);

		StringBuilder paremeter = new StringBuilder();
		if (entityKeyValues != null) {
			for (EntityKeyValue entityKeyValue : entityKeyValues) {
				Object value = entityKeyValue.getValue();
				if (value == null) {
					value = null;
				}
				paremeter.append(entityKeyValue.getKey()).append("=").append(entityKeyValue.getValue()).append("&");
			}
		}
		if (paremeter.length() > 1 && paremeter.toString().endsWith("&")) {
			paremeter = paremeter.deleteCharAt(paremeter.length() - 1);
		}

		OutputStreamWriter outputStreamWriter = null;
		InputStream inputStream = null;
		ByteArrayOutputStream outByteArray = new ByteArrayOutputStream();

		byte[] returnByte = null;
		try {

			OutputStream outputStream = openConnection.getOutputStream();

			outputStreamWriter = new OutputStreamWriter(outputStream, charset);

			if (paremeter != null && paremeter.length() > 0) {
				outputStreamWriter.write(paremeter.toString());
			}
			outputStreamWriter.flush();
			outputStreamWriter.close();

			inputStream = openConnection.getInputStream();
			int b;
			while (inputStream != null && (b = inputStream.read()) != -1) {
				outByteArray.write(b);
			}
			returnByte = outByteArray.toByteArray();
			return new String(returnByte);
		} catch (IOException e) {
			inputStream = ((HttpURLConnection) openConnection).getErrorStream();
			int b;
			while (inputStream != null && (b = inputStream.read()) != -1) {
				outByteArray.write(b);
			}
			returnByte = outByteArray.toByteArray();

			return new String(returnByte);
		} finally {
			outByteArray.close();
			if (outputStreamWriter != null) {
				outputStreamWriter.close();
			}
			if (inputStream != null) {
				inputStream.close();
			}
		}


	}
}
