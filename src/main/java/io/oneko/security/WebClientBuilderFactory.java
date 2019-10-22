package io.oneko.security;

import javax.net.ssl.SSLException;

import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

@Slf4j
@UtilityClass
public class WebClientBuilderFactory {

	public static WebClient.Builder create(boolean trustInsecureCertificates) {
		if (trustInsecureCertificates) {
			try {
				SslContext sslContext = SslContextBuilder
						.forClient()
						.trustManager(InsecureTrustManagerFactory.INSTANCE)
						.build();
				var tcpClient = TcpClient.create().secure(sslProviderBuilder -> sslProviderBuilder.sslContext(sslContext));
				var httpClient = HttpClient.from(tcpClient);
				return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient));
			} catch (SSLException e) {
				log.error("Failed to create an insecure HTTP client. Falling back to the default client.");
			}
		}

		return WebClient.builder();
	}

}
