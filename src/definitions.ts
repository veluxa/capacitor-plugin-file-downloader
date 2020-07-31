declare module '@capacitor/core' {
  interface PluginRegistry {
    FileDownloader: FileDownloaderPlugin;
  }
}

export interface FileDownloaderPlugin {
  download(options: { url: string, filename: string }): Promise<any>;
}
