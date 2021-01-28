package io.oneko.namespace;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class NamespaceConventionsTest {

	@Test
	void testSanitizeNamespace() {
		//upper case
		assertThat(NamespaceConventions.sanitizeNamespace("Hallo"), is("on-hallo"));

		//upper case everywhere
		assertThat(NamespaceConventions.sanitizeNamespace("bR.o-o_Oo.T"), is("on-bro-o-oot"));

		//max length
		assertThat(NamespaceConventions.sanitizeNamespace(
				"sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss"),
				is("on-ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss"));

		//illegal characters
		assertThat(NamespaceConventions.sanitizeNamespace("k%s"), is("on-ks"));
		assertThat(NamespaceConventions.sanitizeNamespace("__a_b-c__d__"), is("on-a-b-c--d"));

		//illegal start and end character
		assertThat(NamespaceConventions.sanitizeNamespace("._-legal-name-_."), is("on-legal-name"));

		try {
			final String s = NamespaceConventions.sanitizeNamespace("%");
			fail();
		} catch (IllegalArgumentException e) {
			//expected
		}

		try {
			NamespaceConventions.sanitizeNamespace("_");
			fail();
		} catch (IllegalArgumentException e) {
			//expected
		}
	}
}
