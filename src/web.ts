import { WebPlugin } from '@capacitor/core';

import type { GoogleDrivePluginPlugin } from './definitions';

export class GoogleDrivePluginWeb extends WebPlugin implements GoogleDrivePluginPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
