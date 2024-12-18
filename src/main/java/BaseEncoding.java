import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseEncoding {

	public static String toBase62(String input){
		return BASE62.encode(input);
	};
	public static String toBase64(String input) {
		return BASE64.encode(input);
	}
	public static String toBase64WithPadding(String input) {
		return BASE64_WITH_PADDING.encode(input);
	}

	abstract String encode(String input);

	private static final String BASE62_ALPHABET_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private static final String BASE64_ALPHABET_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	private static final String BASE64_PADDING_CHAR = "=";

	private static final Base62Encoding BASE62 = new Base62Encoding(BASE62_ALPHABET_CHARS);
	private static final Base64Encoding BASE64 = new Base64Encoding(BASE64_ALPHABET_CHARS,false);
	private static final Base64Encoding BASE64_WITH_PADDING = new Base64Encoding(BASE64_ALPHABET_CHARS,true);

	private static final int LENGTH_8_BIT = 8;
	private static final int LENGTH_6_BIT = 6;
	private static final int BASE64_CALCULATE_SIZE = 4;

	private static final BigInteger BIG_INT_62 = new BigInteger("62");
	private static final BigInteger BASE_NUMBER = new BigInteger("2");

	private static String stringToBinaryStream(String input) {
		StringBuilder sb = new StringBuilder();
		byte[] utf8Bytes = input.getBytes(StandardCharsets.UTF_8);
		for (byte b : utf8Bytes) {
			sb.append(byteToBinaryStream(b));
		}
		return sb.toString();
	}

	private static String byteToBinaryStream(byte b) {
		return padTo8bits(Integer.toBinaryString(b & 0xFF));
	}

	private static String padTo8bits(String binaryString) {
		StringBuilder sb = new StringBuilder().append(binaryString);
		int padCount = LENGTH_8_BIT - binaryString.length();
		for (int i = 0; i < padCount; i++) {
			sb.insert(0, "0");
		}
		return sb.toString();
	}

	private static final class Base62Encoding extends BaseEncoding{
		private final String alphabetChars;

		private Base62Encoding(String alphabetChars){
			this.alphabetChars = alphabetChars;
		}

		@Override
		String encode(String input) {
			return decimalToBase62(binaryToDecimal(stringToBinaryStream(input)));
		}

		private BigInteger binaryToDecimal(String binaryStream) {
			BigInteger sum = new BigInteger("0");
			char[] charArray = binaryStream.toCharArray();

			for (int i = charArray.length - 1, j = 0; i >= 0; i--, j++) {
				char c = charArray[j];
				if (c == '1') {
					sum = sum.add(BASE_NUMBER.pow(i));
				}
			}
			return sum;
		}

		private String decimalToBase62(BigInteger bigInteger) {
			BigInteger decimal = bigInteger;
			StringBuilder sb = new StringBuilder();
			BigInteger remainder;

			while (isGreaterDecimalThan62(decimal)) {
				remainder = decimal.remainder(BIG_INT_62);
				sb.append(alphabetChars.charAt(remainder.intValue()));
				decimal = decimal.divide(BIG_INT_62);

				if (isLessDecimalThan62(decimal)) {
					sb.append(alphabetChars.charAt(decimal.intValue()));
				}
			}
			return sb.reverse().toString();
		}

		private boolean isGreaterDecimalThan62(BigInteger decimal) {
			return decimal.compareTo(BIG_INT_62) > 0;
		}

		private boolean isLessDecimalThan62(BigInteger decimal) {
			return decimal.compareTo(BIG_INT_62) < 0;
		}
	}

	private static final class Base64Encoding extends BaseEncoding {

		private final String alphabetChars;
		private final boolean isPadding;

		private Base64Encoding(String alphabetChars,boolean isPadding) {
			this.alphabetChars = alphabetChars;
			this.isPadding = isPadding;
		}

		@Override
		public String encode(String input) {
			return binaryToBase64(splitInto6bit(stringToBinaryStream(input)));
		}

		private List<String> splitInto6bit(String binaryStream) {
			List<String> arr = new ArrayList<>();
			int binaryStrLength = binaryStream.length();
			int remainder = binaryStrLength % LENGTH_6_BIT;
			int loopCount = binaryStrLength - remainder;

			for (int i = 0; i < loopCount; i += LENGTH_6_BIT) {
				arr.add(binaryStream.substring(i, i + LENGTH_6_BIT));
			}
			if (isRequiredPadFor6bits(remainder)) {
				StringBuilder sb = new StringBuilder(binaryStream.substring(loopCount, binaryStrLength));
				for (int i = 0; i < LENGTH_6_BIT - remainder; i++) {
					sb.append("0");
				}
				arr.add(sb.toString());
			}
			return arr;
		}

		private boolean isRequiredPadFor6bits(int remainder) {
			return remainder != 0;
		}

		private String binaryToBase64(List<String> list) {
			StringBuilder sb = new StringBuilder();
			for (String str : list) {
				char[] charArray = str.toCharArray();
				int decimal = binaryToDecimal(charArray);
				sb.append(alphabetChars.charAt(decimal));
			}
			if (isPadding) {
				appendPaddingCharacter(sb);
			}
			return sb.toString();
		}

		private int binaryToDecimal(char[] charArray) {
			int decimal = 0;
			for (int i = 0, j = charArray.length - 1; i < charArray.length; i++, j--) {
				char c = charArray[i];
				if (c == '1') {
					decimal = decimal + (int) Math.pow(2, j);
				}
			}
			return decimal;
		}

		private void appendPaddingCharacter(StringBuilder sb) {
			int mod = sb.length() % BASE64_CALCULATE_SIZE;
			if (mod == 0) return;
			for (int i = 0; i < BASE64_CALCULATE_SIZE - mod; i++) {
				sb.append(BASE64_PADDING_CHAR);
			}
		}
	}
}
