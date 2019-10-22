export class UploadedFile {
  public constructor(public content: string | ArrayBuffer | null,
                     public type: string,
                     public name: string) {
  }
}
