import { WebPlugin } from '@capacitor/core';
import { FileDownloaderPlugin } from './definitions';

export class FileDownloaderWeb extends WebPlugin implements FileDownloaderPlugin {
  constructor() {
    super({
      name: 'FileDownloader',
      platforms: ['web'],
    });
  }

  async download(options: { url: string, filename: string }) {
    return options
  }
}

const FileDownloader = new FileDownloaderWeb();

export { FileDownloader };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(FileDownloader);
