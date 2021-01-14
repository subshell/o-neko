package io.oneko.helmapi.process;

import java.lang.reflect.Type;

import com.google.gson.Gson;

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
		return gson.fromJson(rawResult, returnType);
	}

	public <T> T executeWithJsonOutput(Type returnType, String... command) {
		final String rawResult = delegate.doExecute(command);
		return gson.fromJson(rawResult, returnType);
	}

}
