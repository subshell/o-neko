package io.oneko.automations;

import lombok.Data;

@Data
public class LifetimeBehaviourDTO {
	/**
	 * 0 == infinite, -1 == ignore
	 */
	private int daysToLive;
}
