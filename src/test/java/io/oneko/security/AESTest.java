package io.oneko.security;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class AESTest {

	@Test
	void testSingleLetter() {
		this.roundTrip("a");
	}

	@Test
	void testExtraLongChar() {
		this.roundTrip("qwerzuiqwoeruzqowieruzqowieurzqoiuwzeroiuzqoiwuzeroiuqzoiuziouzweriuweriuz");
	}

	@Test
	void testSpecialChars() {
		this.roundTrip("%&/()§$'#");
	}

	@Test
	void testKoreanCharacters() {
		this.roundTrip("최고 지도자 족제비");//supreme leader weasel
	}

	@Test
	void testEmoji() {
		this.roundTrip("🍺🥒🧀");
	}

	private void roundTrip(String test) {
		AES uut = new AES("s3cr3t");

		String encode = uut.encrypt(test);
		assertThat(encode, is(not(test)));

		String output = uut.decrypt(encode);

		assertThat(output, is(test));
	}

	@Test
	void testSeparateAESInstances() {
		UUID uuid = UUID.randomUUID();
		String pw = "foo";
		String con = uuid.toString() + pw;
		roundTrip(con);
		AES uut1 = new AES("test");
		String encode = uut1.encrypt(con);
		String decode1 = uut1.decrypt(encode);
		assertThat(decode1, is(con));
		AES uut2 = new AES("test");
		String decode = uut2.decrypt(encode);
		assertThat(decode, is(con));
	}
}
