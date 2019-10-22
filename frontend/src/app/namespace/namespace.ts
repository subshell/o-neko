export interface NamespaceDTO {
  name: string;
  id?: string;
}

export class Namespace implements NamespaceDTO {
  name: string;
  id?: string;

  static from(dto: NamespaceDTO): Namespace {
    let namespace = new Namespace();
    namespace.name = dto.name;
    namespace.id = dto.id;
    return namespace;
  }

  public isImplicit(): boolean {
    return !this.id;
  }

}
