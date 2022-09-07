package io.oneko.helmapi.process;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;

public class CommandExecutor implements ICommandExecutor {

	public String doExecute(String... command) {
		var processBuilder = new ProcessBuilder();
		processBuilder.command(command);
		try {
			final Process process = processBuilder.start();
			var output = IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);
			var error = IOUtils.toString(process.getErrorStream(), StandardCharsets.UTF_8);
			var exited = process.waitFor(5, TimeUnit.MINUTES);
			if (exited && process.exitValue() > 0) {
				throw new CommandException(error);
			} else if (!exited) {
				process.destroy();
				throw new CommandException("Encountered a timeout while executing the command.");
			} else {
				return output;
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			throw new CommandException(e);
		}
	}

}
