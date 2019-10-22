package io.oneko.namespace;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class NamespaceConventionsTest {

	@Test
	public void testSanitizeNamespace() {
		//upper case
		assertThat(NamespaceConventions.sanitizeNamespace("Hallo"), is("hallo"));

		//upper case everywhere
		assertThat(NamespaceConventions.sanitizeNamespace("bR.o-o_Oo.T"), is("bro-o-oot"));

		//max length
		assertThat(NamespaceConventions.sanitizeNamespace(
				"sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss"),
				is("sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss"));

		//illegal characters
		assertThat(NamespaceConventions.sanitizeNamespace("k%s"), is("ks"));
		assertThat(NamespaceConventions.sanitizeNamespace("__a_b-c__d__"), is("a-b-c--d"));

		//illegal start and end character
		assertThat(NamespaceConventions.sanitizeNamespace("._-legal-name-_."), is("legal-name"));

		try {
			NamespaceConventions.sanitizeNamespace("%");
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
