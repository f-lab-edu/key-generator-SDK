import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BaseEncodingTest {

	@Test
	void testBase62Encoding() {
		String input = "Hello World!";
		String encoded = BaseEncoding.toBase62(input);

		assertThat(encoded).isEqualTo("T8dgcjRGkZ3aysdN");
	}

	@Test
	void testBase64Encoding() {
		String input = "if-else-if";
		String encoded = BaseEncoding.toBase64(input);

		assertThat(encoded).isEqualTo("aWYtZWxzZS1pZg");
	}

	@Test
	void testBase64EncodingWithPadding() {
		String input = "if-else-if";
		String encoded = BaseEncoding.toBase64WithPadding(input);

		assertThat(encoded).isEqualTo("aWYtZWxzZS1pZg==");
	}
}
