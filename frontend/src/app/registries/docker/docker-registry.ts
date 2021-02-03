import {Registry} from "../registry";

export interface DockerRegistryDTO extends Registry {
  uuid: string
  name: string
  registryUrl: string
  userName: string
  trustInsecureCertificate: boolean
}

export class DockerRegistry implements DockerRegistryDTO {
  uuid: string;
  name: string;
  registryUrl: string;
  userName: string;
  trustInsecureCertificate: boolean;

  public static from(from: DockerRegistryDTO): DockerRegistry {
    let registry = new DockerRegistry();
    registry.uuid = from.uuid;
    registry.name = from.name;
    registry.registryUrl = from.registryUrl;
    registry.userName = from.userName;
    registry.trustInsecureCertificate = from.trustInsecureCertificate;
    return registry;
  }

  public isNew(): boolean {
    return !this.uuid;
  }

  public getId(): string {
    return this.uuid;
  }
}
