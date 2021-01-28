import {Registry} from "../registry";

export interface HelmRegistryDTO extends Registry {
  id: string
  url: string
  username: string
}

export class HelmRegistry implements HelmRegistryDTO {
  id: string
  name: string
  url: string
  username: string

  public static from(from: HelmRegistryDTO): HelmRegistry {
    let registry = new HelmRegistry();
    registry.id = from.id;
    registry.name = from.name;
    registry.url = from.url;
    registry.username = from.username;
    return registry;
  }

  public isNew(): boolean {
    return !this.id;
  }

  public getId(): string {
    return this.id;
  }
}
