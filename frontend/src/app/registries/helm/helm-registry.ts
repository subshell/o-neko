import {Registry} from "../registry";

export interface HelmRegistryDTO extends Registry {
  id: string
  url: string
  username: string
  ociRegistry: boolean
}

export class HelmRegistry implements HelmRegistryDTO {
  id: string
  name: string
  url: string
  username: string
  ociRegistry: boolean

  public static from(from: HelmRegistryDTO): HelmRegistry {
    let registry = new HelmRegistry();
    registry.id = from.id;
    registry.name = from.name;
    registry.url = from.url;
    registry.username = from.username;
    registry.ociRegistry = from.ociRegistry;
    return registry;
  }

  public isNew(): boolean {
    return !this.id;
  }

  public getId(): string {
    return this.id;
  }
}
