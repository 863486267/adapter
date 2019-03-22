package com.saas.adapter.code.controllers.Tianyu;

 

import java.io.UnsupportedEncodingException;


public class HexM   {
 
    public static final String DEFAULT_CHARSET_NAME =   "UTF-8";

    /**
     * Used to build output as Hex
     */
    private static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * Used to build output as Hex
     */
    private static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
 
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
 
    public static String encodeHexString(final byte[] data) {
        return new String(encodeHex(data));
    } 
    protected static int toDigit(final char ch, final int index) throws RuntimeException {
        final int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new RuntimeException("Illegal hexadecimal character " + ch + " at index " + index);
        }
        return digit;
    }
 
    public byte[] decode(final byte[] array) throws RuntimeException, UnsupportedEncodingException {
        return decodeHex(new String(array, DEFAULT_CHARSET_NAME).toCharArray());
    }
 
    public Object decode(final Object object) throws RuntimeException {
        try {
            final char[] charArray = object instanceof String ? ((String) object).toCharArray() : (char[]) object;
            return decodeHex(charArray);
        } catch (final ClassCastException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    } 
    public byte[] encode(final byte[] array) throws UnsupportedEncodingException {
        return encodeHexString(array).getBytes(DEFAULT_CHARSET_NAME);
    } 
    
    public Object encode(final Object object) throws UnsupportedEncodingException {
            final byte[] byteArray = object instanceof String ? ((String) object).getBytes(DEFAULT_CHARSET_NAME) : (byte[]) object;
            return encodeHex(byteArray);
     
    }
 
}

