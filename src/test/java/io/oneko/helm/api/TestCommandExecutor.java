package io.oneko.helm.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import io.oneko.helm.process.CommandException;
import io.oneko.helm.process.ICommandExecutor;

public class TestCommandExecutor implements ICommandExecutor {

	Map<String, String> commandToResult = new HashMap<>();

	public void addDefinedCommand(String result, String command) {
		this.commandToResult.put(command, result);
	}

	@Override
	public String doExecute(String... command) {
		String commandString = commandToString(command);
		if (commandToResult.containsKey(commandString)) {
			return commandToResult.get(commandString);
		}
		throw new CommandException("Test command not found: " + commandString);
	}

	private String commandToString(String... command) {
		return Arrays.stream(command).collect(Collectors.joining(" "));
	}
}
