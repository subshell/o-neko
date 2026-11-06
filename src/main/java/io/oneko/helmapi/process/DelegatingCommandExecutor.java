package io.oneko.helmapi.process;

import java.lang.reflect.Type;
import java.util.Arrays;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class DelegatingCommandExecutor {
	private final Gson gson = new Gson();
	private final ICommandExecutor delegate;

	public DelegatingCommandExecutor(ICommandExecutor delegate) {
		this.delegate = delegate;
	}

	public String execute(String... command) {
		return delegate.doExecute(command);
	}

	public <T> T executeWithJsonOutput(Class<T> returnType, String... command) {
		final String rawResult = delegate.doExecute(command);
		return parseJson(rawResult, returnType, command);
	}

	public <T> T executeWithJsonOutput(Type returnType, String... command) {
		final String rawResult = delegate.doExecute(command);
		return parseJson(rawResult, returnType, command);
	}

	/**
	 * The underlying process can exit successfully while still printing informational text before the actual JSON on
	 * stdout - e.g. helm writes "Pulled: ..."/"Digest: ..." progress lines there (rather than stderr) for OCI charts,
	 * depending on the helm version/platform. The JSON payload is always the last thing printed, so anything before
	 * the first '{' or '[' is discarded rather than handed to Gson as-is.
	 */
	private <T> T parseJson(String rawResult, Type returnType, String[] command) {
		String json = stripLeadingNonJson(rawResult);
		try {
			return gson.fromJson(json, returnType);
		} catch (JsonSyntaxException e) {
			throw new CommandException("command '" + String.join(" ", Arrays.asList(command))
					+ "' did not return the expected JSON output: " + rawResult, e);
		}
	}

	private String stripLeadingNonJson(String rawResult) {
		if (rawResult == null) {
			return null;
		}
		int objectStart = rawResult.indexOf('{');
		int arrayStart = rawResult.indexOf('[');
		int jsonStart = objectStart < 0 ? arrayStart : arrayStart < 0 ? objectStart : Math.min(objectStart, arrayStart);
		return jsonStart <= 0 ? rawResult : rawResult.substring(jsonStart);
	}

}
