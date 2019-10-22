package io.oneko.security;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Test;

public class AESTest {

	@Test
	public void testSingleLetter() {
		this.roundTrip("a");
	}

	@Test
	public void testExtraLongChar() {
		this.roundTrip("qwerzuiqwoeruzqowieruzqowieurzqoiuwzeroiuzqoiwuzeroiuqzoiuziouzweriuweriuz");
	}

	@Test
	public void testSpecialChars() {
		this.roundTrip("%&/()§$'#");
	}

	@Test
	public void testKoreanCharacters() {
		this.roundTrip("최고 지도자 족제비");//supreme leader weasel
	}

	@Test
	public void testEmoji() {
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
	public void testSeparateAESInstances() {
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
