export class FileDownloadService {
  public static downloadJSON(obj: any, filename: string = 'file.json'): void {
    const json = JSON.stringify(obj, undefined, 2);
    const blob = new Blob([json], {type: 'application/json'});
    const url = URL.createObjectURL(blob);

    const anchorElement = document.createElement('a');
    document.body.appendChild(anchorElement);
    anchorElement.style.display = 'none';
    anchorElement.href = url;
    anchorElement.download = filename;
    anchorElement.click();
    document.body.removeChild(anchorElement);

    window.URL.revokeObjectURL(url);
  }
}
