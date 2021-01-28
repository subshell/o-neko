export interface NamespaceDTO {
  id: string;
  name: string;
}

export class Namespace implements NamespaceDTO {

  id: string;
  name: string;

  public static from(from: NamespaceDTO): Namespace {
    let namespace = new Namespace();
    namespace.id = from.id;
    namespace.name = from.name;
    return namespace;
  }

  public isNew(): boolean {
    return !this.id;
  }
}
