export interface DefinedNamespaceDTO {
  id: string;
  name: string;
}

export class DefinedNamespace implements DefinedNamespaceDTO {

  id: string;
  name: string;

  public static from(from: DefinedNamespaceDTO): DefinedNamespace {
    let namespace = new DefinedNamespace();
    namespace.id = from.id;
    namespace.name = from.name;
    return namespace;
  }

  public isNew(): boolean {
    return !this.id;
  }
}
