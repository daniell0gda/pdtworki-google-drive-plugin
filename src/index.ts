import { registerPlugin } from '@capacitor/core';

import type { GoogleDrivePluginPlugin } from './definitions';

const GoogleDrivePlugin = registerPlugin<GoogleDrivePluginPlugin>('GoogleDrivePlugin', {
  web: () => import('./web').then(m => new m.GoogleDrivePluginWeb()),
});

export * from './definitions';
export { GoogleDrivePlugin };
