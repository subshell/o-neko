package io.oneko.automations;

import org.springframework.stereotype.Component;

@Component
public class LifetimeBehaviourDTOMapper {

	public LifetimeBehaviourDTO toLifetimeBehaviourDTO(LifetimeBehaviour behaviour) {
		LifetimeBehaviourDTO behaviourDTO = new LifetimeBehaviourDTO();
		behaviourDTO.setDaysToLive(behaviour.getDaysToLive());
		return behaviourDTO;
	}

	public LifetimeBehaviour toLifetimeBehaviour(LifetimeBehaviourDTO dto) {
		return dto == null ? null : new LifetimeBehaviour(dto.getDaysToLive());
	}
}
